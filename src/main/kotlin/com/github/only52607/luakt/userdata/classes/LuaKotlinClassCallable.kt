package com.github.only52607.luakt.userdata.classes

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.KClassExtensionProvider
import com.github.only52607.luakt.userdata.function.LuaKotlinOverloadedFunction
import org.luaj.vm2.LuaTable
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
open class LuaKotlinClassCallable(
    kClass: KClass<*>,
    private val valueMapper: ValueMapper,
    private val kClassExtensionProvider: KClassExtensionProvider
) : LuaKotlinClass(kClass) {

    private val associatedStaticProperties: Map<String, KProperty<*>> by lazy {
        kClass.staticProperties.associateBy(KProperty<*>::name)
    }

    private val groupedStaticFunctions: Map<String, List<KFunction<*>>> by lazy {
        kClass.staticFunctions.groupBy(KFunction<*>::name)
    }

    private val overloadedStaticFunctionWrappersLua: Map<String, LuaKotlinOverloadedFunction> by lazy {
        groupedStaticFunctions.mapValues { LuaKotlinOverloadedFunction(it.value, valueMapper) }
    }

    private val associatedMemberProperties: Map<String, KProperty<*>> by lazy {
        kClass.memberProperties.plus(kClassExtensionProvider.provideExtensionProperties(kClass))
            .associateBy(KProperty<*>::name)
    }

    private val groupedFunctions: Map<String, List<KFunction<*>>> by lazy {
        kClass.functions.plus(kClassExtensionProvider.provideExtensionFunctions(kClass)).groupBy(KFunction<*>::name)
    }

    private val overloadedFunctionWrappersLua: Map<String, LuaKotlinOverloadedFunction> by lazy {
        groupedFunctions.mapValues { LuaKotlinOverloadedFunction(it.value, valueMapper) }
    }

    private val constructors: Collection<KFunction<*>> by lazy {
        kClass.constructors
    }

    private val constructorLua: LuaKotlinOverloadedFunction by lazy {
        LuaKotlinOverloadedFunction(constructors, valueMapper)
    }

    private val overloadedConstructorLua: LuaKotlinOverloadedFunction by lazy {
        LuaKotlinOverloadedFunction(constructors.toList(), valueMapper)
    }

    override fun containsProperty(name: String): Boolean =
        associatedMemberProperties.containsKey(name)

    override fun containsFunction(name: String): Boolean =
        groupedFunctions.containsKey(name)

    override fun setProperty(self: Any, name: String, value: LuaValue) {
        val property = associatedMemberProperties[name] ?: throw Exception("No property $name found.")
        if (property.isConst) throw Exception("Const property $name could not be set.")
        property.isAccessible = true
        val mutableProperty = property as KMutableProperty
        mutableProperty.setter.call(
            self,
            valueMapper.mapToKValueNullable(value, property.returnType.jvmErasure)
        )
    }

    override fun getProperty(self: Any, name: String): LuaValue {
        val property = associatedMemberProperties[name] ?: return LuaValue.NIL
        property.isAccessible = true
        val result = property.getter.call(self) ?: return LuaValue.NIL
        return valueMapper.mapToLuaValue(result)
    }

    override fun getAllProperties(self: Any): LuaTable = LuaTable().apply {
        associatedMemberProperties.forEach { property ->
            val value = try {
                property.value.isAccessible = true
                valueMapper.mapToLuaValue(property.value.getter.call(self))
            } catch (e: Exception) {
                LuaValue.NIL
            }
            set(property.key, value)
        }
    }

    override fun getAllFunctions(): LuaTable = LuaTable.listOf(overloadedFunctionWrappersLua.values.toTypedArray())

    override fun getFunction(name: String): LuaValue {
        return overloadedFunctionWrappersLua[name] ?: throw Exception("No function $name found.")
    }

    override fun get(key: LuaValue?): LuaValue {
        val keyString = key?.checkjstring() ?: return NIL
        if (associatedStaticProperties.containsKey(keyString))
            return associatedStaticProperties[keyString]?.getter?.call()?.let(valueMapper::mapToLuaValue) ?: NIL
        if (overloadedStaticFunctionWrappersLua.containsKey(keyString))
            return overloadedStaticFunctionWrappersLua[keyString] ?: NIL
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
    override fun onInvoke(args: Varargs?): Varargs = constructorLua.invoke(args)

    override fun typename(): String = kClass.qualifiedName ?: "Unknown KClass"
    override fun tostring(): LuaValue = LuaValue.valueOf(toString())
    override fun toString(): String = m_instance.toString()
}