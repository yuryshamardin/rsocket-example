package com.shamardin.rsocket.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import reactor.core.publisher.Mono;

@Slf4j
public class ClientHandler {

    @MessageMapping("receive-data")
    public Mono<String> getData(byte[] data) {
        return Mono.just(String.format("I've got %d bytes, decoded message is %s", data.length, new String(data)));
    }
}
