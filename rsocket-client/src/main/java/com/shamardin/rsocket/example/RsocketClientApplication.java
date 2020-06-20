package com.shamardin.rsocket.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;

@ShellComponent
@SpringBootApplication
public class RsocketClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsocketClientApplication.class, args);
    }

}
