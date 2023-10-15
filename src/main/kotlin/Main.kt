import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import java.util.concurrent.*
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy


fun main(args: Array<String>) {

//    threadpoolInJava()
    blockingVsNonBlockingInCoroutine()
    // Shutdown the custom thread pool to release resources
}

/**
 *
 * delay and Thread.sleep are two different mechanisms for introducing pauses or delays in a program, and they have different use cases and behaviors:
   delay (Kotlin Coroutines):
   Use Case: delay is primarily used in Kotlin Coroutines to introduce non-blocking delays within a coroutine. It is used for suspending a coroutine without blocking the underlying thread, allowing other coroutines to run concurrently.
   Behavior: When you use delay in a coroutine, it suspends the coroutine's execution for a specified duration (typically in milliseconds) but frees up the underlying thread to execute other coroutines. It does not block the entire thread, enabling efficient concurrency.

   Thread.sleep (Java Threads):
   Use Case: Thread.sleep is used in traditional Java threading to introduce a blocking pause or delay within a thread. It is typically used when working with low-level threads and not within Kotlin Coroutines.
   Behavior: When you use Thread.sleep, it blocks the current thread (in this case, the entire thread) for a specified duration (in milliseconds). This means that no other tasks or coroutines can execute on the same thread during the sleep period.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun blockingVsNonBlockingInCoroutine() {
    val threadPool = Executors.newFixedThreadPool(2)

//    val scope = CoroutineScope(threadPool.asCoroutineDispatcher().limitedParallelism(3))
    val scope = CoroutineScope(threadPool.asCoroutineDispatcher())

    runBlocking {
        (1..30).forEach { eventId ->
            scope.launch {
                try {
                    // launch does only submit, each task might be executed in different thread
//                    myTaskWithNonBlocking(eventId)
                    executeBlockingTask(eventId)
                } catch (e: CancellationException) {
                    logTs("Task $eventId was cancelled.")
                } catch (e: RejectedExecutionException) {
                    logTs("Task $eventId was rejected.")
                } catch (e: Exception) {
                    logTs("Task $eventId encountered an error: ${e.javaClass.simpleName}")
                }
            }
        }
    }
//    threadPool.shutdown()
}

private fun threadpoolInJava() {
    val threadPool1 = Executors.newFixedThreadPool(2) // Create a fixed thread pool with 2 threads
    val threadPool2 = ThreadPoolExecutor(
        1,                  // Core pool size
        1,                  // Maximum pool size
        0L,                 // Keep-alive time for excess threads
        TimeUnit.MILLISECONDS,  // Time unit for keep-alive time
        LinkedBlockingQueue<Runnable>(1),  // A bounded blocking queue to hold tasks (only 1 task will be waiting)
//        CustomAbortPolicy()      // Rejection policy (throws RejectedExecutionException)
        MyRejectedExecutionHandler()
    )

    // Submit tasks to the thread pool
    (1..30).forEach { eventId ->
        try {
            threadPool2.execute {
                executeBlockingTask(eventId)
            }
        } catch (e: CancellationException) {
            println("Task $eventId was cancelled.")
        } catch (e: RejectedExecutionException) {
            println("Task $eventId was rejected.")
        } catch (e: Exception) {
            println("Caught an exception: ${e.javaClass.simpleName}: ${e.message}")
        }
    }
    threadPool2.shutdown()
}

private suspend fun executeNonBlockingTask(eventId: Int) {
    logTs("Task $eventId is running on ${Thread.currentThread().name}")
    val period = kotlin.random.Random.nextInt(5000)
    delay(period.toLong())
    logTs("Task $eventId on thread ${Thread.currentThread().name} with delay $period completed")
}

private fun executeBlockingTask(eventId: Int) {
    logTs("Task $eventId is running on ${Thread.currentThread().name}")
    val period = kotlin.random.Random.nextInt(5000)
    Thread.sleep(period.toLong())
    logTs("Task $eventId on thread ${Thread.currentThread().name} with delay $period completed")
}

class CustomAbortPolicy() : AbortPolicy() {
    override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
        // Add your custom rejection handling logic here
        System.err.println(
            "Custom rejection policy: Task " + r.toString() +
                    " rejected from " +
                    executor.toString()
        )
        // You can also choose to throw a custom exception or log the rejection differently
        // throw new CustomRejectedExecutionException("Custom rejection message");

    }
}

/**
 * The RejectedExecutionHandler interface in Java's Executor framework does not provide direct access to the runnable's
 * method name or input parameters.
 * It is designed to handle rejected tasks at a high level without access to the specific details of the task.
 * If you need to pass additional information about the task to the RejectedExecutionHandler,
 * you can do so by creating a custom task class that encapsulates both the task's runnable logic and any additional
 * data you want to pass.
 */
internal class MyRejectedExecutionHandler : RejectedExecutionHandler {
    override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
        // Handle the rejected task here
        println("Task rejected: $r")

        // You can attempt to re-submit the task, log the rejection, or take any other action
        // Attempt to re-submit the rejected task
        // Attempt to re-submit the rejected task
        try {
            executor.queue.put(r)
            println("Task resubmitted successfully.")
        } catch (e: InterruptedException) {
            println("Task resubmit failed: $e")
            Thread.currentThread().interrupt()
        }
    }
}