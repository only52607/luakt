package com.github.only52607.luakt

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import java.lang.reflect.Array

class KotlinArray(instance: Any): LuaUserdata(instance) {
    private class LenFunction : OneArgFunction() {
        override fun call(u: LuaValue): LuaValue {
            return valueOf(Array.getLength((u as LuaUserdata).m_instance))
        }
    }

    init {
        setmetatable(array_metatable)
    }

    override fun get(key: LuaValue): LuaValue {
        if (key == LENGTH) return valueOf(Array.getLength(m_instance))
        if (key.isint()) {
            val i = key.toint() - 1
            if (i < 0 || i >= Array.getLength(m_instance)) {
                return NIL
            }
            return CoerceKotlinToLua.coerce(
                Array.get(
                    m_instance,
                    key.toint() - 1
                )
            )
        }
        return super.get(key)
    }

    override fun set(key: LuaValue, value: LuaValue) {
        if (key.isint()) {
            val i = key.toint() - 1
            if (i >= 0 && i < Array.getLength(m_instance)) {
                Array.set(
                    m_instance,
                    i,
                    CoerceLuaToKotlin.coerce(value, m_instance.javaClass.componentType)
                )
            } else if (m_metatable == null || !settable(this, key, value)) {
                error("array index out of bounds")
            }
        } else {
            super.set(key, value)
        }
    }

    override fun checktable(): LuaTable {
        return LuaValue.listOf(
            (0 until Array.getLength(m_instance)).map {
                CoerceKotlinToLua.coerce(
                    Array.get(
                        m_instance,
                        it
                    )
                )
            }.toTypedArray()
        )
    }

    companion object {
        val LENGTH: LuaValue = valueOf("length")
        val array_metatable: LuaTable = LuaTable().apply {
            rawset(LEN, LenFunction())
        }
    }
}

