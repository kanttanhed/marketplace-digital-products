package com.github.kanttanhed.user_service.service;

import com.github.kanttanhed.user_service.entity.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserProducer {
    private final KafkaTemplate<String, User> kafkaTemplate;
    private static final String TOPIC = "user-created-topic";

    // Constructor injection of KafkaTemplate
    public UserProducer(KafkaTemplate<String, User> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Method to send a User object to the Kafka topic
    // This method is called when a new user is created
    // It sends the user object to the Kafka topic "user-created-topic"
    // The Kafka topic is used to communicate between different microservices
    // For example, when a new user is created, this method sends the user object to the Kafka topic
    // The other microservices can consume the message from the Kafka topic and perform their own actions
    public void send(User user) {
        kafkaTemplate.send(TOPIC, user);
    }
}
