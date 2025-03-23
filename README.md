# ProyectoFinGrado_Chefguard
Este repositorio es mi proyecto Final del grado, en el subiré todo el contenido del programa y modificaciones
## Selección de Programas y Planificación
- Para el desarrollo de la app usare Android Studio ya que es un entorno que me permite trabajar en la app con kotlin y tiene emulador para poder trabajar con la app visualmente.
- Para la Base de datos, utilizaré Mysql, En esta base de datos se almacenaran todas las tablas sobre alimentos necesitará, pero también voy a necesitar triggers y puede que llegue a utilizar programas automatizados para generar las alertas y el almacenamiento de los productos.
- Para almacenar el proyecto utilizaré Git Hub, ya que ahí subire todos los cambios que vaya realizando en la aplicación.
- La IA que voy a integrar es la de OPEN IA de Chat GPT, selecciono esta por que la considero la más adecuada para preguntas cotidianas sobre comida, recetas o que pueda ayudar a seleccionar el entorno donde se van a conservar los alimentos.
## Base de datos
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
   ![Modelo entidad relación](/imagenes/Relación%20Base%20de%20datos.png) 
- ### Procedimientos Almacenados:
    1. Insertar Alimentos en la base de datos, este debe de validar previamente que el proveedor, tipo de alimento y ambiente existan previamente.
    2. Insertar un usuario nuevo en la base de datos cuando se registre.
    3. Insertar proveedores nuevos que no se encuentren en la base de datos.
    4. Insertar el ambiente del alimento cuando se añada el alimento.
    5. Insertar el tipo de alimento a la hora de añadir el alimento.
    6. Actualizar el stock de los alimentos al agregar o eliminarlos, para mantener actualizados los datos.
    7. Generar alertas de alimentos que están por caducar, para avisar al usuario de que los alimentos estan por caducar.
    8. Generar un inventario por tipo de alimento, para asir tener mejor agrupado los datos.
    9. Eliminar alimentos que ya están caducados y que no tenga stock, para evitar tener almacenados datos innecesarios.
    10. Evitar cantidades negativas en el inventario, ya que ahi que registrar los alimentos de forma segura. 