package com.github.only52607.luakt.mappers

import com.github.only52607.luakt.CouldNotMapToLuaValueException
import com.github.only52607.luakt.KValueMapper
import com.github.only52607.luakt.LuaValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class CollectionKValueMapper(
    override var nextKValueMapper: KValueMapper? = null,
    override var firstKValueMapper: KValueMapper? = null
) : AbstractKValueMapper() {
    override fun mapToLuaValue(obj: Any?): LuaValue {
        val firstMapper = firstKValueMapper
            ?: throw CouldNotMapToLuaValueException("CollectionKValueMapper must contain the fistKValueMapper")
        return when (obj) {
            is Iterable<*> -> {
                val list = mutableListOf<LuaValue>()
                obj.map(firstMapper::mapToLuaValue).forEach(list::add)
                return LuaValue.listOf(list.toTypedArray())
            }
            is Map<*, *> -> LuaTable().apply {
                obj.forEach { (key, value) ->
                    set(
                        firstMapper.mapToLuaValue(key),
                        firstMapper.mapToLuaValue(value)
                    )
                }
            }
            else -> nextKValueMapper?.mapToLuaValue(obj)
                ?: throw CouldNotMapToLuaValueException("Could not map $obj to LuaValue")
        }
    }
}

/**
 * A complete conversion is not yet implemented
 */

class CollectionLuaValueMapper(
    override var nextLuaValueMapper: LuaValueMapper? = null,
    override var firstLuaValueMapper: LuaValueMapper? = null
) : AbstractLuaValueMapper() {
    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>?
    ): Any {
        if (targetClass == null) {
            return if (luaValue.istable()) {
                mutableMapOf<Any, Any>().addFromLuaValue(luaValue)
            } else {
                nextMapToKValue(luaValue, null)
            }
        }

        when {
            targetClass.isSuperclassOf(MutableMap::class) -> return mutableMapOf<Any, Any>().addFromLuaValue(luaValue)
            targetClass.isSuperclassOf(MutableList::class) -> return mutableListOf<Any>().addFromLuaValue(luaValue)
            targetClass.isSuperclassOf(MutableSet::class) -> return mutableSetOf<Any>().addFromLuaValue(luaValue)
            targetClass.isSuperclassOf(Array::class) -> return mutableListOf<Any>().addFromLuaValue(luaValue)
        }

        return nextMapToKValue(luaValue, targetClass).let {
            if (targetClass.isSuperclassOf(Array::class)) (it as List<*>).toTypedArray()
            else it
        }
    }

    private fun MutableCollection<Any>.addFromLuaValue(luaValue: LuaValue) = apply {
        val firstMapper = firstLuaValueMapper
            ?: throw CouldNotMapToLuaValueException("CollectionLuaValueMapper must contain the firstLuaValueMapper")
        val table = luaValue.checktable()
        for (i in 1..table.keyCount()) {
            add(firstMapper.mapToKValue(table.rawget(i), null))
        }
    }

    private fun MutableMap<Any, Any>.addFromLuaValue(luaValue: LuaValue) = apply {
        val firstMapper = firstLuaValueMapper
            ?: throw CouldNotMapToLuaValueException("CollectionLuaValueMapper must contain the firstLuaValueMapper")
        var k = LuaValue.NIL
        while (true) {
            val n = luaValue.next(k)
            k = n.arg1()
            if (k.isnil()) break
            val v = n.arg(2)
            this[firstMapper.mapToKValue(k, null)] = firstMapper.mapToKValue(v, null)
        }
    }
}
