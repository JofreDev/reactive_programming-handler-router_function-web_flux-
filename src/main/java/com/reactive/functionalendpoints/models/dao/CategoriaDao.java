package com.reactive.functionalendpoints.models.dao;


import com.reactive.functionalendpoints.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
}
