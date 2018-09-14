package com.arny.flightlogbook.presenter.base;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.arny.flightlogbook.utils.ToastMaker;


public abstract class BaseMvpActivity<V extends BaseMvpView, P extends BaseMvpPresenter<V>>
        extends AppCompatActivity implements BaseMvpView {

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    protected P mPresenter;

    @SuppressWarnings("unchecked")
    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseViewModel<V, P> viewModel = ViewModelProviders.of(this).get(BaseViewModel.class);
        boolean isPresenterCreated = false;
        if (viewModel.getPresenter() == null) {
            viewModel.setPresenter(initPresenter());
            isPresenterCreated = true;
        }
        mPresenter = viewModel.getPresenter();
        mPresenter.attachLifecycle(getLifecycle());
        mPresenter.attachView((V) this);
        if (isPresenterCreated) {
            mPresenter.onPresenterCreated();
        }
    }

    @Override
    public void toastError(@Nullable String error) {
        ToastMaker.toastError(this, error);
    }

    @NonNull
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachLifecycle(getLifecycle());
        mPresenter.detachView();
    }

    protected abstract P initPresenter();
}