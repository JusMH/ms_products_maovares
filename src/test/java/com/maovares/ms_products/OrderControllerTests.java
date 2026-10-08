package com.maovares.ms_products;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueueEventPublisher queueEventPublisher;

    @Test
    void validOrderIsPublishedAndReturnsCreated() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"ORD-S10-001","customerEmail":"test@correo.com",
                                 "total":250.00,"items":[{"sku":"BOOK-S10","qty":1}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order ORD-S10-001 enqueued successfully"));

        verify(queueEventPublisher).publish(new OrderEvent(
                "ORD-S10-001", "test@correo.com", 250.00,
                List.of(new OrderEvent.Item("BOOK-S10", 1))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"orderId\":null}", "{\"orderId\":\"\"}", "{\"orderId\":\"   \"}"})
    void missingOrBlankOrderIdIsRejected(String json) throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order data missing"));

        verifyNoInteractions(queueEventPublisher);
    }

    @Test
    void publisherFailureDoesNotReturnCreated() {
        OrderEvent order = new OrderEvent("ORD-S10-001", null, 0, null);
        doThrow(new IllegalStateException("Queue unavailable")).when(queueEventPublisher).publish(order);

        assertThrows(IllegalStateException.class,
                () -> new OrderController(queueEventPublisher).createOrder(order));
    }
}
