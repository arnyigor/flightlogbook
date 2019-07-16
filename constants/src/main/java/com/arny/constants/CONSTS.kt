package com.arny.constants

object CONSTS {

    object DB {
        const val DB_NAME = "PilotDB"
        const val DB_VERSION = 16//15
        const val MAIN_TABLE = "main_table"
        const val TYPE_TABLE = "type_table"
        const val COLUMN_ID = "_id"
        const val COLUMN_TYPE_ID = "type_id"
        const val COLUMN_DATETIME = "datetime"
        const val COLUMN_LOG_TIME = "log_time"
        const val COLUMN_REG_NO = "reg_no"
        const val COLUMN_AIRPLANE_TYPE = "airplane_type"
        const val COLUMN_DAY_NIGHT = "day_night"
        const val COLUMN_IFR_VFR = "ifr_vfr"
        const val COLUMN_FLIGHT_TYPE = "flight_type"
        const val COLUMN_DESCRIPTION = "description"
    }

    object FILES {
        const val EXEL_FILE_NAME = "PilotLogBook.xls"
    }

    object FRAGMENTS{
        const val FRAGMENT_TAG = "fragment_tag"
        const val FRAGMENT_TAG_TYPE_LIST = "type_list"
    }

    object PREFS {
        const val PREF_DROPBOX_AUTOIMPORT_TO_DB = "dropbox_autoimport_to_db"
        const val PREF_USER_FILTER_FLIGHTS = "config_user_filter_flights"
        const val PREF_MOTO_TIME = "motoCheckPref"
    }

    object REQUESTS {
        const val REQUEST_EXTERNAL_STORAGE_XLS = 100
        const val REQUEST_DBX_EXTERNAL_STORAGE = 101
        const val REQUEST_ADD_TIME = 102
        const val REQUEST_ADD_TYPE = 103
    }

    object EXTRAS {
        const val LOGTIME_STATE_KEY = "com.arny.flightlogbook.extra.instance.time"
        const val EXTRA_ADD_TIME_IDS = "extra_add_time_ids"
    }
}