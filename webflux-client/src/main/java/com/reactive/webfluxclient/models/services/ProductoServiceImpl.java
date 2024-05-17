package com.reactive.webfluxclient.models.services;


import com.reactive.webfluxclient.models.Categoria;
import com.reactive.webfluxclient.models.Producto;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/* Para el manejo de errores es mejor usar .retrive() que exchange() al momento de armar la respuesta
 * debido a que este tiene un mejor manejo de errores*/
@Service
@AllArgsConstructor
public class ProductoServiceImpl implements ProductoService{

    // Inyección del bean WebClient
    private final WebClient client;

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);


    @Override
    public Flux<Producto> findAll() {
        return client.get().accept(MediaType.APPLICATION_JSON)
                /*Acá bien se usaria (para la conversión a flux) un :
                * exchange()
                * .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Producto.class))*/
                .exchangeToFlux(clientResponse -> clientResponse.bodyToFlux(Producto.class));
    }


    @Override
    public Mono<Producto> findById(String id) {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("id",id);
        return client.get().uri("/{id}",params)
                .accept(MediaType.APPLICATION_JSON)
                /*Acá bien se usaria (para la conversión a flux) un :
                 * exchange()
                 * .flatMap(clientResponse -> clientResponse.bodyToMono(Producto.class))*/
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return client.post()
                .accept(MediaType.APPLICATION_JSON)
                // Tipo de contenido que estamos enviando
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(producto)
                /// usar mejor .retrieve porque es a más alto nivel y hace conversiones automaticas
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {
        /*Collections.singletonMap("id",id) -> Toma el parametro id del metodo y se lo pasa a la uri*/

        return client.put()
                .uri("/{id}", Collections.singletonMap("id",id))
                .accept(MediaType.APPLICATION_JSON)
                // Tipo de contenido que estamos enviando
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Void> delete(String id) {
        return client.delete()
                .uri("/{id}", Collections.singletonMap("id",id))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Producto> upload(FilePart file, String id) {
        /* MultipartBodyBuilder -> Se usa en el cliente para obtener el file tambien del request pero ademas
                                   pasarlo al webClient en el request para poder enviarlo al back 'api-rest' como un
                                   multipartData
        * request.multipartData() -> Se usa en el back para obtener el file como un 'Filepart'*/
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        /* Aquí, MultipartBodyBuilder se usa para crear una parte multipart que contiene el archivo.
         * La parte se llama "file" y el contenido del archivo se obtiene de file.content(),
         * que devuelve un flujo de DataBuffer.*/
        parts.asyncPart("file",file.content(), DataBuffer.class)
                .headers(httpHeaders -> {
                    // Se agrega el file como nombre parametro
                    httpHeaders.setContentDispositionFormData("file", file.filename());
                });

        /* Nuestro back/api-rest espera en el body un archivo (formato multipartData)
            y en el pathVariable un id. No más !!!*/

        return client.post()
                .uri("/upload/{id}", Collections.singletonMap("id",id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(parts.build())
                .retrieve()
                // La respuesta del servidor debe ser un Mono<Producto>
                .bodyToMono(Producto.class);

    }


}
