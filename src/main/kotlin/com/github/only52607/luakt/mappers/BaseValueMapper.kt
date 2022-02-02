package com.github.only52607.luakt.mappers

import com.github.only52607.luakt.CouldNotMapToKValueException
import com.github.only52607.luakt.KValueMapper
import com.github.only52607.luakt.LuaValueMapper
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class BaseKValueMapper(
    override var nextKValueMapper: KValueMapper? = null,
    override var firstKValueMapper: KValueMapper? = null
) : AbstractKValueMapper() {
    override fun mapToLuaValue(obj: Any?): LuaValue {
        obj ?: return LuaValue.NIL
        return when (obj) {
            is LuaValue -> obj
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
            else -> nextMapToLuaValue(obj)
        }
    }
}

class BaseLuaValueMapper(
    override var nextLuaValueMapper: LuaValueMapper? = null,
    override var firstLuaValueMapper: LuaValueMapper? = null
) : AbstractLuaValueMapper() {
    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>?
    ): Any {
        targetClass ?: return when {
            luaValue.isstring() -> luaValue.tojstring()
            luaValue.isboolean() -> luaValue.toboolean()
            luaValue.isnumber() -> luaValue.todouble()
            luaValue.isnil() -> Unit
            else -> nextMapToKValue(luaValue, targetClass)
        }

        if (targetClass.isSuperclassOf(luaValue::class)) return luaValue

        return when (targetClass) {
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
            Unit::class -> Unit
            Void::class -> Unit
            ByteArray::class -> if (luaValue.isstring()) luaValue.checkjstring()
                .toByteArray() else throw CouldNotMapToKValueException("When the target is a ByteArray, the argument must be a String")
            else -> nextMapToKValue(luaValue, targetClass)
        }
    }
}