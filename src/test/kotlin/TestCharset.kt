
import com.github.only52607.luakt.dsl.withJseStandardGlobals

fun main() {
    withJseStandardGlobals {
        load("""
        function string.urlEncode (srcURL)
        	str = srcURL:gsub (".", function (c)
                local byte = c:byte()
                print(byte)
                if byte == 32 then return "+" end
                if byte <128 and byte >=0 then return c end

        		return string.format ("%%%02X", byte)
        	end)
        	return str
        end

        local t = "a中b文c字符  sadfjkl"
        print(t:urlEncode())
    """)()
    }
}