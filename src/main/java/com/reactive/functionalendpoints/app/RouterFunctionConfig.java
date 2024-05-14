package com.reactive.functionalendpoints.app;

import com.reactive.functionalendpoints.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler productoHandler){
        // Podemos tener varias rutas para el handler
        return route(GET("/api/v2/products").or(GET("/api/v3/products")), productoHandler::listar)
                //.and(contentType(MediaType.APPLICATION_JSON)) -> Valida la estructura del request
                .andRoute(GET("/api/v2/products/{id}")/*.and(contentType(MediaType.APPLICATION_JSON))*/, productoHandler::verDetalle)
                .andRoute(POST("/api/v2/products"),productoHandler::crear)
                .andRoute(PUT("/api/v2/products/{id}"),productoHandler::editar)
                .andRoute(DELETE("/api/v2/products/{id}"),productoHandler::eliminar);
    }

}
