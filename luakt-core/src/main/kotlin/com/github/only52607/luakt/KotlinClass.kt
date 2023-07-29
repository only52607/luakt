package com.github.only52607.luakt

import com.github.only52607.luakt.lib.LuaKotlinLib
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import java.lang.reflect.Proxy
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.isAccessible

class KotlinClass(
    val c: KClass<*>
) : KotlinInstance(c), CoerceKotlinToLua.Coercion {
    companion object {
        private val NEW: LuaValue = valueOf("new")
        val classes: MutableMap<KClass<*>, KotlinClass> = Collections.synchronizedMap(HashMap())
        fun forKClass(c: KClass<*>): KotlinClass {
            return classes[c] ?: KotlinClass(c).also { classes[c] = it }
        }
    }

    private val innerClasses: Map<LuaValue, KClass<*>> by lazy {
        mutableMapOf<LuaValue, KClass<*>>().apply {
            val kClass = m_instance as KClass<*>
            val c = kClass.nestedClasses + kClass.sealedSubclasses
            for (i in c.indices) {
                val ci = c[i]
                val name = ci.simpleName!!
                val stub = name.substring(name.lastIndexOf('$').coerceAtLeast(name.lastIndexOf('.')) + 1)
                this[valueOf(stub)] = ci
            }
        }
    }

    private val properties: Map<LuaValue, KProperty<*>> by lazy {
        val ps: Collection<KProperty<*>> = (c.memberProperties + c.staticProperties)
        ps.associateBy {
            try {
                if (!it.isAccessible) it.isAccessible = true
            } catch (_: SecurityException) {
            } catch (_: Throwable) {
            }
            LuaValue.valueOf(it.name)
        }
    }

    private val enumConstants: Map<LuaValue, Any> by lazy {
        c.java.enumConstants?.associateBy {
            LuaValue.valueOf(it.toString())
        } ?: emptyMap()
    }

    private val functions: Map<LuaValue, LuaValue> by lazy {
        val constructors = mapOf(
            Pair(
                NEW,
                KotlinConstructor.forConstructorFunctions(c.constructors.map {
                    KotlinConstructor.forConstructorFunction(
                        it
                    )
                }.toTypedArray()) as LuaValue
            )
        )
        val fs = c.functions.groupBy { it.name }.map {
            Pair(
                LuaValue.valueOf(it.key) as LuaValue,
                KotlinFunction.forKFunctions(*it.value.toTypedArray()) as LuaValue
            )
        }.toMap()
        constructors + fs
    }

    init {
        this.kotlinClass = this
        c.companionObject?.let {
            this.companionKotlinClass = KotlinClass(it)
        }
    }

    override fun coerce(kotlinValue: Any?): LuaValue {
        return this
    }

    fun getProperty(key: LuaValue): KProperty<*>? {
        return properties[key]
    }

    fun getEnumConstants(key: LuaValue): Any? {
        return enumConstants[key]
    }

    fun getFunction(key: LuaValue): LuaValue? {
        return functions[key]
    }

    fun getInnerClass(key: LuaValue): KClass<*>? {
        return innerClasses[key]
    }

    fun getConstructor(): LuaValue? {
        return getFunction(NEW)
    }

    init {
        setmetatable(KotlinClassMetatable)
    }
}

private val KotlinClassMetatable = LuaTable().apply {
    // sugar for new instance or proxy
    set(LuaUserdata.CALL, object : VarArgFunction() {
        override fun onInvoke(args: Varargs): Varargs {
            val clazz = args.arg1() as KotlinClass
            // create interface proxy
            if (clazz.c.java.isInterface) {
                return LuaValue.userdataOf(
                    Proxy.newProxyInstance(
                        javaClass.classLoader,
                        arrayOf(clazz.c.java),
                        LuaKotlinLib.ProxyInvocationHandler(args.checkFunctionOrTable(2))
                    )
                )
            }
            // or invoke constructor
            return clazz.getConstructor()!!.invoke(args.subargs(2))
        }
    })
}

private fun Varargs.checkFunctionOrTable(index: Int): LuaValue {
    if (isfunction(index)) return checkfunction(index)
    if (istable(index)) return checktable(index)
    LuaValue.argerror(index, "function or table")
    return LuaUserdata.NIL
}