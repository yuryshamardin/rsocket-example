package com.shamardin.rsocket.example;

import io.rsocket.RSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class RSocketBroadcaster {

    private final Map<String, RSocketRequester> clients = new HashMap<>();

    @PreDestroy
    void shutdown() {
        log.info("Detaching all remaining clients...");
        clients.values().stream().map(RSocketRequester::rsocket).forEach(RSocket::dispose);
        log.info("Shutting down.");
    }

    @ConnectMapping("connecting")
    void connect(RSocketRequester requester, @Payload String clientId) {
        requester.rsocket()
                .onClose()
                .doFirst(() -> {
                    log.info("Client: {} CONNECTED.", clientId);
                    clients.put(clientId, requester);
                })
                .doOnError(error -> {
                    log.warn("Channel to client {} CLOSED", clientId);
                })
                .doFinally(consumer -> {
                    clients.remove(clientId);
                    log.info("Client {} DISCONNECTED", clientId);
                })
                .subscribe();
    }

    @MessageMapping("broadcast")
    public void broadcast(Message message) {
        log.info("Starting broadcasting");
        clients.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(message.getClientId()))
                .forEach(client -> sendData(client, message.getData()));
    }

    private void sendData(Map.Entry<String, RSocketRequester> client, byte[] data) {
        log.info("Sent to client " + client.getKey());
        client.getValue().route("receive-data")
                .data(data)
                .retrieveMono(String.class)
                .subscribe(response ->
                        log.info("Client {} got the data. Response was: [{}]", client.getKey(), response));

    }
}
