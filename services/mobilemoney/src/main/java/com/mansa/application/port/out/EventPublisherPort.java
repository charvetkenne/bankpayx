package com.mansa.application.port.out;

import com.mansa.domain.event.DomainEvent;

public interface EventPublisherPort {

    void publish(DomainEvent event, String topic);
}
