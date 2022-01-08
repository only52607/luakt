package com.ooooonly.luakt.utils

import com.ooooonly.luakt.mapper.KValueMapper
import com.ooooonly.luakt.mapper.LuaValueMapper
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

inline fun <reified T> LuaValue.asKValue(): T =
    asKValue(T::class) as T

fun LuaValue.asKValue(
    targetClass: KClass<*>? = null,
    luaValueMapper: LuaValueMapper = globalValueMapper,
): Any = luaValueMapper.mapToKValue(this, targetClass)

fun Any?.asLuaValue(
    kValueMapper: KValueMapper = globalValueMapper
): LuaValue = kValueMapper.mapToLuaValue(this)