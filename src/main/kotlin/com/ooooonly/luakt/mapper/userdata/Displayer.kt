package com.ooooonly.luakt.mapper.userdata

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure


val KParameter.simpleInfo: String
    get() = if (name == null) type.jvmErasure.simpleName ?: ""
    else "${name}:${type.jvmErasure.simpleName}"

val KProperty<*>.simpleInfo: String
    get() = "${name}:${this.returnType.jvmErasure.simpleName}"

val KFunction<*>.simpleInfo: String
    get() = "${name}(${parameters.joinToString(separator = ",") { it.simpleInfo }}) -> ${returnType.jvmErasure.simpleName}"

