import com.github.only52607.luakt.dsl.cr.asKValue
import com.github.only52607.luakt.dsl.cr.asLuaValue
import com.github.only52607.luakt.dsl.cr.luaFunctionOf
import com.github.only52607.luakt.dsl.cr.luaTableOf
import com.github.only52607.luakt.dsl.forEachVararg
import com.github.only52607.luakt.dsl.varArgFunctionOf
import com.github.only52607.luakt.dsl.withJseStandardGlobals
import com.github.only52607.luakt.mappers.withDefaultValueMapper
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

fun sub(a: Int, b: Int) = a - b

class Hide {
    val i = 123
    val s = "hello"
}

fun main() {
    withDefaultValueMapper {
        withJseStandardGlobals {
            //easy to set a value
            "welcome" to "Easy to use lua in kotlin."
            "lucky_number" to 8

            //three ways to build a function
            "kotlin_add" to luaFunctionOf<Int, Int> { a, b ->
                a + b
            }
            "kotlin_add3" to varArgFunctionOf { args: Varargs ->
                LuaValue.valueOf(args.arg(1).asKValue<Int>() + args.arg(2).asKValue<Int>() + args.arg(3).asKValue<Int>())
            }
            "kotlin_sum" to varArgFunctionOf { args: Varargs ->
                var sum = 0
                args.forEachVararg {
                    sum += it.asKValue<Int>()
                }
                LuaValue.valueOf(sum)
            }

            "check_long" to luaFunctionOf { a: Long ->
                println(a)
            }

            //use dsl to build a table
            "info" to luaTableOf {
                "author" to "ooooonly"
                "version" to 1
            }

            "get_table" to luaFunctionOf {
                return@luaFunctionOf mapOf(Pair(1, 1), Pair(2, 2), Pair(3, 3))
            }

            "hook" to Hide()

            load("""
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
            """.trimIndent())()
            //get value in lua
            val luaInfo = this@withJseStandardGlobals["lua_info"].asKValue<String>()
            println(luaInfo)

            //invoke function in lua
            this@withJseStandardGlobals["hello"]()
            val sum = this@withJseStandardGlobals["lua_add"].invoke(3.asLuaValue(), 5.asLuaValue())
            val printFun = this@withJseStandardGlobals["print"]
            printFun.invoke(sum.asLuaValue())
        }
    }
}