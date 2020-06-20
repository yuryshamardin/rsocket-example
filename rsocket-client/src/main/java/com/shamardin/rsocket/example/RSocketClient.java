package com.shamardin.rsocket.example;

import com.shamardin.rsocket.example.dto.Message;
import io.rsocket.SocketAcceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.UUID;

@Slf4j
@ShellComponent
public class RSocketClient {
    private final RSocketRequester rsocketRequester;
    private final String clientId = UUID.randomUUID().toString();

    @Autowired
    public RSocketClient(RSocketRequester.Builder rsocketRequesterBuilder, RSocketStrategies strategies) {
        log.info("Connecting using client ID: {}", clientId);
        SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientHandler());

        this.rsocketRequester = rsocketRequesterBuilder
                .setupRoute("connecting")
                .setupData(clientId)
                .rsocketStrategies(strategies)
                .rsocketConnector(connector -> connector.acceptor(responder))
                .connectWebSocket(URI.create("http://localhost:7000/websocket"))
                .block();

        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(error -> log.warn("Connection CLOSED"))
                .doFinally(consumer -> log.info("Client DISCONNECTED"))
                .subscribe();
    }

    @PreDestroy
    void shutdown() {
        rsocketRequester.rsocket().dispose();
    }

    @ShellMethod("Send one request for broadcasting. Just type broadcast \"message for broadcasting\" ")
    public void broadcast(@ShellOption String text) {
        log.info("Sending one request. Waiting for one response...");
        final byte[] usefulData = text.getBytes();
        final Message message = Message.builder().data(usefulData).clientId(clientId).build();
        this.rsocketRequester
                .route("broadcast")
                .data(message)
                .send()
                .block();
        log.info("Sent!");
    }
}
