package org.pado.api.core.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitSendService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.exchange}") private String defaultExchange;
    @Value("${app.rabbit.routing-key}") private String defaultRoutingKey;

    public void send(String exchange, String routingKey, String text) {
        rabbitTemplate.convertAndSend(exchange, routingKey, text);
    }

    public void sendDefault(String text) {
        rabbitTemplate.convertAndSend(defaultExchange, defaultRoutingKey, text);
    }
}
