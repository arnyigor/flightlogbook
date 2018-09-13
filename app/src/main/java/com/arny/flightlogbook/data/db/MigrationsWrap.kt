@Entity(tableName = "migrations")
class MigrationsWrap {
	@PrimaryKey()
	var _id: Int = 0
	var filename: String = ""
	var applytime: String = ""
}