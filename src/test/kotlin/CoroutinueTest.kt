
import com.github.only52607.luakt.dsl.withJseStandardGlobals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

suspend fun main() {
    val scope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = EmptyCoroutineContext
    }
    val job = scope.launch(Dispatchers.IO) {
        withJseStandardGlobals {
            load("""
                while true do
                    print(123)
                end
            """)()
        }
    }
    delay(200)
    job.cancel()
}