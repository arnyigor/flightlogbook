package com.arny.flightlogbook.presenter.base;

import android.arch.lifecycle.ViewModel;


public final class BaseViewModel<V extends BaseMvpView, T extends BaseMvpPresenter<V>> extends ViewModel {
    private T presenter;

    public void setPresenter(T presenter) {
        if (this.presenter == null) {
            this.presenter = presenter;
        }
    }

    public T getPresenter() {
        return this.presenter;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        presenter.detachView();
        presenter = null;
    }
}