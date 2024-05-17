package com.reactive.webfluxclient.models.services;

import com.reactive.webfluxclient.models.Categoria;
import com.reactive.webfluxclient.models.Producto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

    public Flux<Producto> findAll();

    public Mono<Producto> findById(String id);
    public Mono<Producto> save(Producto producto);

    public Mono<Producto> update(Producto producto, String id);
    public Mono<Void> delete(String id);

    /* FilePart file ->  Corresponde al archivo
    * String id -> Corresponde al id del objeto (producto) que se quiere modificar */
    public Mono<Producto> upload(FilePart file, String id);



}
