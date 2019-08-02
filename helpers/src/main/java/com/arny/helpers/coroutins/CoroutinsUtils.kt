package com.arny.helpers.coroutins

import android.util.Log
import kotlinx.coroutines.*
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

fun <T> CoroutineScope.launch(block: suspend () -> T, onError: (Throwable) -> Unit? = {}, onCanceled: () -> Unit? = {}): Job {
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

fun getMainScope(): CoroutineScope {
    return CoroutineScope(Dispatchers.Main + SupervisorJob())
}

suspend fun <T> async(block: suspend () -> T): T {
    return withContext(Dispatchers.IO) { block.invoke() }
}

fun <T> launch(block: suspend () -> T): CoroutineScope {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    scope.launch { block.invoke() }
    return scope
}