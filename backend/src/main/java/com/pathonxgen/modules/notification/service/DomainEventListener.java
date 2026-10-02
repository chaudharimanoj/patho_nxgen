package com.pathonxgen.modules.notification.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Consumes domain events (sample status changes, invoice created, etc.)
 * from the shared exchange and fans out to SMS / WhatsApp / Email adapters.
 * Kept decoupled from the publishing modules - notification never imports
 * laboratory/billing entities directly, only the event payload shape.
 */
@Service
public class DomainEventListener {

    @RabbitListener(queues = "notification.queue")
    public void onDomainEvent(Map<String, Object> event) {
        // TODO: route to SMS gateway / WhatsApp Business API / SMTP based on
        // tenant notification preferences and event type.
    }
}
