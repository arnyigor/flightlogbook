package com.arny.flightlogbook.presentation.common

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.MvpPresenter
import moxy.MvpView

abstract class BaseMvpPresenter<V : MvpView> : MvpPresenter<V>() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun Disposable.add() = compositeDisposable.add(this)

    private fun onDispose() {
        compositeDisposable.dispose()
    }

    fun <T : Any> Observable<T>.subscribeFromPresenter(
            onNext: (T) -> Unit = {},
            onError: (Throwable) -> Unit = { it.printStackTrace() },
            onComplete: () -> Unit = {},
            scheduler: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread()
    ) = subscribeOn(scheduler)
            .observeOn(observeOn)
            .subscribe(onNext, onError, onComplete)
            .add()

    fun <T : Any> Single<T>.subscribeFromPresenter(
            onSucces: (T) -> Unit = {},
            onError: (Throwable) -> Unit = { it.printStackTrace() },
            scheduler: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread()
    ) = subscribeOn(scheduler)
            .observeOn(observeOn)
            .subscribe(onSucces, onError)
            .add()

    fun Completable.subscribeFromPresenter(
            onComplete: () -> Unit = {},
            onError: (Throwable) -> Unit = { it.printStackTrace() },
            scheduler: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread()
    ) = subscribeOn(scheduler)
            .observeOn(observeOn)
            .subscribe(onComplete, onError)
            .add()

    override fun onDestroy() {
        super.onDestroy()
        onDispose()
    }
}