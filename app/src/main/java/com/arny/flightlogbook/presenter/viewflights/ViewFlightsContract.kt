package com.arny.flightlogbook.presenter.viewflights

import com.arny.arnylib.presenter.base.BaseMvpPresenter
import com.arny.arnylib.presenter.base.BaseMvpView

object ViewFlightsContract {
    interface View : BaseMvpView {
    }

    interface Presenter : BaseMvpPresenter<View> {
    }
}