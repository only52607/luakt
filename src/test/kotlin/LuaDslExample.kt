import com.ooooonly.luakt.*
import com.ooooonly.luakt.utils.*
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.jse.JsePlatform

fun sub(a: Int, b: Int) = a - b

//get the execution environment and initializes it
val testGlobals by lazy {
    val globals = JsePlatform.standardGlobals()
    //use dsl to edit luaTable
    globals.edit {

        //easy to set a value
        "welcome" to "Easy to use lua in kotlin."
        "lucky_number" to 8

        //three ways to build a function
        "kotlin_add" to luaFunctionOf<Int, Int> { a, b ->
            a + b
        }
        "kotlin_add3" to varArgFunctionOf { args: Varargs ->
            LuaValue.valueOf(args[0].asKValue<Int>() + args[1].asKValue<Int>() + args[2].asKValue<Int>())
        }
        "kotlin_sum" to varArgFunctionOf { args: Varargs ->
            var sum = 0
            args.forEach {
                sum += it.asKValue<Int>()
            }
            LuaValue.valueOf(sum)
        }

        "check_long" to luaFunctionOf { a: Long ->
            println(a)
        }

        //use dsl to build a table
        "info" to buildLuaTable {
            "author" to "ooooonly"
            "version" to 1
        }

        "get_table" to luaFunctionOf {
            return@luaFunctionOf mapOf(Pair(1, 1), Pair(2, 2), Pair(3, 3))
        }

        "hook" to Hide()

    }
    globals
}

class Hide {
    val i = 123
    val s = "hello"
}

fun main() {
    testGlobals.load(
        """
        print(hook)
        check_long(1231231231)
        for a,b in ipairs(get_table()) do
            print(a)
        end

        --get value from kotlin
        print(welcome)
        print("your lucky number is " .. lucky_number )

        --get table value from kotlin
        print(info.author)
        print(info.version)

        --invoke method from kotlin
        print( "1 + 2 = " .. kotlin_add(1,2))
        print( "1 + 2 + 3 = " .. kotlin_add3(1,2,3))
        print( "sum of 1, 2, 3, 4, 5, 6 is " .. kotlin_sum(1,2,3,4,5,6))

        -- set value to global
        lua_info = "surprise!"

        -- set function to global
        function hello()
            print("hello from lua")
        end

        function lua_add(a,b)
            return a + b
        end

    """.trimIndent()
    )()

    //get value in lua
    val luaInfo = testGlobals["lua_info"].asKValue<String>()
    println(luaInfo)

    //invoke function in lua
    testGlobals["hello"]()

    val sum = testGlobals["lua_add"].invoke(3.asLuaValue(), 5.asLuaValue())

    val printFun = testGlobals["print"]

    printFun.invoke(sum.asLuaValue())

}