package com.arny.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arny.core.utils.getIntValue
import com.arny.core.utils.toList
import com.arny.flightlogbook.data.db.DatabaseMigrations
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"
    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            MainDB::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate() {
        helper.createDatabase(TEST_DB, 15)
                .apply {
                    // db has schema version 15. insert some data using SQL queries.
                    // You cannot use DAO classes because they expect the latest schema.
                    execSQL("INSERT INTO main_table(log_time) VALUES(15)")
                    // Prepare for the next version.
                    close()
                }
        // Re-open the database with version 2 and provide
        val db = helper.runMigrationsAndValidate(TEST_DB, 16, true, DatabaseMigrations.MIGRATION_15_16)
        val cursor = db.query("SELECT * FROM main_table LIMIT 1")
        var time = 0
        cursor.toList {
            time = it.getIntValue("log_time")
        }
        assert(time != 0)
    }

}