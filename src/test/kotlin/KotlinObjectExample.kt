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
