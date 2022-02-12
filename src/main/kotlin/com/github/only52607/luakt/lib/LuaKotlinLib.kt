package com.github.only52607.luakt.lib

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.mappers.CollectionKValueMapper
import com.github.only52607.luakt.mappers.CollectionLuaValueMapper
import com.github.only52607.luakt.userdata.classes.LuaKotlinClassRegistry
import com.github.only52607.luakt.userdata.objects.LuaKotlinObject
import com.github.only52607.luakt.utils.*
import kotlinx.coroutines.CoroutineScope
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

@Suppress("unused", "UNCHECKED_CAST")
class LuaKotlinLib(
    private val coroutineScope: CoroutineScope,
    valueMapper: ValueMapper,
    private val luaKotlinClassRegistry: LuaKotlinClassRegistry
) : TwoArgFunction(), ValueMapper by valueMapper {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        val globals = env?.checkglobals() ?: return NIL
        val globalsWrapper by lazy {
            LuaKotlinObject(
                globals,
                luaKotlinClassRegistry.obtainLuaKotlinClass(Globals::class)
            )
        }
        val coroutineScopeWrapper by lazy {
            LuaKotlinObject(
                coroutineScope,
                luaKotlinClassRegistry.obtainLuaKotlinClass(CoroutineScope::class)
            )
        }
        val collectionKValueMapper = CollectionKValueMapper(firstKValueMapper = this)
        val collectionLuaValueMapper = CollectionLuaValueMapper(firstLuaValueMapper = this)
        with(globals) {
            "functions" to luaFunctionOf { value: LuaValue ->
                if (value is LuaKotlinObject) {
                    return@luaFunctionOf value.luaKotlinClass.getAllMemberFunctions()
                }
                return@luaFunctionOf NIL
            }
            "currentGlobals" to luaFunctionOf {
                return@luaFunctionOf globalsWrapper
            }
            "currentCoroutineScope" to luaFunctionOf {
                return@luaFunctionOf coroutineScopeWrapper
            }
            "totable" to luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionKValueMapper.mapToLuaValue(value.checkuserdata())
            }
            "tomap" to luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), Map::class)
            }
            "tolist" to luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), List::class)
            }
            "toset" to luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), Set::class)
            }
            "toarray" to luaFunctionOf { value: LuaValue ->
                return@luaFunctionOf collectionLuaValueMapper.mapToKValue(value.checktable(), Array::class)
            }
            "import" to luaFunctionOf { className: String ->
                val clazz = Class.forName(className).kotlin
                return@luaFunctionOf luaKotlinClassRegistry.obtainLuaKotlinClass(clazz)
            }
            "createProxy" to varArgFunctionOf { varargs: Varargs ->
                val varargList = varargs.unpackVarargs()
                if (varargList.size < 2) throw LuaError("No enough parameters.")
                val classes: List<Class<*>> =
                    varargList.take(varargList.size - 1).map { (it.checkuserdata() as KClass<*>).java }
                val proxyTable = varargList.last().checktable()
                lateinit var proxyWrapper: LuaKotlinObject
                val proxy = Proxy.newProxyInstance(
                    Globals::class.java.classLoader, classes.toTypedArray()
                ) { _, method, args ->
                    val proxyMethod =
                        proxyTable.getOrNull(method.name)
                            ?: throw LuaError("No method ${method.name} found in proxy.")
                    val returnType = method.returnType.kotlin
                    val luaArgs = mutableListOf<LuaValue>().apply {
                        add(proxyWrapper)
                        args?.forEach {
                            add(mapToLuaValue(it))
                        }
                    }
                    return@newProxyInstance mapToKValueNullable(
                        proxyMethod.invoke(varargsOf(luaArgs.toTypedArray())).arg1(), returnType
                    )
                }
                proxyWrapper = LuaKotlinObject(
                    proxy,
                    luaKotlinClassRegistry.obtainLuaKotlinClass(proxy::class)
                )
                return@varArgFunctionOf proxyWrapper
            }
        }

        return NIL
    }
}
