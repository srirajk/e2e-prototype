package org.example;

import java.io.Serializable;

public class KafkaStreamingConfig implements Serializable {

    private AppDetails appDetails;
    private Kafka kafka;
    private Deltalake deltalake;

    // getters and setters

    public AppDetails getAppDetails() {
        return appDetails;
    }

    public void setAppDetails(AppDetails appDetails) {
        this.appDetails = appDetails;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public Deltalake getDeltalake() {
        return deltalake;
    }

    public void setDeltalake(Deltalake deltalake) {
        this.deltalake = deltalake;
    }

    public static class AppDetails implements Serializable {
        private String appName;
        private String master;
        private String batchDuration;
        private String checkpointLocation;

        // Getters and Setters
        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getMaster() {
            return master;
        }

        public void setMaster(String master) {
            this.master = master;
        }

        public String getBatchDuration() {
            return batchDuration;
        }

        public void setBatchDuration(String batchDuration) {
            this.batchDuration = batchDuration;
        }

        public String getCheckpointLocation() {
            return checkpointLocation;
        }

        public void setCheckpointLocation(String checkpointLocation) {
            this.checkpointLocation = checkpointLocation;
        }
    }

    public static class Kafka implements Serializable {
        private String brokers;
        private String inputTopic;
        private String notificationTopic;
        private String groupId;

        // getters and setters

        public String getNotificationTopic() {
            return notificationTopic;
        }

        public void setNotificationTopic(String notificationTopic) {
            this.notificationTopic = notificationTopic;
        }

        public String getInputTopic() {
            return inputTopic;
        }

        public void setInputTopic(String inputTopic) {
            this.inputTopic = inputTopic;
        }

        public String getBrokers() {
            return brokers;
        }

        public void setBrokers(String brokers) {
            this.brokers = brokers;
        }


        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
    }

    public static class Deltalake implements Serializable {

        private String storageLocation;

        private String tableName;

        // getters and setters

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getStorageLocation() {
            return storageLocation;
        }

        public void setStorageLocation(String storageLocation) {
            this.storageLocation = storageLocation;
        }


    }
}