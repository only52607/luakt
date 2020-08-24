package com.ooooonly.luakt

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

fun luaFunctionOfKFunction(kFunction: KFunction<*>) = object : VarArgFunction() {
    lateinit var parameters: List<KParameter>

    init {
        parameters = kFunction.parameters
    }

    override fun onInvoke(args: Varargs?): Varargs = args?.let {
        val parametersMap = HashMap<KParameter, Any>()
        var i = 0
        parameters.forEach {
            parametersMap.put(it, args[i++].asKValue(it.type.jvmErasure))
        }
        kFunction.callBy(parametersMap)?.asVarargs() ?: LuaValue.NIL
    } ?: LuaValue.NIL
}

fun luaFunctionOfLambda(function: Function<*>) =
    luaFunctionOfKFunction(function.reflect()!!)

fun luaFunctionOf(block: () -> Any) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        return block().asVarargs()
    }
}

inline fun <reified T0> luaFunctionOf(crossinline block: (T0) -> Any) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs = when (T0::class) {
        Varargs::class -> args?.let {
            block(args as T0).asVarargs()
        }?: LuaValue.NIL
        else -> args?.let {
            block(args[0].asKValue()).asVarargs()
        }?: LuaValue.NIL
    }
}

inline fun <reified T0,reified T1> luaFunctionOf(crossinline block:(T0,T1) -> Any) = object: VarArgFunction(){
    override fun onInvoke(args: Varargs?): Varargs =
        args?.let {
            block(args[0].asKValue(),args[1].asKValue()).asVarargs()
        }?: LuaValue.NIL
}

inline fun <reified T0,reified T1,reified T2> luaFunctionOf(crossinline block:(T0,T1,T2) -> Any) = object: VarArgFunction(){
    override fun onInvoke(args: Varargs?): Varargs =
        args?.let {
            block(args[0].asKValue(),args[1].asKValue(),args[2].asKValue()).asVarargs()
        }?: LuaValue.NIL
}

inline fun <reified T0,reified T1,reified T2,reified T3> luaFunctionOf(crossinline block:(T0,T1,T2,T3) -> Any) = object: VarArgFunction(){
    override fun onInvoke(args: Varargs?): Varargs =
        args?.let {
            block(args[0].asKValue(),args[1].asKValue(),args[2].asKValue(),args[3].asKValue()).asVarargs()
        }?: LuaValue.NIL
}

inline fun <reified T0,reified T1,reified T2,reified T3,reified T4> luaFunctionOf(crossinline block:(T0,T1,T2,T3,T4) -> Any) = object: VarArgFunction(){
    override fun onInvoke(args: Varargs?): Varargs =
        args?.let {
            block(args[0].asKValue(),args[1].asKValue(),args[2].asKValue(),args[3].asKValue(),args[4].asKValue()).asVarargs()
        }?: LuaValue.NIL
}

inline fun <
        reified T0,
        reified T1,
        reified T2,
        reified T3,
        reified T4,
        reified T5
        > luaFunctionOf(crossinline block:(T0,T1,T2,T3,T4,T5) -> Any) = object: VarArgFunction(){
    override fun onInvoke(args: Varargs?): Varargs =
        args?.let {
            block(args[0].asKValue(),
                args[1].asKValue(),
                args[2].asKValue(),
                args[3].asKValue(),
                args[4].asKValue(),
                args[5].asKValue()
            ).asVarargs()
        }?: LuaValue.NIL
}

inline fun <
        reified T0,
        reified T1,
        reified T2,
        reified T3,
        reified T4,
        reified T5,
        reified T6
        > luaFunctionOf(crossinline block:(T0,T1,T2,T3,T4,T5,T6) -> Any) = object: VarArgFunction(){
    override fun onInvoke(args: Varargs?): Varargs =
        args?.let {
            block(args[0].asKValue(),
                args[1].asKValue(),
                args[2].asKValue(),
                args[3].asKValue(),
                args[4].asKValue(),
                args[5].asKValue(),
                args[6].asKValue()
            ).asVarargs()
        }?: LuaValue.NIL
}