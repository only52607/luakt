import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction

class LuaTableBuilder(var tableValue: LuaTable = LuaTable()){
    infix fun String.to(value:Any){
        tableValue[this] = value
    }

    infix fun Any.to(value:Any) {
        tableValue[this.asLuaValue()] = value
    }

    infix fun Any.toFun(value:(Varargs) -> Any) {
        tableValue[this.asLuaValue()] = value
    }
}

fun luaTableOf(builder:(LuaTableBuilder).() ->Unit):LuaTable = LuaTableBuilder().apply{ builder() }.tableValue

fun LuaTable.edit(editor:(LuaTableBuilder).() ->Unit) = LuaTableBuilder(this).editor()



