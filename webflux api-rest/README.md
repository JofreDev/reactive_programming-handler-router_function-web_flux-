&#160;
<img src="https://github.com/JofreDev/reactive_programming-handler-router_function-web_flux-/assets/117787626/64193e6b-517a-4319-86eb-d92eb0e11261" alt="webflux api rest (1)" width="10000" style="border-radius: 100px;"/>
&#160;&#160;

> [!NOTE]
>
> ## Microservice  **webflux api-rest**
> ### Models
> * > ## services :
>   > - **ProductoService:** Interfaz que define el contrato del servicio de producto, estableciendo las operaciones relacionadas con la base de datos.
>   > - **ProductoServiceImpl:** Implementación del servicio de producto, utilizando `CategoriaDao` y `ProductoDao` para realizar las operaciones en la base de datos.
> * > ## dao :
>   > - **CategoriaDao:** DAO reactivo para la entidad `Categoria`.
>   > - **ProductoDao:** DAO reactivo para la entidad `Producto`.
> * > ## documents :
>   > - **Categoria:** Representa las categorías de productos en un supermercado.
>   > - **Producto:** Representa los productos del supermercado.
> ### Handler
> * > **ProductoHandler:** Maneja las solicitudes HTTP relacionadas con los productos y delega la lógica de negocio al servicio correspondiente.
> ### App
> * > **RouterFunctionConfig:** Define las rutas HTTP y las asocia con los métodos del handler correspondiente.


> [!IMPORTANT] 
> ## Key concepts and notes 

