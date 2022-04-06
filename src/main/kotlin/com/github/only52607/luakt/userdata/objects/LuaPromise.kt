package com.github.only52607.luakt.userdata.objects

import org.luaj.vm2.LuaValue

/**
 * ClassName: LuaPromise
 * Description:
 * date: 2022/4/5 21:20
 * @author ooooonly
 * @version
 */

class LuaPromise(
    private val receiver: (ResultReceiver) -> Unit
) : LuaValue() {
    interface ResultReceiver {
        fun resolve(value: LuaValue)
        fun reject(value: LuaValue)
    }

    data class PromiseCallback(
        val onFulfilled: LuaValue,
        val onRejected: LuaValue,
        val receiver: ResultReceiver
    )

    enum class State { Pending, Fulfilled, Rejected }

    private val callbacks = mutableListOf<PromiseCallback>()

    var state = State.Pending
    lateinit var result: LuaValue

    fun then(onFulfilled: LuaValue, onRejected: LuaValue): LuaPromise {
        return LuaPromise { receiver ->
            val callback = PromiseCallback(onFulfilled, onRejected, receiver)
            when (state) {
                State.Pending -> callbacks.add(callback)
                State.Fulfilled -> fulfillCallback(callback)
                State.Rejected -> rejectCallback(callback)
            }
        }
    }

    fun reject(reason: LuaValue) {
        state = State.Rejected
        this.result = reason
        if (reason is LuaPromise) {
            reason.callbacks.addAll(callbacks)
            return
        }
        callbacks.forEach(::rejectCallback)
    }

    fun resolve(result: LuaValue) {
        state = State.Fulfilled
        this.result = result
        if (result is LuaPromise) {
            result.callbacks.addAll(callbacks)
            return
        }
        callbacks.forEach(::fulfillCallback)
    }

    private fun fulfillCallback(callback: PromiseCallback) {
        if (callback.onFulfilled.isnil()) {
            callback.receiver.resolve(result)
            return
        }
        val nextResult = callback.onFulfilled.invoke(result).arg1()
        callback.receiver.resolve(nextResult)
    }

    private fun rejectCallback(callback: PromiseCallback) {
        val exception = callback.onRejected.invoke(result).arg1()
        callback.receiver.reject(exception)
    }

    override fun type(): Int {
        return 0x11
    }

    override fun typename(): String {
        return "Promise"
    }
}