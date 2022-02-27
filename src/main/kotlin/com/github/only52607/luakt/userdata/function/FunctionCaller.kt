package com.github.only52607.luakt.userdata.function

import kotlinx.coroutines.runBlocking
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
    object BLOCKING : FunctionCaller() {
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

    abstract fun callFunction(kFunction: KFunction<*>, parametersMap: Map<KParameter, Any?>): Any?
}