@Entity(tableName = "main_table")
class MainTableWrap {
	@PrimaryKey()
	var _id: Int? = null
	var date: String? = null
	var datetime: Long? = null
	var log_time: Int? = null
	var str_time: String? = null
	var reg_no: String? = null
	var airplane_type: Int? = null
	var day_night: Int? = null
	var ifr_vfr: Int? = null
	var flight_type: Int? = null
	var description: String? = null
}