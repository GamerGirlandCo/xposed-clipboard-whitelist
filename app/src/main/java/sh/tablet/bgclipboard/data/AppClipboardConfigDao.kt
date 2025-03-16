package sh.tablet.bgclipboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface AppClipboardConfigDao {
	@Query("select * from app_config")
	suspend fun all(): List<AppClipboardConfig>

	@Query("select * from app_config where is_system = 0 order by name asc")
	suspend fun allUser(): List<AppClipboardConfig>

	@Query("select * from app_config where (name like :q or id like :q) and is_system = :s order by name asc")
	suspend fun findByName(q: String, s: Boolean): List<AppClipboardConfig>

	@Query("select * from app_config where id = :q limit 1")
	suspend fun findById(q: String): AppClipboardConfig?

	@Upsert
	suspend fun insertAll(vararg configs: AppClipboardConfig)

	@Upsert
	suspend fun insertAll(configs: List<AppClipboardConfig>)

	@Update
	suspend fun updateOne(a: AppClipboardConfig)

	@Delete
	suspend fun delete(c: AppClipboardConfig)
}