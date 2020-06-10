package com.arny.helpers.utils

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

interface CompositeDisposableComponent {
    val compositeDisposable: CompositeDisposable

    fun Disposable.add() = compositeDisposable.add(this)

    fun resetCompositeDisposable() {
        compositeDisposable.clear()
    }

    fun <T : Any> Observable<T>.subsribeFromPresenter(
            onNext: (T) -> Unit = {},
            onError: (Throwable) -> Unit = { it.printStackTrace() },
            onComplete: () -> Unit = {},
            scheduler: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread()
    ) = subscribeOn(scheduler)
            .observeOn(observeOn)
            .subscribe(onNext, onError, onComplete)
            .add()

    fun <T : Any> Single<T>.subsribeFromPresenter(
            onSucces: (T) -> Unit = {},
            onError: (Throwable) -> Unit = { it.printStackTrace() },
            scheduler: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread()
    ) = subscribeOn(scheduler)
            .observeOn(observeOn)
            .subscribe(onSucces, onError)
            .add()

    fun Completable.subsribeFromPresenter(
            onComplete: () -> Unit = {},
            onError: (Throwable) -> Unit = { it.printStackTrace() },
            scheduler: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread()
    ) = subscribeOn(scheduler)
            .observeOn(observeOn)
            .subscribe(onComplete, onError)
            .add()
}