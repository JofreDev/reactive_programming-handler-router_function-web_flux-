package com.reactive.webfluxclient.models;

import lombok.*;
import java.util.Date;

//dto
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Producto {

    private String id;
    private String nombre;
    private Double precio;
    private Date createAt;
    private String foto;
    private Categoria categoria;
}
