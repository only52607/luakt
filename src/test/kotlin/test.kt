import org.junit.Test
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.jse.JsePlatform
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

fun transFunction(f : Any){
    println(f is KCallable<*>)
    println(f is KFunction<*>)
    println(f is Function<*>)

    /*
    when(f){
        is Function3<*, *, *, *> ->{
            (f as ( (Any,Any,Any) ->Any )) (1,2,3)
        }
    }
    f::class.members.forEach {
        it.parameters.forEach {
            it.type.
        }
    }*/
}

fun main(){
    /*
    transFunction({ a:Int, b: Int, c: Int ->
        println("hi")
        println(a+b+c)
    })

    return
    */
    val globals = JsePlatform.standardGlobals()
    globals.edit {
        "add" toFun {
            it[0].asKValue<Int>() + it[1].asKValue<Int>()
        }
        "sub" to { arg:Varargs ->
            arg[0].asKValue<Int>() - arg[1].asKValue<Int>()
        }
        "show" to luaFunctionOf<String> {
            println(it)
        }
    }

    globals.load("""
        print(add(1,2))
        print(sub(1,2))
        show("hello luakt")
    """.trimIndent())()

}

