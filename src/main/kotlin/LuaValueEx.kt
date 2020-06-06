import luakotlin.KotlinInstanceInLua
import org.luaj.vm2.*
import org.luaj.vm2.lib.VarArgFunction
import java.util.*
import kotlin.reflect.KClass

/*
    Varargs
        LuaValue
            LuaUserdata
            LuaTable(Metatable)
                Globals
            LuaFunction
                LuaClosure
                LibFunction
                    ZeroArgFunction
                    TwoArgFunction
                        BaseLib(ResourceFinder)
                        PackageLib
                        IoLib
                        DebugLib
                        StringLib
                    ThreeArgFunction
                    VarArgFunction
*/

interface LuaValueConvertible {
    fun asLuaValue(): LuaValue
}

inline fun <reified T> LuaValue.asKValue(default: T? = null): T =
    asKValue(T::class, default) as T

fun LuaValue.asKValue(clazz: KClass<*>, default: Any? = null): Any =
    when (clazz) {
        String::class -> default?.let { optjstring(default as String) } ?: checkjstring()
        Int::class -> default?.let { optint(default as Int) } ?: checkint()
        Long::class -> default?.let { optlong(default as Long) } ?: checklong()
        Boolean::class -> default?.let { optboolean(default as Boolean) } ?: checkboolean()
        Float::class -> default?.let { optdouble((default as Float).toDouble()) } ?: checkdouble().toFloat()
        Double::class -> default?.let { optdouble(default as Double) } ?: checkdouble()
        Unit::class -> LuaValue.NIL
        Map::class -> checktable().let { table ->
            HashMap<LuaValue, LuaValue>().apply {
                table.keys().forEach {
                    set(it, table[it])
                }
            }
        }
        MutableList::class -> checktable().asMutableList()
        Iterable::class -> checktable().asMutableList()
        Collection::class -> checktable().asMutableList()
        MutableIterable::class -> checktable().asMutableList()
        MutableCollection::class -> checktable().asMutableList()
        List::class -> checktable().asMutableList()
        else -> if (this is LuaUserdata) this.m_instance
        else throw Exception("Could not convert LuaValue to ${clazz.simpleName}!")
    }


inline fun <reified T> Varargs.asKValue(default: T? = null): T =
    when (T::class) {
        Varargs::class -> this
        else -> arg1().asKValue<T>()
    } as T

fun LuaTable.asMutableList() = let { table ->
    mutableListOf<LuaValue>().apply {
        for(i in 1..table.keyCount()){
            add(table.rawget(i))
        }
    }
}

operator fun LuaValue.invoke(vararg args:Any):Varargs{
    val luaArgs = mutableListOf<LuaValue>()
    args.forEach {
        luaArgs.add(it.asLuaValue())
    }
    return invoke(luaArgs.toTypedArray())
}
operator fun LuaFunction.invoke(vararg args:Any):Varargs{
    val luaArgs = mutableListOf<LuaValue>()
    args.forEach {
        luaArgs.add(it.asLuaValue())
    }
    return invoke(luaArgs.toTypedArray())
}

inline operator fun <reified T> LuaValue.invoke(vararg args:Any):T = invoke(*args).asKValue()

fun Any.asLuaValue():LuaValue = when(this){
    is String -> LuaValue.valueOf(this)
    is Int -> LuaValue.valueOf(this)
    is Double -> LuaValue.valueOf(this)
    is Boolean -> LuaValue.valueOf(this)
    is Unit -> LuaValue.NIL
    is Float -> LuaValue.valueOf(this.toDouble())
    is Long -> LuaValue.valueOf(this.toDouble())
    is LuaValue -> this
    is Function1<*, *> -> object : VarArgFunction() {
        override fun onInvoke(args: Varargs?): Varargs =
            args?.let {
                ((this@asLuaValue as (Varargs) -> Any)(args)).asVarargs()
            }?:LuaValue.NIL
    }
    is Map<*,*> -> LuaTable().apply {
        this@asLuaValue.forEach { (key, value) ->
            set(key?.asLuaValue(),value?.asLuaValue()?:LuaValue.NIL)
        }
    }
    is Iterable<*> -> LuaTable().apply {
        var i:Int = 1
            this@asLuaValue.forEach {
                it?.let {
                    this.set(i++,it.asLuaValue())
                }
            }
        }
    is LuaValueConvertible -> this.asLuaValue()
    else -> KotlinInstanceInLua(this)
}

