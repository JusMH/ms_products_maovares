package com.maovares.ms_products;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final QueueEventPublisher queueEventPublisher;

    public OrderController(QueueEventPublisher queueEventPublisher) {
        this.queueEventPublisher = queueEventPublisher;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderEvent order) {
        if (order.orderId() == null || order.orderId().isBlank()) {
            return ResponseEntity.badRequest().body("Order data missing");
        }

        queueEventPublisher.publish(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Order " + order.orderId() + " enqueued successfully");
    }
}
