package com.arny.flightlogbook.constants

object CONSTS {

    object COMMON {
        const val ENABLE_CUSTOM_FIELDS = true
    }

    object FLIGHT {
        const val TYPE_CIRCLE = 0L
        const val TYPE_ZONE = 1L
        const val TYPE_RUOTE = 2L
    }

    object STRINGS {
        const val PARAM_COLOR = "color"
    }

    object DB {
        const val DB_NAME = "PilotDB"
        const val DB_VERSION = 14//13
        const val COLUMN_ID = "_id"
        const val COLUMN_DATETIME = "datetime"
        const val COLUMN_LOG_TIME = "log_time"
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
        const val REQUEST = "request"
        const val REQUEST_OPEN_FILE = 101
        const val REQUEST_ADD_EDIT_FLIGHT = 103
        const val REQUEST_SELECT_PLANE_TYPE = 105
        const val REQUEST_SELECT_FLIGHT_TYPE = 106
        const val REQUEST_EDIT_CUSTOM_FIELD = 107
        const val REQUEST_SELECT_CUSTOM_FIELD = 108
        const val REQUEST_EDIT_PLANE_TYPE = 109
        const val REQUEST_SELECT_AIRPORT_DEPARTURE = 110
        const val REQUEST_SELECT_AIRPORT_ARRIVAL = 111
        const val REQUEST_EDIT_AIRPORT = 112
    }

    object EXTRAS {
        const val EXTRA_FLIGHT_TYPE = "EXTRA_FLIGHT_TYPE_ID"
        const val EXTRA_CUSTOM_FIELD_ID = "EXTRA_CUSTOM_FIELD_ID"
        const val EXTRA_PLANE_TYPE_ID = "EXTRA_PLANE_TYPE_ID"
        const val EXTRA_ACTION_GET_CUSTOM_FIELD = "EXTRA_ACTION_GET_CUSTOM_FIELD"
        const val EXTRA_ACTION_EDIT_FLIGHT = "EXTRA_ACTION_EDIT_FLIGHT"
        const val EXTRA_ACTION_EDIT_PLANE_TYPE = "EXTRA_ACTION_EDIT_PLANE_TYPE"
        const val EXTRA_ACTION_EDIT_CUSTOM_FIELD = "EXTRA_ACTION_EDIT_CUSTOM_FIELD"
        const val EXTRA_ACTION_SELECT_CUSTOM_FIELD = "EXTRA_ACTION_SELECT_CUSTOM_FIELD"
        const val EXTRA_ACTION_SELECT_FLIGHT_TYPE = "EXTRA_ACTION_SELECT_FLIGHT_TYPE"
        const val EXTRA_ACTION_SELECT_PLANE_TYPE = "EXTRA_ACTION_SELECT_PLANE_TYPE"
        const val EXTRA_ACTION_SELECT_AIRPORT = "EXTRA_ACTION_SELECT_AIRPORT"
        const val EXTRA_ACTION_EDIT_AIRPORT = "EXTRA_ACTION_EDIT_AIRPORT"
        const val EXTRA_AIRPORT_ID = "EXTRA_AIRPORT_ID"
        const val EXTRA_AIRPORT = "EXTRA_AIRPORT"
    }
}
