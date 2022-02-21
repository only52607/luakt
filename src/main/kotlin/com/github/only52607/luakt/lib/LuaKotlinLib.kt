package com.github.only52607.luakt.lib

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.mappers.CollectionKValueMapper
import com.github.only52607.luakt.mappers.CollectionLuaValueMapper
import com.github.only52607.luakt.userdata.classes.LuaKotlinClassRegistry
import com.github.only52607.luakt.userdata.objects.LuaKotlinObject
import com.github.only52607.luakt.userdata.objects.LuaKotlinProxy
import com.github.only52607.luakt.utils.forEach
import com.github.only52607.luakt.utils.luaFunctionOf
import com.github.only52607.luakt.utils.unpackVarargs
import com.github.only52607.luakt.utils.varArgFunctionOf
import kotlinx.coroutines.CoroutineScope
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import kotlin.reflect.KClass

@Suppress("unused", "UNCHECKED_CAST")
class LuaKotlinLib(
    private val coroutineScope: CoroutineScope,
    valueMapper: ValueMapper,
    private val luaKotlinClassRegistry: LuaKotlinClassRegistry,
    private val classLoader: ClassLoader = LuaKotlinLib::class.java.classLoader
) : TwoArgFunction(), ValueMapper by valueMapper {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        val globals = env?.checkglobals() ?: return NIL
        val collectionKValueMapper = CollectionKValueMapper(firstKValueMapper = this)
        val collectionLuaValueMapper = CollectionLuaValueMapper(firstLuaValueMapper = this)
        with(globals) {
            this["functions"] = luaFunctionOf { value: LuaValue ->
                if (value is LuaKotlinObject) {
                    return@luaFunctionOf value.luaKotlinClass.getAllMemberFunctions()
                }
                return@luaFunctionOf NIL
            }
            this["totable"] = luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionKValueMapper.mapToLuaValue(value.checkuserdata())
            }
            this["tomap"] = luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), Map::class)
            }
            this["tolist"] = luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), List::class)
            }
            this["toset"] = luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), Set::class)
            }
            this["toarray"] = luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), Array::class)
            }
            this["tobytearray"] = luaFunctionOf { value: LuaValue ->
                if (value.isstring()) {
                    return@luaFunctionOf checkjstring().encodeToByteArray()
                }
                if (value.istable()) {
                    val list = mutableListOf<Byte>()
                    value.forEach { _, v ->
                        list.add(v.checkint().toByte())
                    }
                    return@luaFunctionOf list.toByteArray()
                }
                throw LuaError("Could not convert $value to bytearray")
            }
            this["import"] = luaFunctionOf { className: String ->
                return@luaFunctionOf luaKotlinClassRegistry.obtainLuaKotlinClass(classLoader.loadClass(className).kotlin)
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
        }
        return NIL
    }
}
