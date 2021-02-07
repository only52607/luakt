package com.ooooonly.luakt.mapper

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class ValueMapperChain : ValueMapper {

    companion object DEFAULT : ValueMapperChain() {
        init {
            addKValueMapperAfter(BaseKValueMapper())
            addKValueMapperAfter(CollectionKValueMapper())
            addKValueMapperAfter(UserDataKValueMapper())

            addLuaValueMapperAfter(UserDataLuaValueMapper())
            addLuaValueMapperAfter(BaseLuaValueMapper())
            addLuaValueMapperAfter(CollectionLuaValueMapper())
        }
    }

    protected val kValueMappers: LinkedList<KValueMapper> = LinkedList()
    protected val luaValueMappers: LinkedList<LuaValueMapper> = LinkedList()

    fun addKValueMapperBefore(kValueMapper: KValueMapper) = kValueMappers.addFirst(kValueMapper)

    fun addKValueMapperAfter(kValueMapper: KValueMapper) = kValueMappers.addLast(kValueMapper)

    fun addLuaValueMapperBefore(luaValueMapper: LuaValueMapper) = luaValueMappers.addFirst(luaValueMapper)

    fun addLuaValueMapperAfter(luaValueMapper: LuaValueMapper) = luaValueMappers.addLast(luaValueMapper)

    fun mapToLuaValueNullableInChain(obj: Any?): LuaValue {
        if (obj == null) return LuaValue.NIL
        return mapToLuaValue(obj, this)!!
    }

    fun mapToKValueInChain(
        luaValue: LuaValue,
        targetClass: KClass<*>
    ): Any {
        return mapToKValue(luaValue, targetClass, this)!!
    }

    override fun mapToLuaValue(obj: Any, defaultValueMapperChain: ValueMapperChain?): LuaValue? {
        if (obj is LuaValue) return obj
        var result: LuaValue?
        kValueMappers.forEach {
            result = it.mapToLuaValue(obj, defaultValueMapperChain)
            if (result != null) return result
        }
        throw KValueMapFailException("No mapper found for case ${obj::class.simpleName} to LuaValue")
    }

    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>,
        defaultValueMapperChain: ValueMapperChain?
    ): Any? {
        if (targetClass.isSubclassOf(Varargs::class)) return luaValue
        var result: Any?
        luaValueMappers.forEach {
            result = it.mapToKValue(luaValue, targetClass, defaultValueMapperChain)
            if (result != null) return result
        }
        throw LuaValueMapFailException("No mapper found for case LuaValue to ${targetClass.simpleName}")
    }
}