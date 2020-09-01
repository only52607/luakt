package com.ooooonly.luakt.luakotlin

import com.ooooonly.luakt.asKValue
import com.ooooonly.luakt.asLuaValue
import com.ooooonly.luakt.luaFunctionOfKFunction
import com.ooooonly.luakt.luaFunctionOfKFunctions
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure


class KotlinClassInLua(val kClass: KClass<*>) {
    companion object {
        private val classes: MutableMap<KClass<*>, KotlinClassInLua> = Collections.synchronizedMap(HashMap())
        fun forKClass(c: KClass<*>): KotlinClassInLua {
            val j: KotlinClassInLua? = classes[c]
            return j ?: run {
                KotlinClassInLua(c).also {
                    classes[c] = it
                }
            }
        }
    }

    private val properties: MutableMap<String, KProperty<*>> = mutableMapOf()
    private val kFunctions: MutableMap<String, MutableList<KFunction<*>>> = mutableMapOf()
    private val luaFunctions: MutableMap<String, LuaFunction> = mutableMapOf()

    init {
        initWithClass(kClass)
        kClass.allSuperclasses.forEach(::initWithClass)
    }

    private val constructors: Collection<KFunction<*>> by lazy {
        kClass.constructors
    }

    private val luaConstructors: List<LuaFunction> by lazy {
        val result = mutableListOf<LuaFunction>()
        constructors.forEach {
            result.add(luaFunctionOfKFunction(it))
        }
        result
    }

    private fun initWithClass(kc: KClass<*>) {
        kc.declaredMemberProperties.forEach {
            properties[it.name] = it
        }
        kc.declaredMemberFunctions.forEach {
            val list = kFunctions[it.name] ?: mutableListOf()
            list.add(it)
            kFunctions[it.name] = list
        }
    }

    fun containProperty(name: String) = properties.containsKey(name)
    fun containFunction(name: String) = kFunctions.containsKey(name)

    fun setProperty(self: KotlinInstanceInLua, name: String, value: LuaValue) {
        properties[name]?.let { property ->
            if (!property.isConst) {
                (property as KMutableProperty).let { mutableProperty ->
                    val setter = mutableProperty.setter
                    setter.call(self.m_instance, value.asKValue(setter.parameters[1].type.jvmErasure))
                }
            }
        }
    }

    fun getProperty(self: KotlinInstanceInLua, name: String): LuaValue =
        properties[name]?.let { property ->
            property.getter.call(self.m_instance)?.asLuaValue() ?: LuaValue.NIL
        } ?: LuaValue.NIL

    fun getLuaFunction(name: String): LuaFunction {
        if (!luaFunctions.containsKey(name)) {
            luaFunctions[name] = luaFunctionOfKFunctions(kFunctions[name]!!)
        }
        return luaFunctions[name]!!
    }

    fun getConstructors(name: String): List<LuaFunction> = luaConstructors
    fun getProperties() = properties
}
