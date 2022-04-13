package com.github.only52607.luakt.userdata.objects

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.LuaKotlinUserdata
import org.luaj.vm2.LuaNil
import org.luaj.vm2.LuaValue
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

/**
 * ClassName: KotlinLuaProxy
 * Description:
 * date: 2022/2/21 14:59
 * @author ooooonly
 * @version
 */
open class LuaKotlinProxy(
    valueMapper: ValueMapper,
    private val luaInvocationHandler: LuaValue,
    interfaces: List<KClass<*>>,
    loader: ClassLoader = LuaValue::class.java.classLoader
) : LuaKotlinUserdata(), ValueMapper by valueMapper {
    init {
        instance = Proxy.newProxyInstance(loader, interfaces.map { it.java }.toTypedArray()) { _, method, args ->
            val proxyMethod =
                luaInvocationHandler.get(method.name).takeIf { it !is LuaNil }
                    ?: throw org.luaj.vm2.LuaError("No method ${method.name} found in proxy")
            val luaArgs = mutableListOf<LuaValue>().apply {
                add(this@LuaKotlinProxy)
                args?.forEach {
                    add(mapToLuaValue(it))
                }
            }
            val resultVarargs = proxyMethod.invoke(varargsOf(luaArgs.toTypedArray()))
            return@newProxyInstance mapToKValue(resultVarargs.arg1(), method.returnType.kotlin)
        }

    }

    override fun get(key: LuaValue): LuaValue = luaInvocationHandler.get(key)

    override fun set(key: LuaValue, value: LuaValue) {
        luaInvocationHandler.set(key, value)
    }

    override fun typename(): String? = "LuaKotlinProxy"
    override fun tostring(): LuaValue = LuaValue.valueOf(toString())
    override fun toString(): String = instance.toString()
}