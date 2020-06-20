package com.shamardin.rsocket.example;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Message {
    String clientId;
    byte[] data;
}
