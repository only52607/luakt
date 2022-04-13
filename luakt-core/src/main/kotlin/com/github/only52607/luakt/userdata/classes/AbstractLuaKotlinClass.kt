package com.github.only52607.luakt.userdata.classes

import com.github.only52607.luakt.userdata.LuaKotlinUserdata
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import kotlin.reflect.KClass

/**
 * ClassName: AbstractLuaKotlinClass
 * Description:
 * date: 2022/1/8 20:44
 * @author ooooonly
 * @version
 */
abstract class AbstractLuaKotlinClass(
    val kClass: KClass<*>
) : LuaKotlinUserdata(kClass) {
    abstract fun containsMemberProperty(name: String): Boolean
    abstract fun containsMemberFunction(name: String): Boolean

    abstract fun containsStaticProperty(name: String): Boolean
    abstract fun containsStaticFunction(name: String): Boolean

    abstract fun invokeConstructor(args: Varargs?): Varargs

    abstract fun setMemberProperty(self: Any, name: String, value: LuaValue)
    abstract fun getMemberProperty(self: Any, name: String): LuaValue
    abstract fun getMemberFunction(name: String): LuaValue

    abstract fun setStaticProperty(name: String, value: LuaValue)
    abstract fun getStaticProperty(name: String): LuaValue
    abstract fun getStaticFunction(name: String): LuaValue

    abstract fun getAllMemberProperties(self: Any): LuaValue
    abstract fun getAllMemberFunctions(): LuaValue

    override fun get(key: LuaValue?): LuaValue {
        val keyString = key?.checkjstring() ?: return NIL
        if (containsStaticProperty(keyString))
            return getStaticProperty(keyString)
        if (containsStaticFunction(keyString))
            return getStaticFunction(keyString)
        return NIL
    }

    override fun set(key: LuaValue?, value: LuaValue) {
        val keyString = key?.checkjstring() ?: return
        if (containsStaticProperty(keyString))
            setStaticProperty(keyString, value)
    }

    override fun call(): LuaValue? = invoke(NONE).arg1()
    override fun call(arg: LuaValue?): LuaValue? = invoke(arg).arg1()
    override fun call(arg1: LuaValue?, arg2: LuaValue?): LuaValue? = invoke(varargsOf(arg1, arg2)).arg1()
    override fun call(arg1: LuaValue?, arg2: LuaValue?, arg3: LuaValue?): LuaValue? =
        invoke(varargsOf(arg1, arg2, arg3)).arg1()

    override fun invoke(args: Varargs?): Varargs = onInvoke(args).eval()
    override fun onInvoke(args: Varargs?): Varargs = invokeConstructor(args)

    override fun typename(): String = kClass.qualifiedName ?: "Unknown KClass"
    override fun tostring(): LuaValue = LuaValue.valueOf(toString())
    override fun toString(): String = m_instance.toString()
}