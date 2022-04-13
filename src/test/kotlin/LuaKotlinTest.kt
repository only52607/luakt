import com.github.only52607.luakt.dsl.withJseStandardGlobals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

class MyLib : TwoArgFunction() {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        val globals = env?.checkglobals() ?: return NIL
        globals.set("only", "hello66666")
        return NIL
    }
}

class TClass {
    fun hello() = println("hello")
}

fun main() {
    withJseStandardGlobals {
        load("""
        print(import)
        local t = import "TClass"
        print(t)
        i = t()
        i:hello()
        local m = import "java.lang.Math"
        print(m.PI)
        print(m.cos(0.5))

        require "MyLib"
        print(only)

        Thread = import "java.lang.Thread"
        Runnable = import "java.lang.Runnable"
        local thread = Thread(createProxy(Runnable,{
            run = function()
                print(888)
                print(999)
            end
        }))
        print("aa")
        thread:start()
    """.trimIndent())()
    }
}