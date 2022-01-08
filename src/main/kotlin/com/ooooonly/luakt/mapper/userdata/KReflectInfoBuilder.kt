package com.ooooonly.luakt.mapper.userdata

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

interface KReflectInfoBuilder {
    fun buildKClassInfo(kClass: KClass<*>): String

    fun buildKParameterInfo(kParameter: KParameter): String

    fun buildKPropertyInfo(kProperty: KProperty<*>): String

    fun buildKFunctionInfo(kFunction: KFunction<*>): String
}

object SimpleKReflectInfoBuilder : KReflectInfoBuilder {
    override fun buildKClassInfo(kClass: KClass<*>): String {
        return "Class: ${kClass.qualifiedName}"
    }

    override fun buildKParameterInfo(kParameter: KParameter): String {
        return if (kParameter.name == null) kParameter.type.jvmErasure.simpleName ?: ""
        else "${kParameter.name}:${kParameter.type.jvmErasure.simpleName}"
    }

    override fun buildKPropertyInfo(kProperty: KProperty<*>): String {
        return "${kProperty.name}:${kProperty.returnType.jvmErasure.simpleName}"
    }

    override fun buildKFunctionInfo(kFunction: KFunction<*>): String {
        return "${kFunction.name}(${
            kFunction.parameters.joinToString(
                separator = ",",
                transform = ::buildKParameterInfo
            )
        }) -> ${kFunction.returnType.jvmErasure.simpleName}"
    }
}