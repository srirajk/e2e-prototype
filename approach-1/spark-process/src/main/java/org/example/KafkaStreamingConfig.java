package org.example;

import java.io.Serializable;

public class KafkaStreamingConfig implements Serializable {
    private Kafka kafka;
    private Deltalake deltalake;

    // getters and setters
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

    public static class Kafka implements Serializable {
        private String brokers;
        private String topic;
        private String groupId;

        // getters and setters
        public String getBrokers() {
            return brokers;
        }

        public void setBrokers(String brokers) {
            this.brokers = brokers;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
    }

    public static class Deltalake implements Serializable {
        private String location;

        // getters and setters
        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}