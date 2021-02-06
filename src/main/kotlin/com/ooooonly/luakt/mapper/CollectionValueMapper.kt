package com.ooooonly.luakt.mapper

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class CollectionKValueMapper : KValueMapper {
    override fun mapToLuaValue(obj: Any, defaultValueMapperChain: ValueMapperChain?): LuaValue? {
        if (defaultValueMapperChain == null) throw KValueMapFailException("CollectionKValueMapper required defaultValueMapperChain.")
        return when (obj) {
            is Iterable<*> -> {
                val list = mutableListOf<LuaValue?>()
                obj.map { defaultValueMapperChain.mapToLuaValueNullableInChain(it) }.forEach {
                    list.add(it)
                }
                return LuaValue.listOf(list.toTypedArray())
            }
            is Map<*, *> -> LuaTable().apply {
                obj.forEach { (key, value) ->
                    set(
                        defaultValueMapperChain.mapToLuaValueNullableInChain(key),
                        defaultValueMapperChain.mapToLuaValueNullableInChain(value)
                    )
                }
            }
            else -> null
        }
    }
}

/**
 * A complete conversion is not yet implemented
 */

class CollectionLuaValueMapper : LuaValueMapper {
    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>,
        defaultValueMapperChain: ValueMapperChain?
    ): Any? {
        /**
         * Support only converted to HashMap<LuaValue, LuaValue>()
         */
        if (targetClass.isSubclassOf(Map::class)) {
            val map = HashMap<LuaValue, LuaValue>()
            var k = LuaValue.NIL
            while (true) {
                val n = luaValue.next(k)
                k = n.arg1()
                if (k.isnil()) break
                val v = n.arg(2)
                map[k] = v
            }
            return map
        }

        /**
         * Support only converted to MutableList<LuaValue>()
         */
        if (targetClass.isSubclassOf(Iterable::class)) return mutableListOf<LuaValue>().apply {
            val table = luaValue.checktable()
            for (i in 1..table.keyCount()) {
                add(table.rawget(i))
            }
        }
        return null
    }
}