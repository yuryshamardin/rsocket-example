package com.shamardin.rsocket.example.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Message {
    String clientId;
    byte[] data;
}
