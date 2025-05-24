package data.local.entity

data class Alimento(
    val nombre: String,
    val cantidad: Int,
    val fechaCaducidad: String,
    val fechaConsumo: String?,
    val lote: String,
    val estado: String,
    val proveedor: String,
    val tipoAlimento: String,
    val ambiente: String
)