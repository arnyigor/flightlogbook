package com.arny.core.utils

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

fun <T> fromCallable(callable: () -> T): Observable<T> = Observable.fromCallable(callable)

fun <T> fromSingle(callable: () -> T): Single<T> = Single.fromCallable(callable)

fun fromCompletable(action: (() -> Unit)): Completable = Completable.fromAction(action)

fun <T> fromNullable(callable: () -> T?): Observable<OptionalNull<T?>> =
    Observable.fromCallable { callable.invoke().toOptionalNull() }

fun <T> Observable<T>.observeOnMain(): Observable<T> {
    return this.subscribeOn(io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(observable: Observable<T>): Observable<T> {
    return observable.subscribeOn(io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(observable: Single<T>): Single<T> {
    return mainThreadObservable(io(), observable)
}

fun <T> mainThreadObservable(scheduler: Scheduler, observable: Single<T>): Single<T> {
    return observable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(scheduler: Scheduler, observable: Observable<T>): Observable<T> {
    return observable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(flowable: Flowable<T>): Flowable<T> {
    return flowable.subscribeOn(io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(scheduler: Scheduler, flowable: Flowable<T>): Flowable<T> {
    return flowable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}
