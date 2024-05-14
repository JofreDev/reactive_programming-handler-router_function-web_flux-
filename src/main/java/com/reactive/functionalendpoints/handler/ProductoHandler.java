package com.reactive.functionalendpoints.handler;

import com.reactive.functionalendpoints.models.documents.Producto;
import com.reactive.functionalendpoints.models.services.ProductoService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@Component
public class ProductoHandler {

    /*ServerResponse es por defecto reactivo*/

    private final ProductoService productoService;


    public ProductoHandler(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Recibe un ServerRequest, devuelve un ServerResponse
    public Mono<ServerResponse> listar(ServerRequest request){

       return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                // el body por defecto devuelve un observable
                // Primer parametro la logica a ejecutar y segundo paramatro la clase o tipo de objeto del observable
                .body(productoService.findAll(), Producto.class);



    }

    public Mono<ServerResponse> verDetalle(ServerRequest request){

        return productoService.findById(request.pathVariable("id"))
                .flatMap( producto -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        // body espera un publisher u observable y como estamos dentro del flatMap usamos 'BodyInserters.fromValue'
                        .body(BodyInserters.fromValue(producto)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    /* ¿ Una forma menos reactiva ?
    public Mono<ServerResponse> verDetalle2(ServerRequest request){

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                // el body por defecto devuelve un observable
                // Primer parametro la logica a ejecutar y segundo paramatro la clase o tipo de objeto del observable
                .body(productoService.findById(request.pathVariable("id")), Producto.class);

    }
    */

    public Mono<ServerResponse> crear(ServerRequest request){

        return request.bodyToMono(Producto.class)
                .flatMap(p -> {
                    if(p.getCreateAt()==null)
                        p.setCreateAt(new Date());
                    return productoService.save(p);
                })
                .flatMap( p -> ServerResponse
                        .created(URI.create("/api/v2/products".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        // .body(BodyInserters.fromValue(producto)) -> Por si solo devuelve un 'Mono<ServerResponse>' por lo que se debe aplanar todo con flatMap
                        .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    // Otra opción del metodo editar (que yo propuse)  (solo con faltMap  para la operación funcional)!!

    public Mono<ServerResponse> editarv2(ServerRequest request){


        return productoService.findById(request.pathVariable("id"))
                .flatMap( productoBD ->  {

                     return request.bodyToMono(Producto.class)
                             /*al usar 'request.bodyToMono(Producto.class)' ya estamos sobre un Mono
                             * ahora con 'productoService.save(productoBD)' se devuelve un nuevo Mono<Producto>, es decir,
                             * que se devolveria un Mono<Mono<Producto>>
                             * por lo tanto podemos usar flatMap para aplanarlo y devolver un solo Mono<Producto>
                             * */
                            .flatMap( productoRequest -> { // Acá no se puede usar map porque devolveria Mono<Mono<Producto>>

                                productoBD.setCategoria(productoRequest.getCategoria());
                                productoBD.setNombre(productoRequest.getNombre());
                                productoBD.setPrecio(productoRequest.getPrecio());
                                return  productoService.save(productoBD) ;

                            });
                }).flatMap( p -> ServerResponse
                        .created(URI.create("/api/v2/products".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        // .body(BodyInserters.fromValue(producto)) -> Por si solo devuelve un 'Mono<ServerResponse>' por lo que se debe aplanar todo con flatMap
                        .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> editar(ServerRequest request){

        /* Uso de zipWith :
         * Publisher1.zipWith(Publisher2, (alias-Publisher1, alias-Publisher2) -> {
         *  // Logica
         *  return objeto (se retorna como publisher)
         * }) */

        return productoService.findById(request.pathVariable("id"))
                // Me permite trabajar con 2 flujos al tiempo
                .zipWith(request.bodyToMono(Producto.class), (productDB, productRequest) -> {

                    productDB.setCategoria(productRequest.getCategoria());
                    productDB.setNombre(productRequest.getNombre());
                    productDB.setPrecio(productRequest.getPrecio());
                    /* Acá no se puede hacer un productoService.save(productoBD) directamente
                       porque devolveria un Mono<Mono<Producto>> debido a que zipWith devuelve ya un Mono<Object>
                       por defecto
                    */
                    return  productDB;

                }).flatMap( p -> ServerResponse
                        .created(URI.create("/api/v2/products".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        // .body(BodyInserters.fromValue(producto)) -> Por si solo devuelve un 'Mono<ServerResponse>' por lo que se debe aplanar todo con flatMap
                        .body(productoService.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> eliminar(ServerRequest request){

        return productoService.findById(request.pathVariable("id"))
                .flatMap( producto ->  productoService.delete(producto)
                        // Se usa .then porque acá se devuelve un 'Mono<void>')
                                .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());

    }
}
