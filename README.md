# LuaKt
 Luaj extensions that support the Kotlin feature.
## Feature

- Unicode keywords such as Chinese are supported.
- Better interaction with Kotlin, for example, support for converting kotlin function types directly to lua functions
- You need to write just few code to use lua in kotlin.


## Gradle

```gradle
dependencies {
    implementation 'implementation 'com.ooooonly:luakt:0.0.1'
}
```

## Usage

Sample code 1

```kotlin
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
        "kotlin_add" to luaFunctionOf<Int,Int> {a,b->
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
```

Sample code 2

```kotlin
import org.luaj.vm2.lib.jse.JsePlatform

//test kotlin class
class MyKotlinClass {
    var info: String = "hello"
    var lucky_number: Int = 8
    fun add(a: Int, b: Int) = a + b
}

fun main() {
    val globals = JsePlatform.standardGlobals()
    val obj = MyKotlinClass()
    globals.edit {
        //set a kotlin object to lua
        "obj" to obj
    }
    globals.load(
        """
        print(obj.info)
        obj.info = "surprise!" --change value
        print(obj.info)
        
        print("your lucky number is " .. obj.lucky_number)
        
        --invoke method in kotlin object
        print(obj:add(1,5))
    """.trimIndent()
    )()
}
```

## License
[MIT License](https://github.com/ooooonly/luakt/blob/master/LICENSE)