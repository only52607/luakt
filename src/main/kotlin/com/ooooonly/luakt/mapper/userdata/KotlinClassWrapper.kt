package com.ooooonly.luakt.mapper.userdata

import com.ooooonly.luakt.mapper.ValueMapperChain
import org.luaj.vm2.LuaValue
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

class KotlinClassWrapper(
    private val kClass: KClass<*>,
    private val mapperChain: ValueMapperChain? = null
) {
    companion object {
        private val classes: MutableMap<KClass<*>, KotlinClassWrapper> = Collections.synchronizedMap(HashMap())
        private val mapperChain = ValueMapperChain.DEFAULT

        fun forKClass(kClass: KClass<*>): KotlinClassWrapper {
            val wrapper: KotlinClassWrapper? = classes[kClass]
            if (wrapper != null) return wrapper

            val newWrapper = KotlinClassWrapper(kClass, mapperChain)
            classes[kClass] = newWrapper
            return newWrapper
        }
    }

    private val superWrappers: List<KotlinClassWrapper> by lazy {
        kClass.superclasses.map { forKClass(it) }
    }

    private val properties: Map<String, KProperty<*>> by lazy {
        kClass.declaredMemberProperties.associateBy { it.name }
    }

    private val functions: Map<String, List<KFunction<*>>> by lazy {
        kClass.declaredMemberFunctions.groupBy { it.name }
    }

    private val overloadFunctionWrappers: MutableMap<String, KotlinOverloadFunctionWrapper> = mutableMapOf()

    private val constructors: Collection<KFunction<*>> by lazy {
        kClass.constructors
    }

    private val overloadConstructorWrapper: KotlinOverloadFunctionWrapper by lazy {
        KotlinOverloadFunctionWrapper(constructors.toList(), mapperChain)
    }

    fun containSelfProperty(name: String): Boolean = properties.containsKey(name)

    fun containSelfFunction(name: String): Boolean = functions.containsKey(name)

    fun containProperty(name: String): Boolean =
        properties.containsKey(name) || superWrappers.any { it.containProperty(name) }

    fun containFunction(name: String): Boolean =
        functions.containsKey(name) || superWrappers.any { it.containFunction(name) }

    fun setProperty(self: KotlinInstanceWrapper, name: String, value: LuaValue) {
        val property = properties[name]
        if (property == null) {
            superWrappers.forEach { wrapper ->
                if (wrapper.containSelfProperty(name)) return@setProperty wrapper.setProperty(self, name, value)
            }
            throw Exception("No property $name found.")
        }
        if (property.isConst) throw Exception("Const property $name could not be set.")
        property.isAccessible = true
        val mutableProperty = property as KMutableProperty
        mutableProperty.setter.call(
            self.m_instance,
            mapperChain!!.mapToKValueInChain(value, property.returnType.jvmErasure)
        )
    }

    fun getProperty(self: KotlinInstanceWrapper, name: String): LuaValue {
        val property = properties[name]
        if (property == null) {
            superWrappers.forEach { wrapper ->
                if (wrapper.containSelfProperty(name)) return@getProperty wrapper.getProperty(self, name)
            }
            return LuaValue.NIL
        }
        property.isAccessible = true
        val result = property.getter.call(self.m_instance) ?: return LuaValue.NIL
        return mapperChain!!.mapToLuaValueNullableInChain(result)
    }

    fun getFunctionWrapper(name: String): KotlinOverloadFunctionWrapper {
        val function = functions[name]
        if (function == null) {
            superWrappers.forEach { wrapper ->
                if (wrapper.containSelfFunction(name)) return@getFunctionWrapper wrapper.getFunctionWrapper(name)
            }
            throw Exception("No function $name found.")
        }
        if (!overloadFunctionWrappers.containsKey(name)) {
            val wrapper = KotlinOverloadFunctionWrapper(function, mapperChain)
            overloadFunctionWrappers[name] = wrapper
            return wrapper
        }
        return overloadFunctionWrappers[name]!!
    }
}
