# ProyectoFinGrado_Chefguard
Este repositorio es mi proyecto Final del grado, en el subiré todo el contenido del programa y modificaciones
## Selección de Programas y Planificación
- Para el desarrollo de la app usare Android Studio ya que es un entorno que me permite trabajar en la app con kotlin y tiene emulador para poder trabajar con la app visualmente.
- Para la Base de datos, utilizaré Mysql, En esta base de datos se almacenaran todas las tablas sobre alimentos necesitará, pero también voy a necesitar triggers y puede que llegue a utilizar programas automatizados para generar las alertas y el almacenamiento de los productos.
- Para almacenar el proyecto utilizaré Git Hub, ya que ahí subire todos los cambios que vaya realizando en la aplicación.
- La IA que voy a integrar es la de OPEN IA de Chat GPT, selecciono esta por que la considero la más adecuada para preguntas cotidianas sobre comida, recetas o que pueda ayudar a seleccionar el entorno donde se van a conservar los alimentos.
## Base de datos
En el repositorio puedes encontrar chefguard.sql que es una base de datos de prueba con datos reales de los datos que se explican mas a bajo.
- ### Tablas:
    - **Alimentos**: Esta tabla se encarga de almacenar todos los alimentos que se van guardando en la aplicación.
    - **Tipo_Alimento**: Esta tabla se encarga de asociar el tipo con el alimento que es, como por ejemplo los carnicos, lácteos, etc.
    - **Ambiente**: Esta tabla almacenará el ambiente en el que se debe conservar un alimento para que perdure.
    - **Tipo_ambiente**: Esta tabla se encarga de almacenar el tipo de ambiente y va asociada a la tabla de ambiente.
    - **Proveedores**: Esta tabla se encarga de almacenar los proveedores que estan asociados a los alimentos, para tener mejor control de los productos que van llegando.
    - **Usuario**: Esta tabla almacena los datos del usuario de la aplicación, como su usuario, correo o contraseña.
    - **Registro de consumo**: Esta tabla se encarga de almacenar un registro del consumo de alimentos.
    - **Alertas**: Esta tabla se encarga de almacenar alertas que se van generando en base a la información de los alimentos.
- ### Modelo entidad relación:
    - Un tipo de alimento está asociado a muchos alimentos. 
    - Muchos alimentos están asociados a un ambiente.
    - Muchos alimentos están asociados a un proveedor.
    - Un alimento tiene muchos registros de consumo.
    - Un usuario está asociado a un registro de consumo.
    - Un alimento está asociado a muchas alertas.
    - Un tipo de ambiente está asociado a muchos ambientes.
   ![Modelo entidad relación](/imagenes/Relación%20Base%20de%20datos.png) 
- ### Procedimientos Almacenados:
    - Insertar Alimentos en la base de datos, este debe de validar previamente que el proveedor, tipo de alimento y ambiente existan previamente.
    - Insertar un usuario nuevo en la base de datos cuando se registre.
    - Insertar proveedores nuevos que no se encuentren en la base de datos.
    - Insertar el ambiente del alimento cuando se añada el alimento.
    - Insertar el tipo de alimento a la hora de añadir el alimento.
    - Actualizar el stock de los alimentos al agregar o eliminarlos, para mantener actualizados los datos.
    - Generar alertas de alimentos que están por caducar, para avisar al usuario de que los alimentos estan por caducar.
    - Generar un inventario por tipo de alimento, para asir tener mejor agrupado los datos.
    - Eliminar alimentos que ya están caducados y que no tenga stock, para evitar tener almacenados datos innecesarios.
    - Evitar cantidades negativas en el inventario, ya que ahi que registrar los alimentos de forma segura. 
- ### Triggers:
    - Un Trigger que avise con 3 días de anticipación cuando va a caducar un alimento, progresivamente hasta que el usuario consuma todo el producto:
    - Un Trigger que cuando se registre un consumo, se debe actualizar automáticamente la cantidad de alimentos.
    - Un Trigger que impida al usuario registrar un alimento cuya fecha de caducidad haya pasado.
    - Un Trigger que avise de que un producto nuevo tiene caducidad temprana y que recomiende su uso.
    - Un Trigger que registre un proveedor asignado, evita que un producto se quede sin proveedor
    - Trigger para evitar que se registren da alimentos con cantidad negativa.
    - Trigger que cambia el estado a caducado si se pacha da fecha de caducidad y agotado no disponible en función a la cantidad.
    - Trigger que cambia el estado de registro de consumo en función de consumido, parcial, desperdiciado.
    - Eliminar alimentos que ya están caducados y que no tenga stock.
    - Evitar cantidades negativas en el inventario, para ello hay que registrar de manera segura los alimentos. 
- ### Vistas:
    - Vista con detalles al completo, donde se pueda ver los datos de los alimentos más en detalle con ambiente y proveedor. 
    - Vista que realice una vista de inventario de alimentos que se encuentra disponible.
    - Vista que muestre los alimentos que están más próximos a caducar en unos intervalos de días cercanos.
    - Vista que muestre el consumo de alimentos que están siendo consumidos por los usuarios y que usuario.
    - Vista de alimentos que han sido suministrados por ciertos proveedores.
    - Vistas de alertas pendientes que no han sido gestionadas por el usuario. 
    - Vista que muestra una estadística de consumo de alimentos.
    - Vista que muestra el alimento asociado al ambiente y al tipo de ambiente.
    - Vista de usuario y registro de consumo, muestra el consumo de alimentos con fechas y cantidades.
    - Vista de alimentos desperdiciados, solo muestran los alimentos que están desperdiciados.
