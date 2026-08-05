package gg.aquatic.pakket.api.nms.entity

class EntityDataValue private constructor(
    val id: Int,
    val value: Any,
    val serializerType: DataSerializerTypes.DataSerializerType<*>
) {

    companion object {
        fun <T: Any> create(id: Int, serializerType: DataSerializerTypes.DataSerializerType<T>, value: T): EntityDataValue {
            return EntityDataValue(id, value, serializerType)
        }
    }

}