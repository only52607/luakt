import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import java.lang.Exception
import java.util.HashMap

/*
    Varargs
        LuaValue
        LuaTable(Metatable)
        LuaFunction
            LuaClosure
        LuaUserData
*/

interface LuaValueConvertible{
    fun asLuaValue():LuaValue
}

inline fun <reified T> LuaValue.asKValue(default:T? = null) : T =
    when(T::class){
        String::class -> default?.let { optjstring(default as String) }?:checkjstring()
        Int::class -> default?.let { optint(default as Int) }?:checkint()
        Long::class -> default?.let { optlong(default as Long) }?:checklong()
        Boolean::class -> default?.let { optboolean(default as Boolean) }?:checkboolean()
        Float::class -> default?.let { optdouble((default as Float).toDouble())}?:checkdouble().toFloat()
        Double::class -> default?.let { optdouble(default as Double) }?:checkdouble()
        Unit::class -> LuaValue.NIL
        Map::class -> checktable().let {table->
            HashMap<LuaValue,LuaValue>().apply {
                table.keys().forEach{
                    set(it,table[it])
                }
            }
        }
        MutableList::class -> checktable().asMutableList()
        Iterable::class -> checktable().asMutableList()
        Collection::class -> checktable().asMutableList()
        MutableIterable::class -> checktable().asMutableList()
        MutableCollection::class -> checktable().asMutableList()
        List::class -> checktable().asMutableList()
        else -> throw Exception("Could not convert LuaValue to ${T::class.simpleName}!")
    } as T

fun LuaTable.asMutableList() = let {table->
    mutableListOf<LuaValue>().apply {
        for(i in 1..table.keyCount()){
            add(table.rawget(i))
        }
    }
}

fun LuaValue.invoke(vararg args:Any):Varargs{
    val luaArgs = mutableListOf<LuaValue>()
    args.forEach {
        luaArgs.add(it.asLuaValue())
    }
    return invoke(luaArgs.toTypedArray())
}

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
    else ->  throw Exception("Could not convert a ${this::class.simpleName} to LuaValue!")
}

fun Any.asVarargs():Varargs = when(this){
    is Varargs -> this
    is Array<*> -> LuaValue.varargsOf(mutableListOf<LuaValue>().apply {
        this@asVarargs.forEach {
            it?.let{
                add(it.asLuaValue())
            }
        }
    }.toTypedArray())
    else -> asLuaValue()
}

operator fun LuaTable.get(key:Any): LuaValue = get(key.asLuaValue())
operator fun LuaTable.set(key:Any, value:Any) = set(key.asLuaValue(),value.asLuaValue())

operator fun Varargs.get(index:Int) : LuaValue = arg(index + 1)

fun main(){
}
