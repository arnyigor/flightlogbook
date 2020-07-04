package com.arny.flightlogbook.constants

object CONSTS {
    object STRINGS {
        const val LOG_TIME_FORMAT = "[00]:[00]"
        const val PARAM_COLOR = "color"
    }

    object DB {
        const val DB_NAME = "PilotDB"
        const val DB_VERSION = 14//13
        const val COLUMN_ID = "_id"
        const val COLUMN_DATETIME = "datetime"
        const val COLUMN_LOG_TIME = "log_time"
        const val TABLE_CUSTOM_FIELDS = "custom_fields"
    }

    object FILES {
        const val EXEL_FILE_NAME = "PilotLogBook.xls"
    }

    object PREFS {
        const val PREF_DROPBOX_AUTOIMPORT_TO_DB = "dropbox_autoimport_to_db"
        const val PREF_USER_FILTER_FLIGHTS = "config_user_filter_flights"
        const val PREF_MOTO_TIME = "motoCheckPref"
        const val AUTO_EXPORT_XLS = "autoExportXLSPref"
    }

    object REQUESTS {
        const val REQUEST_OPEN_FILE = 101
        const val REQUEST_ADD_EDIT_FLIGHT = 103
        const val REQUEST_SELECT_PLANE_TYPE = 105
        const val REQUEST_SELECT_FLIGHT_TYPE = 106
    }

    object EXTRAS {
        const val EXTRA_PLANE_TYPE = "extra_plane_type_id"
        const val EXTRA_FLIGHT_TYPE = "extra_flight_type_id"
    }
}
