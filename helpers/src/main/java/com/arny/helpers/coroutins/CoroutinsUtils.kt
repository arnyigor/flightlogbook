package com.arny.helpers.coroutins

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

fun getMainScope() = CoroutineScope(Dispatchers.Main)

fun getIOScope() = CoroutineScope(Dispatchers.IO)