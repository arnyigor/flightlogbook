package  com.arny.flightlogbook.presenter.base

import android.arch.lifecycle.Lifecycle
import android.os.Bundle


interface BaseMvpPresenter<V : BaseMvpView> {
    fun attachView(mvpView: V)
    fun detachView()
    fun getView(): V?
    fun getStateBundle(): Bundle?
    fun attachLifecycle(lifecycle: Lifecycle)
    fun detachLifecycle(lifecycle: Lifecycle)
    fun isViewAttached(): Boolean
    fun onPresenterCreated()
    fun onPresenterDestroy()
}