package com.maovares.ms_products;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.QueueMessageEncoding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueEventPublisher {

    private final QueueClient queueClient;
    private final ObjectMapper objectMapper;

    public QueueEventPublisher(
            ObjectMapper objectMapper,
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.queue-name}") String queueName) {
        this.objectMapper = objectMapper;
        this.queueClient = new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                // Azure Functions queue triggers use Base64 by default.
                .messageEncoding(QueueMessageEncoding.BASE64)
                .buildClient();
    }

    public void publish(OrderEvent order) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize order event", exception);
        }
        queueClient.sendMessage(json);
    }
}
