package com.arny.domain.common

import com.arny.constants.CONSTS
import com.arny.data.repositories.MainRepositoryImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsUseCase @Inject constructor(private val repository: MainRepositoryImpl) {

    fun isAutoImportEnabled(): Boolean {
        return repository.getPrefBoolean(CONSTS.PREFS.PREF_DROPBOX_AUTOIMPORT_TO_DB, false)
    }

    fun isShowMoto(): Boolean {
        return repository.getPrefBoolean(CONSTS.PREFS.PREF_MOTO_TIME, false)
    }
}