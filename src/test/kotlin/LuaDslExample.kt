import com.ooooonly.luakt.luaFunctionOf
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
        "kotlin_add3" to { args: Varargs ->
            args[0].asKValue<Int>() + args[1].asKValue<Int>() + args[2].asKValue<Int>()
        }
        "kotlin_sum" to { args: Varargs ->
            var sum: Int = 0
            args.forEach {
                sum += it.asKValue<Int>(0)
            }
            sum
        }
        "kotlin_sub" to luaFunctionOfKFunction(::sub)


        //use dsl to build a table
        "info" to luaTableOf {
            "author" to "ooooonly"
            "version" to 1
        }

    }
    globals
}


fun main(){
    //load string as lua code and also execute
    testGlobals.load("""
        --get value from kotlin
        print(welcome)
        print("your lucky number is " .. lucky_number )
        
        --get table value from kotlin
        print(info.author)
        print(info.version)
        
        --invoke method from kotlin
        print( "1 + 2 = " .. kotlin_add(1,2))
        print( "1 + 2 + 3 = " .. kotlin_add3(1,2,3))
        print( "5 - 3 = "  .. kotlin_sub(5,3))
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

    """.trimIndent())()

    //get value in lua
    val luaInfo = testGlobals["lua_info"].asKValue<String>()
    println(luaInfo)

    //invoke function in lua
    testGlobals["hello"]()

    val sum = testGlobals["lua_add"](3,5)
    val printFun = testGlobals["print"]

    printFun(sum)
}