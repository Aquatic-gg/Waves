package gg.aquatic.pakket.api

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object ReflectionUtils {

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setField(instance: Any, value: Any) {
        setField(instance.javaClass, instance, value)
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setStaticField(target: Class<*>, value: Any) {
        setField(target, null, value)
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setField(fieldName: String, instance: Any, value: Any) {
        setField(fieldName, instance.javaClass, instance, value)
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setStaticField(fieldName: String, target: Class<*>, value: Any) {
        setField(fieldName, target, null, value)
    }

    @Throws(IllegalAccessException::class, NoSuchFieldException::class)
    fun setField(targetClass: Class<*>, targetInstance: Any?, value: Any) {
        val field = getField(targetClass, value.javaClass)
        setField(field, targetInstance, value)
    }

    @Throws(IllegalAccessException::class, NoSuchFieldException::class)
    fun setField(fieldName: String, targetClass: Class<*>, targetInstance: Any?, value: Any) {
        val field = targetClass.getDeclaredField(fieldName)
        setField(field, targetInstance, value)
    }

    @Throws(IllegalAccessException::class)
    private fun setField(field: Field, targetInstance: Any?, value: Any) {
        field.isAccessible = true
        field.set(targetInstance, value)
    }

    @Throws(NoSuchFieldException::class)
    fun getField(targetClass: Class<*>, fieldClass: Class<*>): Field {
        val fields = targetClass.declaredFields.filter {
            it.type == fieldClass
        }
        if (fields.size != 1) throw NoSuchFieldException("Fields found: $fields")
        fields[0].isAccessible = true
        return fields[0]
    }

    @Throws(NoSuchFieldException::class)
    fun getField(fieldName: String, targetClass: Class<*>): Field {
        val field = targetClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    inline fun <reified U> get(targetClass: Class<*>, targetInstance: Any?): U {
        val field = getField(targetClass, U::class.java)
        return field.get(targetInstance) as U
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    inline fun <reified U> get(fieldName: String, targetClass: Class<*>, targetInstance: Any?): U {
        val field = getField(fieldName, targetClass)
        return field.get(targetInstance) as U
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    inline fun <reified U> getStatic(targetClass: Class<*>): U {
        return get(targetClass, null)
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    inline fun <reified U> getStatic(fieldName: String, targetClass: Class<*>): U {
        return get(fieldName, targetClass, null)
    }

    @Throws(InvocationTargetException::class, NoSuchMethodException::class, IllegalAccessException::class)
    fun invoke(targetInstance: Any, vararg parameters: Any) {
        invoke(targetInstance, Unit::class.javaObjectType, *parameters)
    }

    @Throws(InvocationTargetException::class, NoSuchMethodException::class, IllegalAccessException::class)
    fun invokeStatic(target: Class<*>, vararg parameters: Any) {
        invokeStatic(target, Unit::class.javaObjectType, *parameters)
    }

    @Throws(InvocationTargetException::class, NoSuchMethodException::class, IllegalAccessException::class)
    inline fun <reified U> invoke(targetInstance: Any, returnType: Class<U>, vararg parameters: Any): U {
        return invoke(targetInstance.javaClass, targetInstance, returnType, *parameters) as U
    }

    @Throws(InvocationTargetException::class, NoSuchMethodException::class, IllegalAccessException::class)
    inline fun <reified U> invokeStatic(target: Class<*>, returnType: Class<U>, vararg parameters: Any): U {
        return invoke(target, null, returnType, *parameters)
    }

    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    inline fun <reified U> invoke(targetClass: Class<*>, targetInstance: Any?, returnType: Class<U>, vararg parameters: Any): U {
        val method = getMethod(targetClass, returnType, *parameters.map { it.javaClass }.toTypedArray())
        return method.invoke(targetInstance, *parameters) as U
    }

    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    fun invoke(methodName: String, targetClass: Class<*>, targetInstance: Any?, vararg parameters: Any): Any {
        val method = targetClass.getDeclaredMethod(methodName, *parameters.map { it.javaClass }.toTypedArray())
        method.isAccessible = true
        return method.invoke(targetInstance, *parameters)
    }

    @Throws(NoSuchMethodException::class)
    fun getMethod(targetClass: Class<*>, returnType: Class<*>, vararg paramTypes: Class<*>): Method {
        val methods = targetClass.declaredMethods.filter { m ->
            m.parameterTypes.size == paramTypes.size &&
                    m.returnType == returnType &&
                    paramTypes.indices.all { i -> m.parameterTypes[i].isAssignableFrom(paramTypes[i]) }
        }
        if (methods.size != 1) throw NoSuchMethodException("Methods found: $methods")
        methods[0].isAccessible = true
        return methods[0]
    }

    @Throws(NoSuchMethodException::class)
    fun getMethod(methodName: String, targetClass: Class<*>, vararg paramTypes: Class<*>): Method {
        val method = targetClass.getDeclaredMethod(methodName, *paramTypes)
        method.isAccessible = true
        return method
    }

}