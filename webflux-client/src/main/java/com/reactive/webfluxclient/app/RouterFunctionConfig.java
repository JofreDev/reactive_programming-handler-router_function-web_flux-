package com.reactive.webfluxclient.app;

import com.reactive.webfluxclient.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler productoHandler){
        // Podemos tener varias rutas para el handler
        return route(GET("/api/client/products"), productoHandler::listar)
                //.and(contentType(MediaType.APPLICATION_JSON)) -> Valida la estructura del request
                .andRoute(GET("/api/client/products/{id}")/*.and(contentType(MediaType.APPLICATION_JSON))*/, productoHandler::ver)
                .andRoute(POST("/api/client/products")/*.and(contentType(MediaType.APPLICATION_JSON))*/, productoHandler::crear)
                .andRoute(PUT("/api/client/products/{id}")/*.and(contentType(MediaType.APPLICATION_JSON))*/, productoHandler::editar)
                .andRoute(DELETE("/api/client/products/{id}")/*.and(contentType(MediaType.APPLICATION_JSON))*/, productoHandler::eliminar);
    }
}
