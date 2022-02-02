package com.github.only52607.luakt.lib

import com.github.only52607.luakt.*
import com.github.only52607.luakt.userdata.*
import com.github.only52607.luakt.userdata.classes.LuaKotlinClassRegistry
import com.github.only52607.luakt.userdata.objects.LuaKotlinObject
import com.github.only52607.luakt.utils.*
import kotlinx.coroutines.CoroutineScope
import org.luaj.vm2.*
import org.luaj.vm2.lib.TwoArgFunction

@Suppress("unused", "UNCHECKED_CAST")
class LuaKotlinLib(
    private val coroutineScope: CoroutineScope,
    private val valueMapper: ValueMapper,
    private val luaKotlinClassRegistry: LuaKotlinClassRegistry
) : TwoArgFunction() {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        val globals = env?.checkglobals() ?: return NIL
        val globalsWrapper by lazy {
            LuaKotlinObject(globals, luaKotlinClassRegistry.obtainLuaKotlinClass(Globals::class))
        }
        val coroutineScopeWrapper by lazy {
            LuaKotlinObject(coroutineScope, luaKotlinClassRegistry.obtainLuaKotlinClass(CoroutineScope::class))
        }
        valueMapper.provideScope {
            globals.edit {
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
                "import" to luaFunctionOf { className: String ->
                    val clazz = Class.forName(className).kotlin
                    return@luaFunctionOf luaKotlinClassRegistry.obtainLuaKotlinClass(clazz)
                }
//                "importlib" to luaFunctionOf { className: String ->
//                    val clazz = Class.forName(className).kotlin
//                    if (!clazz.isSubclassOf(LuaFunction::class)) throw LuaError("Class is not a Lib Class.")
//                    val firstConstructor = clazz.constructors.first()
//                    val parametersList = firstConstructor.parameters
//                    val parametersMap = parametersList.associateWith {
//                        if (it.type.jvmErasure.isSubclassOf(CoroutineScope::class))
//                            coroutineScope
//                        else null
//                    }
//                    val lib = firstConstructor.callBy(parametersMap)
//                    return@luaFunctionOf globals.load(lib as LuaValue)
//                }
//                "createProxy" to varArgFunctionOf { varargs: Varargs ->
//                    val varargList = varargs.varargsToLuaValueList()
//                    if (varargList.size < 2) throw LuaError("No enough parameters.")
//                    val classes: List<Class<*>> =
//                        varargList.take(varargList.size - 1).map { (it.checkuserdata() as KClass<*>).java }
//                    val proxyTable = varargList.last().checktable()
//                    lateinit var proxyWrapper: LuaKotlinObject
//                    val proxy = Proxy.newProxyInstance(
//                        Globals::class.java.classLoader, classes.toTypedArray()
//                    ) { _, method, args ->
//                        val proxyMethod =
//                            proxyTable.getOrNull(method.name)
//                                ?: throw LuaError("No method ${method.name} found in proxy.")
//                        val returnType = method.returnType.kotlin
//                        val luaArgs = mutableListOf<LuaValue>().apply {
//                            add(proxyWrapper)
//                            args?.forEach {
//                                add(valueMapper.mapToLuaValue(it))
//                            }
//                        }
//                        return@newProxyInstance proxyMethod.invoke(varargsOf(luaArgs.toTypedArray()))
//                            .let { valueMapper.mapToKValueNullable(it.arg1(), returnType) }
//                    }
//                    proxyWrapper = LuaKotlinObject(
//                        proxy,
//                        luaKotlinClassRegistry.obtainLuaKotlinClass(proxy::class)
//                    )
//                    return@varArgFunctionOf proxyWrapper
//                }
            }
        }
        return NIL
    }
}
