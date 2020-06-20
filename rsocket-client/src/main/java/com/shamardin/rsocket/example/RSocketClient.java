package com.shamardin.rsocket.example;

import io.rsocket.SocketAcceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@ShellComponent
public class RSocketClient {
    private final RSocketRequester rsocketRequester;
    private final String clientId;

    @Autowired
    public RSocketClient(RSocketRequester.Builder rsocketRequesterBuilder, RSocketStrategies strategies) {
        clientId = UUID.randomUUID().toString();
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

    @ShellMethod("Send one request. One response will be printed. request-response")
    public void requestResponse() {
        log.info("\nSending one request. Waiting for one response...");
        final byte[] usefulData = "some message".getBytes();
        final Message message = Message.builder().data(usefulData).clientId(clientId).build();
        this.rsocketRequester
                .route("broadcast")
                .data(message)
                .send()
                .block();
        log.info("\nSent!");
    }
}

@Slf4j
class ClientHandler {
    @MessageMapping("client-example-data")
    public Flux<String> statusUpdate(String status) {
        log.info("\nConnection {}", status);
        return Flux.interval(Duration.ofSeconds(5)).map(index -> String.format("Free memory %s",
                Runtime.getRuntime().freeMemory()));
    }

    @MessageMapping("receive-data")
    public Mono<String> getData(byte[] data) {
        log.info("\nI've got data!" + new String(data));
        return Mono.just(String.format("I've got %d bytes", data.length));
    }
}
