package com.reactive.webfluxclient.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Config {

    /*Clase de configuración para consumir alguna
    * api rest de manera reactiva*/

    private final String pathEndpoint;

    public Config(@Value("${config.base.endpoint}") String pathEndpoint) {
        this.pathEndpoint = pathEndpoint;
    }

    @Bean
    public WebClient registrarWebClient(){
        return WebClient.create(pathEndpoint);
    }
}
