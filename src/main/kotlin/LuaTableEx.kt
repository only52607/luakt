import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

operator fun LuaTable.get(key:Any): LuaValue = get(key.asLuaValue())
operator fun LuaTable.set(key:Any, value:Any) = set(key.asLuaValue(),value.asLuaValue())