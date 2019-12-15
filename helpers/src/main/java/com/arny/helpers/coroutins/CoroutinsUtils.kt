package com.arny.helpers.coroutins

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.CoroutineContext

fun <T> launchAsync(block: suspend () -> T, onComplete: (T) -> Unit = {}, onError: (Throwable) -> Unit = {}, dispatcher: CoroutineDispatcher = Dispatchers.IO, context: CoroutineContext = Dispatchers.Main + SupervisorJob(), onCanceled: () -> Unit = {}): Job {
    val scope = CoroutineScope(context)
    return scope.launch {
        try {
            val result = withContext(dispatcher) { block.invoke() }
            onComplete.invoke(result)
        } catch (e: CancellationException) {
            onCanceled()
        } catch (e: Exception) {
            onError(e)
        }
    }
}

fun Job.addTo(compositeJob: CompositeJob) {
    compositeJob.add(this)
}

fun <T> CoroutineScope.launchSafe(block: suspend () -> T, onError: (Throwable) -> Unit? = { it.printStackTrace() }, onCanceled: () -> Unit? = {}): Job {
    return this.launch {
        try {
            block.invoke()
        } catch (e: CancellationException) {
            Log.e("Execute Block", "canceled by user")
            onCanceled()
        } catch (e: Exception) {
            onError(e)
        }
    }
}

fun <T> CoroutineScope.launchSafe(block: suspend () -> T): Job {
    return this.launch {
        try {
            block.invoke()
        } catch (e: CancellationException) {
            Log.e("Execute Block", "canceled by user")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getMainCoroutinContext() = Dispatchers.Main + SupervisorJob()

fun getMainScope(): CoroutineScope {
    return CoroutineScope(getMainCoroutinContext())
}

suspend fun <T> ioThread(block: suspend () -> T): T {
    return withContext(Dispatchers.IO) { block.invoke() }
}


fun <T> Flow<T>.handleErrors(onError: (Throwable) -> Unit = { it.printStackTrace() }): Flow<T> =
        catch { e -> onError(e) }

suspend fun <T> flowIO(block: suspend () -> T): Flow<T> {
    return withContext(Dispatchers.IO) { flowOf(block.invoke()) }
}

suspend fun <T> ioThread(block: suspend () -> T, onError: (Throwable) -> Unit? = {}): T? {
    return withContext(Dispatchers.IO) {
        try {
            block.invoke()
        } catch (e: CancellationException) {
            onError(e)
            null
        } catch (e: Exception) {
            onError(e)
            null
        }

    }
}