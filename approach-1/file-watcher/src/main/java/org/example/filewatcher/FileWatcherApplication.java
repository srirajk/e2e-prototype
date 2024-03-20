package org.example.filewatcher;

import org.example.filewatcher.utility.FileWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class FileWatcherApplication implements CommandLineRunner {

    private FileWatcher fileWatcher;

    public FileWatcherApplication(@Autowired final FileWatcher fileWatcher) {
        this.fileWatcher = fileWatcher;
    }

    public static void main(String[] args) {
        SpringApplication.run(FileWatcherApplication.class, args);
    }

    public void run(String... args) throws Exception {
        fileWatcher.watch();
    }

}
