package  com.arny.flightlogbook.presenter.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.os.Bundle
import android.util.Log


open class BaseMvpPresenterImpl<V : BaseMvpView> : BaseMvpPresenter<V>, LifecycleObserver {
    private var stateBundle: Bundle? = null
    protected var mView: V? = null

    override fun attachView(mvpView: V) {
        mView = mvpView
    }

    override fun detachView() {
        mView = null
    }

    override fun getView(): V? {
        return mView
    }

    override fun getStateBundle(): Bundle? {
        if (stateBundle == null) {
            stateBundle = Bundle()
        }
        return stateBundle
    }

    override fun attachLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    override fun detachLifecycle(lifecycle: Lifecycle) {
        lifecycle.removeObserver(this)
    }

    override fun isViewAttached(): Boolean {
        return mView != null
    }

    override fun onPresenterCreated() {
        Log.i(BaseMvpPresenterImpl::class.java.simpleName, "onPresenterCreated: ");
    }

    override fun onPresenterDestroy() {
        if (stateBundle != null && !stateBundle!!.isEmpty) {
            stateBundle!!.clear()
        }
    }
}