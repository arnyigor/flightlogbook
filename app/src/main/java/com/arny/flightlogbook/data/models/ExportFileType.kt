package com.arny.flightlogbook.data.models

import com.arny.flightlogbook.data.CONSTS.FILES.FILE_NAME_JSON
import com.arny.flightlogbook.data.CONSTS.FILES.FILE_NAME_XLS

enum class ExportFileType(val fileName: String) {
    XLS(FILE_NAME_XLS),
    JSON(FILE_NAME_JSON)
}