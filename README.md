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