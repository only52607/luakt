package com.ooooonly.luakt.mapper

import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

class BaseKValueMapper : KValueMapper {
    override fun mapToLuaValue(obj: Any, defaultValueMapperChain: ValueMapperChain?): LuaValue? {
        return when (obj) {
            is Byte -> LuaValue.valueOf(obj.toInt())
            is Short -> LuaValue.valueOf(obj.toDouble())
            is Char -> LuaValue.valueOf(obj.toString())
            is String -> LuaValue.valueOf(obj)
            is Int -> LuaValue.valueOf(obj)
            is Double -> LuaValue.valueOf(obj)
            is Boolean -> LuaValue.valueOf(obj)
            is Unit -> LuaValue.NIL
            is Void -> LuaValue.NIL
            is Float -> LuaValue.valueOf(obj.toDouble())
            is Long -> LuaValue.valueOf(obj.toDouble())
            is ByteArray -> LuaValue.valueOf(String(obj))
            else -> null
        }
    }
}

class BaseLuaValueMapper : LuaValueMapper {
    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>,
        defaultValueMapperChain: ValueMapperChain?
    ): Any? {
        return try {
            when (targetClass) {
                Byte::class -> luaValue.toint()
                Char::class -> luaValue.tojstring()
                String::class -> luaValue.tojstring()
                Int::class -> if (luaValue.isstring()) luaValue.checkjstring().toInt() else luaValue.checkint()
                Long::class -> if (luaValue.isstring()) luaValue.checkjstring().toLong() else luaValue.checklong()
                Boolean::class -> if (luaValue.isstring()) luaValue.checkjstring()!!
                    .toBoolean() else luaValue.checkboolean()
                Float::class -> if (luaValue.isstring()) luaValue.checkjstring().toFloat() else luaValue.checkdouble()
                    .toFloat()
                Double::class -> if (luaValue.isstring()) luaValue.checkjstring().toDouble() else luaValue.checkdouble()
                Unit::class -> LuaValue.NIL
                Void::class -> LuaValue.NIL
                ByteArray::class -> if (luaValue.isstring()) luaValue.checkjstring()
                    .toByteArray() else throw LuaValueMapFailException("When the target is a ByteArray, the argument must be a String")
                else -> null
            }
        } catch (e: Exception) {
            if (e is LuaValueMapFailException) throw e
            throw LuaValueMapFailException("Map failed when case LuaValue:${luaValue.tojstring()} to ${targetClass.simpleName}:${e.message}")
        }
    }
}