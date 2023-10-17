import kotlinx.coroutines.*
import java.util.concurrent.Executors


fun main() {
    println(getPerson())
}

fun getPerson(): Person {
    val threadPool = Executors.newFixedThreadPool(5) // Create a fixed thread pool with 2 threads
    val dispatcher = threadPool.asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)


    return runBlocking {
        withContext(dispatcher) {
            val deferredRes1 = async { mockHttpCallWithReturn("Tom", 3) }
            val deferredRes2 = async { mockHttpCallWithReturn("Patrick", 4) }
            val deferredRes3 = async { mockHttpCallWithReturn("Brady", 5) }

            val person = Person(
                firstName = deferredRes1.await(),
                middleName = deferredRes2.await(),
                lastName = deferredRes3.await()
            )

            return@withContext person
        }
    }
}

suspend fun mockHttpCallWithReturn(result: String, delay: Int): String {

    logTs("call started with delay of $delay second on thread ${Thread.currentThread().name}")
    delay((delay * 1000).toLong())
    logTs("call completed with delay of $delay second on thread ${Thread.currentThread().name}")

    return result
}

data class Person(
    val firstName: String,
    val middleName: String,
    val lastName: String
)