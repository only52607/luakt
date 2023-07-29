package com.github.only52607.luakt.lib

import com.github.only52607.luakt.CoerceKotlinToLua
import com.github.only52607.luakt.CoerceLuaToKotlin
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.full.createType

@Suppress("unused")
class LuaKotlinExLib : TwoArgFunction() {
    private inline fun LuaValue.forEach(process: (key: LuaValue, value: LuaValue) -> Unit) {
        var k: LuaValue = LuaValue.NIL
        while (true) {
            val n = next(k)
            k = n.arg1()
            if (k.isnil())
                break
            val v = n.arg(2)
            process(k, v)
        }
    }

    override fun call(modName: LuaValue, env: LuaValue): LuaValue? {
        env["import"] = object : VarArgFunction() {
            override fun onInvoke(args: Varargs): Varargs {
                val fullName = args.checkjstring(1)
                val simpleName = Regex("\\.([_A-Za-z]\\w*)$").find(fullName)?.groupValues?.get(1)
                if (simpleName == null) {
                    LuaValue.argerror(1, "not a valid class name")
                }
                val alias = args.optjstring(2, simpleName)
                if (env.get(alias) != NIL) {
                    error("global variable '$alias' is already used")
                }
                env[alias] = env["luakotlin"]["bindClass"].call(LuaValue.valueOf(fullName))
                return NIL
            }
        }
        env["totable"] = object : VarArgFunction() {
            override fun onInvoke(args: Varargs): Varargs {
                if(args.istable(1)) return args.checktable(1)
                return when(val obj = args.checkuserdata(1)) {
                    is Collection<*> -> {
                        LuaValue.listOf(obj.map { CoerceKotlinToLua.coerce(it) }.toTypedArray())
                    }
                    is Map<*, *> -> {
                        val t = LuaTable()
                        for ((k, v) in obj) {
                            t.rawset(CoerceKotlinToLua.coerce(k), CoerceKotlinToLua.coerce(v))
                        }
                        t
                    }
                    else -> argerror("Collection or Map")
                }
            }
        }
        env["tomap"] = object : VarArgFunction() {
            override fun onInvoke(args: Varargs): Varargs {
                val map = mutableMapOf<Any?, Any?>()
                args.checktable(1).forEach { k, v ->
                    map[CoerceLuaToKotlin.coerce(k, Any::class.createType())] = CoerceLuaToKotlin.coerce(v, Any::class.createType())
                }
                return CoerceKotlinToLua.coerce(map)
            }
        }
        env["tolist"] = object : VarArgFunction() {
            override fun onInvoke(args: Varargs): Varargs {
                val list = mutableListOf<Any?>()
                args.checktable(1).forEach { _, v ->
                    list.add(CoerceLuaToKotlin.coerce(v, Any::class.createType()))
                }
                return CoerceKotlinToLua.coerce(list)
            }
        }
        env["toset"] = object : VarArgFunction() {
            override fun onInvoke(args: Varargs): Varargs {
                val set = mutableSetOf<Any?>()
                args.checktable(1).forEach { _, v ->
                    set.add(CoerceLuaToKotlin.coerce(v, Any::class.createType()))
                }
                return CoerceKotlinToLua.coerce(set)
            }
        }
        return NIL
    }
}