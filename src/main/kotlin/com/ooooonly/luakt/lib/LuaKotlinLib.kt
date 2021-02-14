package com.ooooonly.luakt.lib

import com.ooooonly.luakt.*
import com.ooooonly.luakt.mapper.ValueMapperChain
import com.ooooonly.luakt.mapper.userdata.KotlinClassWrapper
import com.ooooonly.luakt.mapper.userdata.KotlinInstanceWrapper
import kotlinx.coroutines.CoroutineScope
import org.luaj.vm2.*
import org.luaj.vm2.lib.TwoArgFunction
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

@Suppress("unused")
class LuaKotlinLib(private val coroutineScope: CoroutineScope, private val mapperChain: ValueMapperChain) :
    TwoArgFunction() {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        val globals = env?.checkglobals() ?: return NIL
        val globalsWrapper by lazy {
            KotlinInstanceWrapper(globals)
        }
        val coroutineScopeWrapper by lazy {
            KotlinInstanceWrapper(coroutineScope)
        }
        globals.edit {
            "internalGlobals" to luaFunctionOf {
                return@luaFunctionOf globalsWrapper
            }
            "internalCoroutineScope" to luaFunctionOf {
                return@luaFunctionOf coroutineScopeWrapper
            }
            "import" to luaFunctionOf { className: String ->
                val clazz = Class.forName(className).kotlin
                return@luaFunctionOf KotlinClassWrapper(clazz, ValueMapperChain)
            }
            "importlib" to luaFunctionOf { className: String ->
                val clazz = Class.forName(className).kotlin
                if (!clazz.isSubclassOf(LuaFunction::class)) throw LuaError("Class is not a Lib Class.")
                val firstConstructor = clazz.constructors.first()
                val parametersList = firstConstructor.parameters
                val parametersMap = parametersList.associateWith {
                    if (it.type.jvmErasure.isSubclassOf(CoroutineScope::class))
                        coroutineScope
                    else null
                }
                val lib = firstConstructor.callBy(parametersMap)
                return@luaFunctionOf globals.load(lib as LuaValue)
            }
            "createProxy" to varArgFunctionOf { varargs: Varargs ->
                val varargList = varargs.toList()
                if (varargList.size < 2) throw LuaError("No enough parameters.")
                val classes: List<Class<*>> =
                    varargList.take(varargList.size - 1).map { (it.checkuserdata() as KClass<*>).java }
                val proxyTable = varargList.last().checktable()
                lateinit var proxyWrapper: KotlinInstanceWrapper
                val proxy = Proxy.newProxyInstance(
                    Globals::class.java.classLoader, classes.toTypedArray()
                ) { _, method, args ->
                    val proxyMethod =
                        proxyTable.getOrNull(method.name) ?: throw LuaError("No method ${method.name} found in proxy.")
                    val returnType = method.returnType.kotlin
                    val luaArgs = mutableListOf<LuaValue>().apply {
                        add(proxyWrapper)
                        args?.forEach {
                            add(mapperChain.mapToLuaValueNullableInChain(it))
                        }
                    }
                    return@newProxyInstance proxyMethod.invoke(LuaValue.varargsOf(luaArgs.toTypedArray()))
                        .let { mapperChain.mapToKValueInChain(it.arg1(), returnType) }
                }
                proxyWrapper = KotlinInstanceWrapper(proxy)
                return@varArgFunctionOf proxyWrapper
            }
        }
        return NIL
    }
}
