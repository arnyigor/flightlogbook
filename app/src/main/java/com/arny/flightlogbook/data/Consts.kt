package com.arny.flightlogbook.data

object Consts {

    object DB {
        //Table Name
        const val MAIN_TABLE = "main_table"
        const val TYPE_TABLE = "type_table"
        //Column Name
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

    object Files {
        const val EXEL_FILE_NAME = "PilotLogBook.xls"
    }

    object Prefs {
        const val DROPBOX_AUTOIMPORT_TO_DB = "dropbox_autoimport_to_db"
        const val CONFIG_USER_FILTER_FLIGHTS = "config_user_filter_flights"
    }

    object RequestCodes {
        const val REQUEST_EXTERNAL_STORAGE_XLS = 110
        const val REQUEST_DBX_EXTERNAL_STORAGE = 101
    }
}