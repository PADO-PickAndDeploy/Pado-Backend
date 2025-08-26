package org.pado.api.core.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitSimpleConfig {

    @Bean
    public DirectExchange directExchange(@Value("${app.rabbit.exchange}") String ex) {
        return new DirectExchange(ex, true, false);
    }

    @Bean
    public Queue queue(@Value("${app.rabbit.queue}") String q) {
        return new Queue(q, true);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange ex,
                           @Value("${app.rabbit.routing-key}") String key) {
        return BindingBuilder.bind(queue).to(ex).with(key);
    }
}