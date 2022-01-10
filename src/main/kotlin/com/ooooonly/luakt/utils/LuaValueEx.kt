package com.ooooonly.luakt.utils

import com.ooooonly.luakt.mapper.KValueMapper
import com.ooooonly.luakt.mapper.LuaValueMapper
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

inline fun <reified T> LuaValue.asKValue(luaValueMapper: LuaValueMapper): T =
    asKValue(T::class, luaValueMapper) as T

fun LuaValue.asKValue(
    targetClass: KClass<*>? = null,
    luaValueMapper: LuaValueMapper,
): Any = luaValueMapper.mapToKValue(this, targetClass)

fun Any?.asLuaValue(
    kValueMapper: KValueMapper
): LuaValue = kValueMapper.mapToLuaValue(this)