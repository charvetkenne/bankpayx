package com.mansa.application.port.out;

import com.mansa.domain.event.DomainEvent;
import java.util.List;

public interface EventPublisherPort {

    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
