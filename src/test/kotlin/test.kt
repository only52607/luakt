import org.luaj.vm2.lib.jse.JsePlatform

class MyClass {
    var info: String = "123456"
    var hello: Int = 0

    fun add(a: Int, b: Int) = a + b
}

fun hello() {
    println("hello")
}

fun add(a: Int, b: Int) = a + b

fun main() {
/*
    MyClass::class.declaredMemberFunctions.forEach {
        println(it.name)
        it.parameters.forEach {
            println(it.type.jvmErasure)
        }
    }
*/
    /*
    MyClass::class.declaredMemberProperties.forEach {
        println(it.name)
        println(it.isConst)
        println(it::class)
        println(it.getter.call(MyClass()))

        it.parameters.forEach {
            println(it.type.jvmErasure)
        }
    }
*/
    val globals = JsePlatform.standardGlobals()
    //use dsl to edit luaTable
    globals.edit {
        "obj" to MyClass()
    }
    globals.load(
        """
        print(obj.info)
        obj.info = "surprise!"
        print(obj.info)
        print(obj:add(1,5))
    """.trimIndent()
    )()
}

