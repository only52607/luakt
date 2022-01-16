import kotlinx.coroutines.delay
import org.luaj.vm2.lib.jse.JsePlatform

//test kotlin class
class MyKotlinClass : MySuperClass() {
    var info: String = "hello"
    var lucky_number: Int = 8
    override fun add(a: Int, b: Int): Int {
        println("invoke add2")
        return a + b
    }

    fun add(a: Int, b: Int, c: Int): Int {
        println("invoke add3")
        return a + b + c
    }

    var inner = InnerClass()
    suspend fun suspendAdd(a: Int, b: Int): Int {
        delay(2000)
        return a + b
    }
}

open class MySuperClass {
    open fun add(a: Int, b: Int): Int {
        println("I will be overrided")
        return -1
    }

    fun super_add(a: Int, b: Int): Int {
        println("super add2")
        return a + b
    }
}

class InnerClass {
    var num: Int = 666
    fun test() = "test success"
}

fun main() {
    val globals = JsePlatform.standardGlobals()
    val obj = MyKotlinClass()
//    globals["obj"] = obj
    globals.load(
        """
        print(obj.info)
        obj.info = "surprise!" --change value
        print(obj.info)
        
        print("your lucky number is " .. obj.lucky_number)
        
        --invoke method in kotlin object
        print(obj:add(1,5))
        print(obj:add(1,6,3))
        --print(obj:add(1,obj))
        print(obj:super_add(1,2))
        
        print(obj.inner.num)
        print(obj.inner:test())
        
        --invoke suspend function
        print(obj:suspendAdd(2,6))
    """.trimIndent()
    )()
}
