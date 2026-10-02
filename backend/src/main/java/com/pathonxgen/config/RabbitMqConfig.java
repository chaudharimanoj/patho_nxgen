package com.pathonxgen.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central exchange for domain events (tenant.provisioned, sample.status.changed,
 * billing.invoice.created, etc). Each module publishes to this exchange with a
 * routing key of the form "<module>.<event>"; consumers bind their own queues.
 */
@Configuration
public class RabbitMqConfig {

    public static final String DOMAIN_EVENTS_EXCHANGE = "patho.domain.events";

    @Bean
    public TopicExchange domainEventsExchange() {
        return new TopicExchange(DOMAIN_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue("notification.queue", true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange domainEventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(domainEventsExchange).with("*.notify.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
