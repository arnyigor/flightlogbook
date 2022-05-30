package com.arny.core

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
        const val COLUMN_ID = "_id"
        const val COLUMN_DATETIME = "datetime"
        const val COLUMN_LOG_TIME = "log_time"
    }

    object FILES {
        const val FILE_NAME_XLS = "PilotLogBook.xls"
        const val FILE_NAME_JSON = "PilotLogBook.json"
        const val FILE_EXTENTION_XLS = ".xls"
        const val FILE_EXTENTION_JSON = ".json"
    }

    object PREFS {
        const val PREF_USER_FILTER_FLIGHTS = "config_user_filter_flights"
        const val PREF_LAST_FLIGHT_DATA_AIRPLANE_ID = "PREF_LAST_FLIGHT_DATA_AIRPLANE_ID"
        const val PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID = "PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID"
    }

    object REQUESTS {
        const val REQUEST_PLANE_TYPE = "request_plane_type"
        const val REQUEST_PLANE_TYPE_EDIT = "request_plane_type_edit"
        const val REQUEST_FLIGHT_TYPE = "request_flight_type"
        const val REQUEST_CUSTOM_FIELD_EDIT = "request_custom_field_edit"
        const val REQUEST_CUSTOM_FIELD = "request_custom_field"
        const val REQUEST_AIRPORT_EDIT = "request_airport_edit"
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
