package com.arny.flightlogbook.domain.models

import com.arny.core.CONSTS.FILES.FILE_NAME_JSON
import com.arny.core.CONSTS.FILES.FILE_NAME_XLS

enum class ExportFileType(val fileName: String) {
    XLS(FILE_NAME_XLS),
    JSON(FILE_NAME_JSON)
}