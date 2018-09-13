@Entity(tableName = "type_table")
class TypeTableWrap {
	@PrimaryKey()
	var type_id: Int? = null
	var airplane_type: String? = null
}