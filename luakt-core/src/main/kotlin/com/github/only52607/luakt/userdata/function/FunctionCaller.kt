package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.objects.LuaPromise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy

/**
 * ClassName: FunctionCaller
 * Description:
 * date: 2022/2/27 13:40
 * @author ooooonly
 * @version
 */
sealed class FunctionCaller {
    object Blocking : FunctionCaller() {
        override fun callFunction(kFunction: KFunction<*>, parametersMap: Map<KParameter, Any?>): Any? {
            return if (kFunction.isSuspend) {
                runBlocking {
                    kFunction.callSuspendBy(parametersMap)
                }
            } else {
                kFunction.callBy(parametersMap)
            }
        }
    }

    class Promise(valueMapper: ValueMapper) : FunctionCaller(), CoroutineScope, ValueMapper by valueMapper {
        override fun callFunction(kFunction: KFunction<*>, parametersMap: Map<KParameter, Any?>): Any? {
            return if (kFunction.isSuspend) {
                return LuaPromise { receiver ->
                    launch {
                        try {
                            receiver.resolve(mapToLuaValue(kFunction.callSuspendBy(parametersMap)))
                        } catch (e: Exception) {
                            receiver.reject(mapToLuaValue(e))
                        }
                    }
                }
            } else {
                kFunction.callBy(parametersMap)
            }
        }

        override val coroutineContext: CoroutineContext
            get() = EmptyCoroutineContext
    }

    abstract fun callFunction(kFunction: KFunction<*>, parametersMap: Map<KParameter, Any?>): Any?
}