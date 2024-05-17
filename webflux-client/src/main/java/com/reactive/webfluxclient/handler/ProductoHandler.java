package com.reactive.webfluxclient.handler;

import ch.qos.logback.core.model.Model;
import com.reactive.webfluxclient.models.Producto;
import com.reactive.webfluxclient.models.services.ProductoService;
import com.reactive.webfluxclient.models.services.ProductoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@Component
public class ProductoHandler {

    private final ProductoService productoService;

    private static final Logger log = LoggerFactory.getLogger(ProductoHandler.class);

    public ProductoHandler(ProductoService productoService) {
        this.productoService = productoService;
    }

    public Mono<ServerResponse> listar(ServerRequest request){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request){
        return productoService.findById(request.pathVariable("id"))
                .doOnNext(producto -> log.info("El producto traido del api es :" + producto.getNombre()))
                .flatMap( producto -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(producto)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request){

        return request.bodyToMono(Producto.class)
                .flatMap( producto -> {
                    if(producto.getCreateAt()==null)
                        producto.setCreateAt(new Date());
                    return productoService.save(producto);
                }).flatMap( producto -> ServerResponse.created(URI.create("/api/client/products".concat(producto.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(producto)))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException errorResponse = (WebClientResponseException) error;
                        if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(BodyInserters.fromValue(errorResponse.getResponseBodyAsString()));
                        }
                        return Mono.error(errorResponse);
                    }
                    return Mono.error(error);

                });


    }

    public Mono<ServerResponse> editar(ServerRequest request){

        return request.bodyToMono(Producto.class)
                .flatMap( producto -> ServerResponse.created(URI.create("/api/client/products/".concat(request.pathVariable("id"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoService.update(producto,request.pathVariable("id")), Producto.class));


    }

    public Mono<ServerResponse> eliminar(ServerRequest request){

        return productoService.delete(request.pathVariable("id"))
                .then(ServerResponse.noContent().build());


    }


}
