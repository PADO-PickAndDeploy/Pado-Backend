package org.pado.api.core.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Getter
public class RabbitSendService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.exchange}") private String defaultExchange;
    @Value("${app.rabbit.routing-key-stop}") private String stopRoutingKey;
    @Value("${app.rabbit.routing-key-start}") private String startRoutingKey;

    public void send(String exchange, String routingKey, String text) {
        rabbitTemplate.convertAndSend(exchange, routingKey, text);
    }

    public void sendStart(String text) {
        rabbitTemplate.convertAndSend(defaultExchange, startRoutingKey, text);
    }

    public void sendStop(String text) {
        rabbitTemplate.convertAndSend(defaultExchange, stopRoutingKey, text);
    }
}
