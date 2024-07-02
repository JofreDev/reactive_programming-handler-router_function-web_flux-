&#160;
<img src="https://github.com/JofreDev/reactive_programming-handler-router_function-web_flux-/assets/117787626/64193e6b-517a-4319-86eb-d92eb0e11261" alt="webflux api rest (1)" width="10000" style="border-radius: 100px;"/>
&#160;&#160;

> [!NOTE]
>
> ## Microservice  **webflux api-rest**
> ### Models
> * > ## services :
>   > - **ProductoService:** Interface that defines the contract for the product service, establishing the operations related to the database.
>   > - **ProductoServiceImpl:** Implementation of the product service, utilizing `CategoriaDao` and `ProductoDao` to perform database operations.
> * > ## dao :
>   > - **CategoriaDao:** Reactive DAO for the `Categoria` entity.
>   > - **ProductoDao:** Reactive DAO for the `Producto` entity.
> * > ## documents :
>   > - **Categoria:** Represents the product categories in a supermarket.
>   > - **Producto:** Represents the products in a supermarket.
> ### Handler
> * > **ProductoHandler:** Handles HTTP requests related to products and delegates business logic to the corresponding service.
> ### App
> * > **RouterFunctionConfig:** Defines the HTTP routes and associates them with the methods of the designated handler.


> [!IMPORTANT] 
> ## Key concepts and notes 

