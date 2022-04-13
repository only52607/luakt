package com.github.only52607.luakt.lib

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.dsl.forEach
import com.github.only52607.luakt.dsl.oneArgLuaFunctionOf
import com.github.only52607.luakt.dsl.unpackVarargs
import com.github.only52607.luakt.dsl.varArgFunctionOf
import com.github.only52607.luakt.mappers.CollectionKValueMapper
import com.github.only52607.luakt.mappers.CollectionLuaValueMapper
import com.github.only52607.luakt.userdata.classes.registry.LuaKotlinClassRegistry
import com.github.only52607.luakt.userdata.objects.LuaKotlinObject
import com.github.only52607.luakt.userdata.objects.LuaKotlinProxy
import kotlinx.coroutines.CoroutineScope
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import kotlin.reflect.KClass

@Suppress("unused", "UNCHECKED_CAST")
class LuaKotlinLib(
    coroutineScope: CoroutineScope,
    valueMapper: ValueMapper,
    luaKotlinClassRegistry: LuaKotlinClassRegistry,
    private val classLoader: ClassLoader = LuaKotlinLib::class.java.classLoader
) : TwoArgFunction(),
    ValueMapper by valueMapper,
    LuaKotlinClassRegistry by luaKotlinClassRegistry,
    CoroutineScope by coroutineScope {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        return LuaTable().apply {
            val collectionKValueMapper = CollectionKValueMapper(firstKValueMapper = this@LuaKotlinLib)
            val collectionLuaValueMapper = CollectionLuaValueMapper(firstLuaValueMapper = this@LuaKotlinLib)
            this["functions"] = oneArgLuaFunctionOf { value: LuaValue ->
                if (value is LuaKotlinObject) {
                    return@oneArgLuaFunctionOf value.luaKotlinClass.getAllMemberFunctions()
                }
                return@oneArgLuaFunctionOf NIL
            }
            this["totable"] = oneArgLuaFunctionOf { value: LuaValue ->
                val kValue = collectionKValueMapper.mapToLuaValue(value.checkuserdata())
                return@oneArgLuaFunctionOf LuaKotlinObject(kValue, obtainLuaKotlinClass(kValue::class))
            }
            this["tomap"] = oneArgLuaFunctionOf { value: LuaValue ->
                val kValue = collectionLuaValueMapper.mapToKValue(
                    value.checktable(),
                    Map::class
                )
                return@oneArgLuaFunctionOf LuaKotlinObject(kValue, obtainLuaKotlinClass(kValue::class))
            }
            this["tolist"] = oneArgLuaFunctionOf { value: LuaValue ->
                val kValue = collectionLuaValueMapper.mapToKValue(
                    value.checktable(),
                    List::class
                )
                return@oneArgLuaFunctionOf LuaKotlinObject(kValue, obtainLuaKotlinClass(kValue::class))
            }
            this["toset"] = oneArgLuaFunctionOf { value: LuaValue ->
                val kValue = collectionLuaValueMapper.mapToKValue(
                    value.checktable(),
                    Set::class
                )
                return@oneArgLuaFunctionOf LuaKotlinObject(kValue, obtainLuaKotlinClass(kValue::class))
            }
            this["toarray"] = oneArgLuaFunctionOf { value: LuaValue ->
                val kValue = collectionLuaValueMapper.mapToKValue(
                    value.checktable(),
                    Array::class
                )
                return@oneArgLuaFunctionOf LuaKotlinObject(kValue, obtainLuaKotlinClass(kValue::class))
            }
            this["tobytearray"] = oneArgLuaFunctionOf { value: LuaValue ->
                val kValue = when {
                    value.isstring() -> checkjstring().encodeToByteArray()
                    value.istable() -> buildList {
                        value.forEach { _, v ->
                            this@buildList.add(v.checkint().toByte())
                        }
                    }.toByteArray()
                    else -> throw LuaError("Could not convert $value to bytearray")
                }
                return@oneArgLuaFunctionOf LuaKotlinObject(kValue, obtainLuaKotlinClass(kValue::class))
            }
            this["import"] = oneArgLuaFunctionOf { className: LuaValue ->
                return@oneArgLuaFunctionOf obtainLuaKotlinClass(classLoader.loadClass(className.checkjstring()).kotlin)
            }
            this["createProxy"] = varArgFunctionOf { varargs: Varargs ->
                val varargList = varargs.unpackVarargs()
                if (varargList.size < 2) throw LuaError("No enough parameters")
                return@varArgFunctionOf LuaKotlinProxy(
                    valueMapper = this@LuaKotlinLib,
                    luaInvocationHandler = varargList.last(),
                    interfaces = varargList.take(varargList.size - 1).map { (it.checkuserdata() as KClass<*>) },
                    loader = classLoader
                )
            }
        }.also { env?.get("package")?.get("loaded")?.set("luakotlin", it) }
    }
}
