package com.ooooonly.luakt

import com.ooooonly.luakt.mapper.ValueMapperChain
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

private val mapperChain = ValueMapperChain.DEFAULT

inline fun <reified T> LuaValue.asKValue(): T =
    asKValue(T::class) as T

fun LuaValue.asKValue(targetClass: KClass<*>): Any = mapperChain.mapToKValueInChain(this, targetClass)

fun Any?.asLuaValue(): LuaValue = mapperChain.mapToLuaValueNullableInChain(this)