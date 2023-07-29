package com.github.only52607.luakt

import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

open class KotlinInstance(
    instance: Any
) : LuaUserdata(instance) {
    lateinit var kotlinClass: KotlinClass
    lateinit var companionKotlinClass: KotlinClass

    private fun prepareClass() {
        if (!::kotlinClass.isInitialized) {
            kotlinClass = KotlinClass.forKClass(m_instance::class)
            m_instance::class.companionObject?.let {
                companionKotlinClass = KotlinClass(it)
            }
        }
    }

    private fun get(key: LuaValue, kc: KotlinClass): LuaValue? {
        val p: KProperty<*>? = kc.getProperty(key)
        if (p != null) {
            return try {
                CoerceKotlinToLua.coerce(p.getter.call(m_instance))
            } catch (e: Exception) {
                throw LuaError(e)
            }
        }
        val e: Any? = kc.getEnumConstants(key)
        if (e != null) {
            return try {
                CoerceKotlinToLua.coerce(e)
            } catch (e: Exception) {
                throw LuaError(e)
            }
        }
        val m: LuaValue? = kc.getFunction(key)
        if (m != null) {
            return m
        }
        val c: KClass<*>? = kc.getInnerClass(key)
        if (c != null) {
            return if (c.isCompanion)
                CoerceKotlinToLua.coerce(c.objectInstance)
            else
                KotlinClass.forKClass(c)
        }
        return null
    }

    override fun get(key: LuaValue): LuaValue {
        prepareClass()
        val r = get(key, kotlinClass)
        if (r != null) {
            return r
        }
        if (::companionKotlinClass.isInitialized) {
            val rc = get(key, companionKotlinClass)
            if (rc != null) {
                return rc
            }
        }
        return super.get(key)
    }

    private fun set(key: LuaValue, value: LuaValue, c: KotlinClass): Boolean {
        val p: KProperty<*>? = c.getProperty(key)
        if (p != null) {
            try {
                (p as KMutableProperty<*>).setter.call(
                    m_instance,
                    CoerceLuaToKotlin.coerce(value, p.returnType)
                )
                return true
            } catch (e: Exception) {
                throw LuaError(e)
            }
        }
        return false
    }

    override fun set(key: LuaValue, value: LuaValue) {
        prepareClass()
        var success = set(key, value, kotlinClass)
        if (!success && ::companionKotlinClass.isInitialized) {
            success = set(key, value, companionKotlinClass)
        }
        if (!success) {
            super.set(key, value)
        }
    }
}