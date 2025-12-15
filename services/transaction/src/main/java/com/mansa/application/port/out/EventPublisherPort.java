package com.mansa.application.port.out;


public interface EventPublisherPort {
  void publish(String topic, Object event);
}
// └─  ├─