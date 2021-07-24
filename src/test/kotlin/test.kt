import com.ooooonly.luakt.*
import com.ooooonly.luakt.mapper.ValueMapperChain
import com.ooooonly.luakt.mapper.userdata.KotlinInstanceWrapper
import org.luaj.vm2.lib.jse.JsePlatform
import kotlin.jvm.internal.Intrinsics
import kotlin.reflect.full.declaredMemberFunctions

class MyClass {
    var info: String = "123456"
    var hello: Int = 0
    fun add(a: Int, b: Int) = a + b
    val data:Injected = Injected()
}

class ClassA{
    fun print(a:Any){
        println("ClassA got $a")
    }
}

class Injected {
    override fun toString(): String  = "Injected"
}
class Injector{
    override fun toString(): String  = "Injector"
}

fun hello() {
    println("hello")
}

fun add(a: Int, b: Int) = a + b

fun main() {

    ValueMapperChain.addKValueMapperBefore{ value, _ ->
        if(value is Injected) return@addKValueMapperBefore KotlinInstanceWrapper(Injector())
        return@addKValueMapperBefore null
    }

    ValueMapperChain.addLuaValueMapperBefore { value, targetClass,_ ->
        if(value is KotlinInstanceWrapper && value.m_instance is Injector) return@addLuaValueMapperBefore Injected()
        return@addLuaValueMapperBefore null
    }

    var hello by defaultGlobals.provideDelegate<Int>(defaultValue = 123)

    var b by defaultGlobals.provideDelegate<MyClass>()

    var c by defaultGlobals.provideDelegate<ClassA>()

    b = MyClass()
    c = ClassA()


    defaultGlobals["a"] = MyClass()

    executeLuaCode(
        """
        print(a)
        print(a.info)
        print(a:add(1,2))
        
        for k,v in pairs(a.__functions) do
            print(v)
        end
        print()
        for k,v in pairs(a.__properties) do
            print(k .. ":" .. tostring(v))
        end
        print()
        
        print(b)
        print(b.info)
        print(b:add(1,2))
        
        print(b.data)
        
        c:print(b.data)
        
    """.trimIndent()
    )

    println(hello)
}


