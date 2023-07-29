package com.github.only52607.luakt.lib

import com.github.only52607.luakt.CoerceKotlinToLua
import com.github.only52607.luakt.CoerceLuaToKotlin
import com.github.only52607.luakt.KotlinClass
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import java.lang.reflect.*
import kotlin.reflect.KClass

class LuaKotlinLib : VarArgFunction() {
    override fun invoke(args: Varargs): Varargs {
        return try {
            when (opcode) {
                INIT -> {
                    // LuaValue modname = args.arg1();
                    val env = args.arg(2)
                    val t = LuaTable()
                    bind(t, this.javaClass, NAMES, BINDCLASS)
                    env["luakotlin"] = t
                    env["package"]["loaded"]["luakotlin"] = t
                    t
                }

                BINDCLASS -> {
                    val clazz = classForName(args.checkjstring(1))
                    KotlinClass.forKClass(clazz.kotlin)
                }

                NEWINSTANCE, NEW -> {
                    // get constructor
                    val c = args.checkvalue(1)
                    val clazz = if (opcode == NEWINSTANCE) classForName(c.tojstring()).kotlin else (c.checkuserdata(
                        KClass::class.java
                    ) as KClass<*>)
                    val consargs = args.subargs(2)
                    KotlinClass.forKClass(clazz).getConstructor()!!.invoke(consargs)
                }

                CREATEPROXY -> {
                    val niface = args.narg() - 1
                    if (niface <= 0) throw LuaError("no interfaces")
                    val lobj: LuaValue = args.arg(niface + 1)

                    // get the interfaces
                    val ifaces: Array<Class<*>?> = arrayOfNulls(niface)
                    var i = 0
                    while (i < niface) {
                        ifaces[i] = classForName(args.checkjstring(i + 1))
                        i++
                    }

                    // functional sugar check
                    when {
                        lobj.istable() -> {}
                        args.isfunction(niface + 1) -> {
                            if(ifaces.size != 1 || !ifaces[0]!!.isSAMInterface) {
                                throw LuaError("Functional handler is only applicable for proxying a single SAM interface")
                            }
                        }
                        else -> argerror("table")
                    }

                    // create the invocation handler
                    val handler: InvocationHandler = ProxyInvocationHandler(lobj)

                    // create the proxy object
                    val proxy = Proxy.newProxyInstance(javaClass.classLoader, ifaces, handler)

                    // return the proxy
                    userdataOf(proxy)
                }

                LOADLIB -> {

                    // get constructor
                    val classname = args.checkjstring(1)
                    val methodname = args.checkjstring(2)
                    val clazz = classForName(classname)
                    val method = clazz.getMethod(methodname, *arrayOf())
                    val result = method.invoke(clazz, *arrayOf())
                    if (result is LuaValue) {
                        result
                    } else {
                        NIL
                    }
                }

                else -> throw LuaError("not yet supported: $this")
            }
        } catch (e: LuaError) {
            throw e
        } catch (ite: InvocationTargetException) {
            throw LuaError(ite.targetException)
        } catch (e: Exception) {
            throw LuaError(e)
        }
    }

    // load classes using app loader to allow luaj to be used as an extension
    @Throws(ClassNotFoundException::class)
    fun classForName(name: String): Class<*> {
        return Class.forName(name, true, ClassLoader.getSystemClassLoader())
    }

    class ProxyInvocationHandler(private val lobj: LuaValue) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
            val name = method.name
            val func = if (lobj.isfunction()) lobj else lobj[name]  // sugar for SAM interface
            if (func.isnil()) return null
            val isvarargs = method.modifiers and METHOD_MODIFIERS_VARARGS != 0
            var n = args?.size ?: 0
            val v: Array<LuaValue?>
            if (isvarargs) {
                val o = args?.get(--n)
                val m = java.lang.reflect.Array.getLength(o)
                v = arrayOfNulls(n + m)
                for (i in 0 until n) v[i] = CoerceKotlinToLua.coerce(args?.get(i))
                for (i in 0 until m) v[i + n] = CoerceKotlinToLua.coerce(java.lang.reflect.Array.get(o, i))
            } else {
                v = arrayOfNulls(n)
                for (i in 0 until n) v[i] = CoerceKotlinToLua.coerce(args?.get(i))
            }
            val result = func.invoke(v).arg1()
            return CoerceLuaToKotlin.coerce(result, method.returnType)
        }
    }

    companion object {
        const val INIT = 0
        const val BINDCLASS = 1
        const val NEWINSTANCE = 2
        const val NEW = 3
        const val CREATEPROXY = 4
        const val LOADLIB = 5
        val NAMES = arrayOf(
            "bindClass",
            "newInstance",
            "new",
            "createProxy",
            "loadLib"
        )
        const val METHOD_MODIFIERS_VARARGS = 0x80
    }

    private val Class<*>.isSAMInterface: Boolean
        get() = isInterface && declaredMethods.filter { Modifier.isAbstract(it.modifiers) }.size == 1
}