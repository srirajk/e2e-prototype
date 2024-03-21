package org.example.filewatcher.utility;

import lombok.extern.log4j.Log4j2;
import org.example.common.model.FileEvent;
import org.example.common.utility.Utility;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Log4j2
public class FileWatcher {

    /**
     * The directory to be monitored for file changes.
     */
    private final Path dir;

    /**
     * A map to store the last modification time of each file in the directory.
     * This is used to implement the quiet period feature.
     */
    private final Map<Path, Instant> lastModifiedTimes = new HashMap<>();

    /**
     * The quiet period in milliseconds.
     * After a file change event is detected, further changes to the same file within the quiet period are ignored.
     * This is useful in situations where a file might be updated multiple times in quick succession,
     * but you only want to process the final state of the file.
     */
    private final long quietPeriodMillis;

    /**
     * The polling interval in milliseconds.
     * The watcher service checks for file changes every pollIntervalMillis milliseconds.
     */
    private final long pollIntervalMillis;

    private final String fileExtension;

    private final KafkaProducer kafkaProducer;

    /**
     * Constructs a new FileWatcher with the specified directory, quiet period, and polling interval.
     *
     * @param dir                the directory to be monitored for file changes
     * @param quietPeriodMillis  the quiet period in milliseconds
     * @param pollIntervalMillis the polling interval in milliseconds
     */
    public FileWatcher(final String dir, final long quietPeriodMillis,
                       final long pollIntervalMillis,
                       final String fileExtension,
                       final KafkaProducer kafkaProducer) {
        this.dir = Paths.get(dir);
        Utility.createDirectoryIfNotExists(this.dir.toAbsolutePath().toString());
        this.quietPeriodMillis = quietPeriodMillis;
        this.pollIntervalMillis = pollIntervalMillis;
        this.fileExtension = fileExtension;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Starts the file watcher service.
     * The service runs in an infinite loop, checking for file changes every pollIntervalMillis milliseconds.
     * When a file change event is detected, it checks the current time against the last modification time stored in the map.
     * If the difference is less than the quiet period, the event is ignored.
     * If the difference is greater than the quiet period, the event is processed and the last modification time in the map is updated.
     *
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the thread is interrupted
     */
    public void watch() throws IOException, InterruptedException {
        processExistingFiles();
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

        log.info("Watch Service registered for dir: " + dir.getFileName());

        while (true) {
            WatchKey key;
            try {
                key = watcher.poll(pollIntervalMillis, TimeUnit.MILLISECONDS);
                if (key == null) {
                    continue;
                }
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                Path child = dir.resolve(fileName);
                if (Files.notExists(child) || !child.toString().endsWith(fileExtension)) {
                    lastModifiedTimes.remove(child);
                    continue;
                }

                FileTime fileTime = Files.getLastModifiedTime(child);
                Instant lastModifiedTime = fileTime.toInstant();
                Instant lastRecordedTime = lastModifiedTimes.get(child);

                if (lastRecordedTime != null && lastModifiedTime.isBefore(lastRecordedTime.plusMillis(quietPeriodMillis))) {
                    continue;
                }

                // Try to get an exclusive lock on the file
                try (FileChannel channel = FileChannel.open(child, StandardOpenOption.WRITE)) {
                    try {
                        FileLock lock = channel.tryLock();
                        if (lock == null) {
                            // File is currently being written to, skip this event
                            continue;
                        }
                        lock.release();
                    } catch (OverlappingFileLockException e) {
                        // File is currently being written to, skip this event
                        continue;
                    }
                } catch (IOException e) {
                    // Error opening the file, skip this event
                    continue;
                }

                lastModifiedTimes.put(child, lastModifiedTime);

                log.info(kind.name() + ": " + fileName);
                File file = child.toFile();

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    // Process create event
                    createFileEvent(file);
                    /*Path readPath = Paths.get(child.toString() + ".read");
                    Files.move(child, readPath, StandardCopyOption.REPLACE_EXISTING);*/
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    // TODO
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    // TODO
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    /**
     * Processes all existing files in the directory at the start.
     *
     * @throws IOException if an I/O error occurs
     */
    private void processExistingFiles() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry) || !entry.toString().endsWith(fileExtension)) {
                    continue;
                }
                File file = entry.toFile();
                // Process the file
                createFileEvent(file);

                // Rename the file after reading
              /*  Path readPath = Paths.get(entry.toString() + ".read");
                Files.move(entry, readPath, StandardCopyOption.REPLACE_EXISTING);*/
            }
        }
    }

    private void createFileEvent(File file) throws IOException {
        var fileName = file.getName();
        //Pattern pattern = Pattern.compile("^(.*)_([^_]*)_.*\\.txt$");
        Pattern pattern = Pattern.compile("^([^_]*)_([^_]*)_(.*)\\.txt$");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String businessId = matcher.group(1);
            String productId = matcher.group(2);
            FileEvent fileEvent = new FileEvent(businessId, productId, file.getAbsolutePath(),
                    getUtcLastModifiedTime(file.toPath()),
                    "ENTRY_CREATE");
            log.info("File event created: {}", fileEvent);
            kafkaProducer.publishEvent(fileEvent);
        } else {
            log.error("IGNORED :: File name does not match the pattern: {}", fileName);
        }

    }

    private String getUtcLastModifiedTime(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        Instant timestamp = attrs.lastModifiedTime().toInstant();
        return timestamp.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT);
    }



    public static void main(String[] args) {
        try {
            FileWatcher fileWatcher = new FileWatcher("/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/demo-test-folders",
                    TimeUnit.SECONDS.toMillis(10),
                    TimeUnit.SECONDS.toMillis(5),
                    ".txt", null);
            fileWatcher.watch();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}