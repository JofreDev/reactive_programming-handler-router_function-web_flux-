package com.reactive.functionalendpoints.models.dao;


import com.reactive.functionalendpoints.models.documents.Categoria;
import com.reactive.functionalendpoints.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

    public Mono<Categoria> findByNombre(String nombre);
}
