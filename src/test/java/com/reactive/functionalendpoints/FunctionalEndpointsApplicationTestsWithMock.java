package com.reactive.functionalendpoints;

import com.reactive.functionalendpoints.models.documents.Categoria;
import com.reactive.functionalendpoints.models.documents.Producto;
import com.reactive.functionalendpoints.models.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/* @SpringBootTest -> Le indica a la aplicaci[on se spring que tome la configuraci[on principal
* de la aplicación, es decir, el context application
*
* webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT -> Para levantar un servidor para las pruebas con un puerto
* generado siempre de manera aleatoria que este siempre disponible.
*
* @AutoConfigureWebTestClient -> para que tome la configuración de manera automática
*
* 'gradle test' o 'gradlew test' -> Comando de ejecución de test desde la consola */

@AutoConfigureWebTestClient
/*No levanta todo un server para hacer las pruebas*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class FunctionalEndpointsApplicationTestsWithMock {

	@Autowired
	private  WebTestClient client;

	@Autowired
	private ProductoService productoService;

	@Value("${config.base.endpoint}")
	private String basePath;


    @Test
	void listarTest() {

		client.get()
				// uri de los endpoints a probar
				.uri(basePath)  // Uri
				// Especificamos aceptar un determinado MEDIA.TYPE
				.accept(MediaType.APPLICATION_JSON)
				/*  metodo exchange para enviar nuestro request al endpoint y para consumir nuestro
				* apirest de intercambio. Restorna un response de tipo ResponseSpec*/
				.exchange()
				/*Con la respuesta obtenida podemos realizar pruebas como :
				* 1. expectStatus() : estatus esperado
				* 2. expectHeader()
				* 3. expectBodyList('Clase.class') - con hasSize(numero) donde especificamos, en este caso, tamaño de lista
				* 4. consumeWith( var -> {... return x}) operador con el que podemos hacer multiples pruebas con la respuesta
				* */
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				/*Especificamos tipo de respuesta y tipo de objeto*/
				.expectBodyList(Producto.class)
				.consumeWith(response -> {

					List<Producto> productos = response.getResponseBody();
					productos.forEach( producto -> System.out.println(producto.getNombre()));
                    Assertions.assertFalse(productos.isEmpty());

				});


				//.hasSize(9);

	}

	@Test
	void verTest() {

		/* En este test no podemos hacer directamente una busqueda por id,
		* debido a que cada vez que se inicia la app, se eliminan y vuelven a
		* crear los documentos en base de datos. Por lo tanto los id´s estan
		* cambiando constantemente.
		*
		* Solución :
		*
		* Por ende la solución es hacer una busqueda del objeto por nombre y luego si
		* pasar el id del mismo de manera manual al test. Para esto se creo una query
		* personalizada en el DAO de producto con el fin de hacer dicha busqueda por
		* nombre.
		* */

		/*Para acceder a producto no se puede haciendolo con un subscribe
		 * dentro de un observable, ya que la ejecución debe ocurrir
		 * dentro del contexto del metodo. Por ende se usa .block(),es decir,
		 * se accede de forma sincrona al objeto en pruebas unitarias */

		/* comprobamos los paramatros del body como queramos
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.id")... */

		Producto producto = productoService.findByNombre("TV Panasonic").block();

		client.get()
				// Collections.singletonMap("id", producto) -> Le pasamos a la ruta el id ({id}) obteniedolo del producto
				.uri(basePath+"/{id}", Collections.singletonMap("id", producto.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				/* Una forma de hacerlo
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("TV Panasonic");
				* */
				.expectBody(Producto.class)
				.consumeWith(response -> {
					Producto producto1 = response.getResponseBody();
                    Assertions.assertFalse(producto1.getId().isEmpty());
                    Assertions.assertEquals("TV Panasonic", producto1.getNombre());
				});

	}

	@Test
	void creaProductoTest() {

		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();

		client.post().uri(basePath)
				// Tipo de contenido en request
				.contentType(MediaType.APPLICATION_JSON)
				// Tipo de contenido en response
				.accept(MediaType.APPLICATION_JSON)
				// Va el objeto producto que queremos crear
				.body(Mono.just(Producto.builder()
						.nombre("Mesa comedor").precio(100.00).categoria(categoria).build()), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Mesa comedor")
				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
	}

	@Test
	void creaProducto2Test() {

		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();

		client.post().uri(basePath)
				// Tipo de contenido en request
				.contentType(MediaType.APPLICATION_JSON)
				// Tipo de contenido en response
				.accept(MediaType.APPLICATION_JSON)
				// Va el objeto producto que queremos crear
				.body(Mono.just(Producto.builder()
						.nombre("Mesa comedor").precio(100.00).categoria(categoria).build()), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Producto.class)
				.consumeWith(response -> {
					Producto p = response.getResponseBody();
                    Assertions.assertFalse(p.getId().isEmpty());
					Assertions.assertEquals("Mesa comedor",p.getNombre());
					Assertions.assertEquals("Muebles",p.getCategoria().getNombre());
				});
	}

	void editarTest (){

		Producto producto = productoService.findByNombre("Sony Notebook").block();
		// Se quiere cambiar tambien la categoria
		Categoria categoria = productoService.findCategoriaByNombre("Electrónico").block();

		client.put().uri(basePath+"/{id}", Collections.singletonMap("id", producto.getId()))
				// Tipo de contenido en request
				.contentType(MediaType.APPLICATION_JSON)
				// Tipo de contenido en response
				.accept(MediaType.APPLICATION_JSON)
				// Va el objeto producto que queremos crear
				.body(Mono.just(Producto.builder()
						// Creamos el producto editado ahí mismo
						.nombre("Asus Notebook").precio(700.00).categoria(categoria).build()), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Asus Notebook")
				.jsonPath("$.precio").isEqualTo(700.00)
				.jsonPath("$.categoria.nombre").isEqualTo("Electrónico");


	}

	@Test
	void eliminarTest(){

		Producto producto = productoService.findByNombre("Mica Cómoda 5 Cajones").block();
		/* No usamos :
		* 1. .contentType(MediaType.APPLICATION_JSON) -> No enviamos un request para eliminar
		* 2. .accept(MediaType.APPLICATION_JSON) -> No esperamos un respuesta (body como tal)
		* .body(Mono.just(Object, Object.class) -> No enviamos un request para eliminar por lo tanto
		* 	no definimos el objeto y su tipo de objeto  */

		client.delete().uri(basePath+"/{id}", Collections.singletonMap("id", producto.getId()))
				.exchange()
				// Lo que devuelve por defecto el delete
				.expectStatus().isNoContent()
				// Que no se devuelva nada
				.expectBody().isEmpty();

		// Buscar por id el producto que eliminamos (NotFound)

		client.get().uri(basePath+"/{id}", Collections.singletonMap("id", producto.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody().isEmpty();
	}



}
