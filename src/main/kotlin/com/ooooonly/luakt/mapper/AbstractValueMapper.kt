package com.ooooonly.luakt.mapper

import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/**
 * ClassName: AbstractValueMapper
 * Description:
 * date: 2022/1/7 18:20
 * @author ooooonly
 * @version
 */
abstract class AbstractLuaValueMapper : LuaValueMapper {
    abstract var nextLuaValueMapper: LuaValueMapper?
    abstract var firstLuaValueMapper: LuaValueMapper?

    override fun mapToKValueNullable(luaValue: LuaValue, targetClass: KClass<*>?): Any? {
        return mapToKValue(luaValue, targetClass).takeIf { it !is Unit }
    }

    protected fun nextMapToKValue(luaValue: LuaValue, targetClass: KClass<*>?): Any {
        return nextLuaValueMapper?.mapToKValue(luaValue, targetClass)
            ?: throw CouldNotMapToKValueException("Could not map LuaValue:${luaValue.tojstring()} to ${targetClass?.simpleName}")
    }

    fun appendNext(next: AbstractLuaValueMapper): AbstractLuaValueMapper {
        firstLuaValueMapper = firstLuaValueMapper ?: this
        nextLuaValueMapper = next
        next.firstLuaValueMapper = firstLuaValueMapper
        return next
    }
}

abstract class AbstractKValueMapper : KValueMapper {
    abstract var nextKValueMapper: KValueMapper?
    abstract var firstKValueMapper: KValueMapper?

    protected fun nextMapToLuaValue(obj: Any?): LuaValue {
        return nextKValueMapper?.mapToLuaValue(obj)
            ?: throw CouldNotMapToLuaValueException("Could not map $obj to LuaValue")
    }

    fun appendNext(next: AbstractKValueMapper): AbstractKValueMapper {
        firstKValueMapper = firstKValueMapper ?: this
        nextKValueMapper = next
        next.firstKValueMapper = firstKValueMapper
        return next
    }
}