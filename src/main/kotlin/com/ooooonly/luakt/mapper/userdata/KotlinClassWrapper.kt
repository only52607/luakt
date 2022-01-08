package com.ooooonly.luakt.mapper.userdata

import com.ooooonly.luakt.mapper.ValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

@Suppress("unused")
open class KotlinClassWrapper(
    private val kClass: KClass<*>,
    private val valueMapper: ValueMapper,
    private val kClassExtensionProvider: KClassExtensionProvider
) : LuaUserdata(kClass) {

    private val associatedStaticProperties: Map<String, KProperty<*>> by lazy {
        kClass.staticProperties.associateBy(KProperty<*>::name)
    }

    private val groupedStaticFunctions: Map<String, List<KFunction<*>>> by lazy {
        kClass.staticFunctions.groupBy(KFunction<*>::name)
    }

    private val overloadStaticFunctionWrappers: Map<String, KotlinOverloadFunctionWrapper> by lazy {
        groupedStaticFunctions.mapValues { KotlinOverloadFunctionWrapper(it.value, valueMapper) }
    }

    private val associatedMemberProperties: Map<String, KProperty<*>> by lazy {
        kClass.memberProperties.plus(kClassExtensionProvider.provideExtensionProperties(kClass))
            .associateBy(KProperty<*>::name)
    }

    private val groupedFunctions: Map<String, List<KFunction<*>>> by lazy {
        kClass.functions.plus(kClassExtensionProvider.provideExtensionFunctions(kClass)).groupBy(KFunction<*>::name)
    }

    private val overloadFunctionWrappers: Map<String, KotlinOverloadFunctionWrapper> by lazy {
        groupedFunctions.mapValues { KotlinOverloadFunctionWrapper(it.value, valueMapper) }
    }

    private val constructors: Collection<KFunction<*>> by lazy {
        kClass.constructors
    }

    private val constructorWrapper: KotlinOverloadFunctionWrapper by lazy {
        KotlinOverloadFunctionWrapper(constructors, valueMapper)
    }

    private val overloadConstructorWrapper: KotlinOverloadFunctionWrapper by lazy {
        KotlinOverloadFunctionWrapper(constructors.toList(), valueMapper)
    }

    fun containProperty(name: String): Boolean =
        associatedMemberProperties.containsKey(name)

    fun containFunction(name: String): Boolean =
        groupedFunctions.containsKey(name)

    fun setProperty(self: KotlinInstanceWrapper, name: String, value: LuaValue) {
        val property = associatedMemberProperties[name] ?: throw Exception("No property $name found.")
        if (property.isConst) throw Exception("Const property $name could not be set.")
        property.isAccessible = true
        val mutableProperty = property as KMutableProperty
        mutableProperty.setter.call(
            self.m_instance,
            valueMapper.mapToKValueNullable(value, property.returnType.jvmErasure)
        )
    }

    fun getProperty(self: KotlinInstanceWrapper, name: String): LuaValue {
        val property = associatedMemberProperties[name] ?: return LuaValue.NIL
        property.isAccessible = true
        val result = property.getter.call(self.m_instance) ?: return LuaValue.NIL
        return valueMapper.mapToLuaValue(result)
    }

    fun getFunctionWrapper(name: String): KotlinOverloadFunctionWrapper {
        return overloadFunctionWrappers[name] ?: throw Exception("No function $name found.")
    }

    fun getAllProperties(self: KotlinInstanceWrapper): LuaTable = LuaTable().apply {
        associatedMemberProperties.forEach { property ->
            val value = try {
                property.value.isAccessible = true
                valueMapper.mapToLuaValue(property.value.getter.call(self.m_instance))
            } catch (e: Exception) {
                LuaValue.NIL
            }
            set(property.key, value)
        }
    }

    fun getAllFunctions(): LuaTable = LuaTable.listOf(overloadFunctionWrappers.values.toTypedArray())

    fun isSubClassOf(className: String) = kClass.allSuperclasses.any { it.simpleName == className }

    override fun get(key: LuaValue?): LuaValue {
        val keyString = key?.checkjstring() ?: return NIL
        if (associatedStaticProperties.containsKey(keyString))
            return associatedStaticProperties[keyString]?.getter?.call()?.let(valueMapper::mapToLuaValue) ?: NIL
        if (overloadStaticFunctionWrappers.containsKey(keyString))
            return overloadStaticFunctionWrappers[keyString] ?: NIL
        return NIL
    }

    override fun set(key: LuaValue?, value: LuaValue) {
        val keyString = key?.checkjstring() ?: return
        if (associatedStaticProperties.containsKey(keyString)) {
            val property = associatedStaticProperties[keyString] as KMutableProperty
            val kValue = valueMapper.mapToKValueNullable(value, property.setter.parameters.first().type.jvmErasure)
            property.setter.call(kValue)
        }
    }

    override fun call(): LuaValue? = invoke(NONE).arg1()
    override fun call(arg: LuaValue?): LuaValue? = invoke(arg).arg1()
    override fun call(arg1: LuaValue?, arg2: LuaValue?): LuaValue? = invoke(varargsOf(arg1, arg2)).arg1()
    override fun call(arg1: LuaValue?, arg2: LuaValue?, arg3: LuaValue?): LuaValue? =
        invoke(varargsOf(arg1, arg2, arg3)).arg1()

    override fun invoke(args: Varargs?): Varargs = onInvoke(args).eval()
    override fun onInvoke(args: Varargs?): Varargs = constructorWrapper.invoke(args)

    override fun typename(): String = kClass.qualifiedName ?: "Unknown KClass"
    override fun tostring(): LuaValue = LuaValue.valueOf(toString())
    override fun toString(): String = m_instance.toString()
}