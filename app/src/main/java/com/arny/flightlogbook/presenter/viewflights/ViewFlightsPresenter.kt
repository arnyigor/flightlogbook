package com.arny.flightlogbook.presenter.viewflights

import com.arny.arnylib.presenter.base.BaseMvpPresenterImpl
import com.arny.flightlogbook.data.source.MainRepository


class MainPresenter : BaseMvpPresenterImpl<ViewFlightsContract.View>(), ViewFlightsContract.Presenter {
    private val repository = MainRepository()

}
