import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


fun main(args: Array<String>) {

//    runInParallel(result = { s1, s2 ->
//        println("String 1: $s1")
//        println("String 2: $s2")
//    })

//    runInParallel2()
    val result = runInParallel3()
    println("result in runInParallel3 is $result")
//    runInParallel4()
}


fun runInParallel(result: (result1: Int, result2: Int) -> Unit) {
    val threadPool = Executors.newFixedThreadPool(2) // Create a fixed thread pool with 2 threads
    val scope = CoroutineScope(threadPool.asCoroutineDispatcher())

    runBlocking {
        scope.launch {
            val deferredResult1 = async {
                logTs( "register resultDelay3Deferred")
                httpCallWithDelay(3)
            }
            val deferredResult2 = async {
                logTs( "register resultDelay5Deferred")
                httpCallWithDelay(5)
            }

            val result1 = deferredResult1.await()
            val result2 = deferredResult2.await()

            result(result1, result2)
        }
    }
}

fun runInParallel2() {
    val threadPool = Executors.newFixedThreadPool(2) // Create a fixed thread pool with 2 threads
    val scope = CoroutineScope(threadPool.asCoroutineDispatcher())

    runBlocking {
        scope.launch {
            val deferredResult1 = async {
                logTs( "register resultDelay3Deferred")
                httpCallWithDelay(3)
            }
            val deferredResult2 = async {
                logTs( "register resultDelay5Deferred")
                httpCallWithDelay(5)
            }

            val result1 = deferredResult1.await()
            val result2 = deferredResult2.await()

            println(result1)
            println(result2)
        }
    }
}

fun runInParallel3(): Int {
    val threadPool = Executors.newFixedThreadPool(2) // Create a fixed thread pool with 2 threads
    val scope = CoroutineScope(threadPool.asCoroutineDispatcher())
    var sum = 0

    runBlocking {
        (3..10).forEach{ n ->
            scope.launch {
                val deferredResult1 = async {
                    logTs( "register resultDelay3Deferred")
                    httpCallWithDelay(n)
                }

                val result1 = deferredResult1.await()

                println(result1)
                sum += result1
                println("current sum is $sum")
            }
        }
    }

    println("final sum is $sum")
    return sum
}

fun runInParallel4() {
    val threadPool = Executors.newFixedThreadPool(5) // Create a fixed thread pool with 2 threads
    val dispatcher = threadPool.asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)
    var sum = 0

    scope.launch {
        withContext(dispatcher) {
            (3..5).forEach { n ->
                val deferredResult1 = async {
                    logTs( "register resultDelay3Deferred")
                    httpCallWithDelay(n)
                }

                val result1 = deferredResult1.await()
                println(result1)
            }
        }
    }
}

fun httpCallWithDelay(delay: Int): Int {
    var result = ""
    try {
        logTs("call started with delay of $delay second on thread ${Thread.currentThread().name}")
        // Define the API URL you want to request
        val apiUrl = "https://hub.dummyapis.com/delay?seconds=$delay"

        // Create a URL object with the API URL
        val url = URL(apiUrl)

        // Open a connection to the URL
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

        // Set the HTTP request method to GET
        connection.setRequestMethod("GET")

        // Get the response code from the server
        val responseCode: Int = connection.getResponseCode()
        logTs("call completed with delay of $delay second on thread ${Thread.currentThread().name}")
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Create a BufferedReader to read the response
            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            var inputLine: String?
            val response = StringBuffer()

            // Read the response line by line
            while (reader.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            reader.close()

            // Print the response
//            println(response.toString())
            result = response.toString()
        } else {
            println("GET request failed with response code: $responseCode")
            result = "call with delay $delay has failed"
        }

        // Close the connection
        connection.disconnect()
    } catch (e: IOException) {
        e.printStackTrace()
        result = "call with delay $delay has exception: ${e.message}"
    }
//    return result
    return delay
}

fun logTs(msg: String) {
    val currentTimeMillis = System.currentTimeMillis()

    // Create a SimpleDateFormat object to format the timestamp
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    // Create a Date object from the timestamp in milliseconds
    val date = Date(currentTimeMillis)

    // Format the date and time
    val formattedDate = sdf.format(date)

    // Print the formatted timestamp
    println("$msg at timestamp: $formattedDate")
}