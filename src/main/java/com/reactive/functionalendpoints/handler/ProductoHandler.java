package com.reactive.functionalendpoints.handler;

import com.reactive.functionalendpoints.models.documents.Categoria;
import com.reactive.functionalendpoints.models.documents.Producto;
import com.reactive.functionalendpoints.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class ProductoHandler {

    /*ServerResponse es por defecto reactivo*/

    private final ProductoService productoService;

    private final String ruta;

    /*Importante !! Para validaciones */
    private final Validator validator;


    public ProductoHandler(ProductoService productoService, @Value("${configuration.path}")String ruta, Validator validator) {
        this.productoService = productoService;
        this.ruta = ruta;
        this.validator = validator;
    }

    public Mono<ServerResponse> crearConFoto(ServerRequest request){

        // Construimos el objeto producto proveniente del request (form-data)

        // Obtengo el multipartData y extraigo solo el 'file'
        return request.multipartData().map( multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> {
                    /*Objetivo : Usamos  request.multipartData() porque la información está siendo enviada desde
                    * un formulario de postman debido a que va con una imagen. Por lo anterior usamos un .map()
                    * para crear/modificar el Mono<multipartData> a Mono<Producto> creando ah[i el objeto.
                    * Como estamos usando .map() es necesario solo devolver el objeto -Producto- y ya queda
                    * encapsulado en el Mono*/
                    return request.multipartData().map( multipart -> {
                        FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
                        FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
                        FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
                        FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

                        Categoria categoria = Categoria.builder().nombre(categoriaNombre.value()).id(categoriaId.value()).build();
                        return Producto.builder().nombre(nombre.value()).precio(Double.valueOf(precio.value())).categoria(categoria).build();
                        /*A continuación usamos el filePart para terminar de armar el objeto Producto,
                        * guardar el File en disco y guardar el objeto producto en BD*/
                    }).flatMap( producto -> {
                        producto.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
                                .replace(" ","-")
                                .replace(":","")
                                .replace("\\",""));
                        producto.setCreateAt(new Date());
                        return filePart.transferTo(new File(ruta + producto.getFoto()))
                                .then(productoService.save(producto));

                    });
                }).flatMap( p -> ServerResponse
                .created(URI.create("/api/v2/products".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(p)));

    }

    public Mono<ServerResponse> upload(ServerRequest request){

        // Obtengo el multipartData y extraigo solo el 'file'
        return request.multipartData().map( multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> productoService.findById(request.pathVariable("id"))
                        .flatMap( producto -> {
                            producto.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
                                    .replace(" ","-")
                                    .replace(":","")
                                    .replace("\\",""));
                            return filePart.transferTo(new File(ruta + producto.getFoto()))
                                    .then(productoService.save(producto));

                        })).flatMap( p -> ServerResponse
                        .created(URI.create("/api/v2/products".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());

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
                    // Errors para la validación del objeto a crear, es decir, 'p'
                    Errors errors = new BeanPropertyBindingResult(p,Producto.class.getName());
                    validator.validate(p,errors);
                    if (errors.hasErrors()){
                        return Flux.fromIterable(errors.getFieldErrors())
                                .map(fieldError -> "El campo "+fieldError.getField()+" "+fieldError.getDefaultMessage())
                                .collectList()
                                .flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromValue(list)));
                    }else {

                        if(p.getCreateAt()==null)
                            p.setCreateAt(new Date());
                        return productoService.save(p)
                                .flatMap( productoDB -> ServerResponse
                                        .created(URI.create("/api/v2/products".concat(p.getId())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        // .body(BodyInserters.fromValue(producto)) -> Por si solo devuelve un 'Mono<ServerResponse>' por lo que se debe aplanar todo con flatMap
                                        .body(BodyInserters.fromValue(productoDB)))
                                .switchIfEmpty(ServerResponse.notFound().build());


                    }

                });

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
