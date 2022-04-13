package com.github.only52607.luakt.userdata.classes

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.function.LuaKotlinOverloadedFunction
import com.github.only52607.luakt.userdata.function.joinAsLuaKotlinOverloadedFunction
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
open class LuaKotlinClass(
    kClass: KClass<*>,
    valueMapper: ValueMapper,
    private val kClassExtensionProvider: KClassExtensionProvider
) : AbstractLuaKotlinClass(kClass), ValueMapper by valueMapper {
    companion object {
        const val ASYNC_CALL_PREFIX = "async_"
    }

    // Statics
    private val associatedStaticProperties: Map<String, KProperty<*>> by lazy {
        kClass.staticProperties.associateBy(KProperty<*>::name)
    }
    private val groupedStaticFunctions: Map<String, List<KFunction<*>>> by lazy {
        kClass.staticFunctions.groupBy(KFunction<*>::name)
    }
    private val overloadedStaticFunctionWrappersLua: Map<String, LuaKotlinOverloadedFunction> by lazy {
        groupedStaticFunctions.mapValues { it.value.joinAsLuaKotlinOverloadedFunction(this) }
    }

    // Members
    private val associatedMemberProperties: Map<String, KProperty<*>> by lazy {
        kClass.memberProperties.plus(kClassExtensionProvider.provideExtensionProperties(kClass))
            .associateBy(KProperty<*>::name)
    }
    private val groupedFunctions: Map<String, List<KFunction<*>>> by lazy {
        kClass.functions.plus(kClassExtensionProvider.provideExtensionFunctions(kClass)).groupBy(KFunction<*>::name)
    }
    private val overloadedFunctionWrappersLua: Map<String, LuaKotlinOverloadedFunction> by lazy {
        groupedFunctions.mapValues { it.value.joinAsLuaKotlinOverloadedFunction(this) }
    }

    // Constructors
    private val overloadedConstructorLua: LuaKotlinOverloadedFunction by lazy {
        kClass.constructors.joinAsLuaKotlinOverloadedFunction(this)
    }

    override fun containsMemberProperty(name: String): Boolean =
        associatedMemberProperties.containsKey(name)

    override fun containsMemberFunction(name: String): Boolean =
        groupedFunctions.containsKey(name)

    override fun containsStaticProperty(name: String): Boolean =
        associatedStaticProperties.containsKey(name)

    override fun containsStaticFunction(name: String): Boolean =
        overloadedStaticFunctionWrappersLua.containsKey(name)

    override fun setMemberProperty(self: Any, name: String, value: LuaValue) {
        val property = associatedMemberProperties[name] ?: throw Exception("No property $name found.")
        if (property.isConst) throw Exception("Const property $name could not be set.")
        property.isAccessible = true
        val mutableProperty = property as KMutableProperty
        mutableProperty.setter.call(
            self,
            mapToKValue(value, property.returnType.jvmErasure)
        )
    }

    override fun getMemberProperty(self: Any, name: String): LuaValue {
        val property = associatedMemberProperties[name] ?: return NIL
        property.isAccessible = true
        val result = property.getter.call(self) ?: return NIL
        return mapToLuaValue(result)
    }

    override fun getMemberFunction(name: String): LuaValue {
        return overloadedFunctionWrappersLua[name] ?: throw Exception("No function $name found.")
    }

    override fun setStaticProperty(name: String, value: LuaValue) {
        val property = associatedStaticProperties[name] as KMutableProperty
        val kValue = mapToKValue(value, property.setter.parameters.first().type.jvmErasure)
        property.setter.call(kValue)
    }

    override fun getStaticProperty(name: String): LuaValue =
        associatedStaticProperties[name]?.getter?.call()?.let { mapToLuaValue(it) } ?: NIL

    override fun getStaticFunction(name: String): LuaValue =
        overloadedStaticFunctionWrappersLua[name] ?: NIL

    override fun getAllMemberProperties(self: Any): LuaValue = LuaTable()
        .apply {
        associatedMemberProperties.forEach { property ->
            val value = try {
                property.value.isAccessible = true
                mapToLuaValue(property.value.getter.call(self))
            } catch (e: Exception) {
                NIL
            }
            set(property.key, value)
        }
    }

    private val allMemberFunctions by lazy { listOf(overloadedFunctionWrappersLua.values.toTypedArray()) }
    override fun getAllMemberFunctions(): LuaValue = allMemberFunctions

    override fun invokeConstructor(args: Varargs?): Varargs = overloadedConstructorLua.invoke(args)
}