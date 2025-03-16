@file:SuppressLint("StaticFieldLeak", "RestrictedApi")

package sh.tablet.bgclipboard.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Process
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import com.highcapable.yukihookapi.hook.factory.field
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sh.tablet.bgclipboard.Util
import sh.tablet.bgclipboard.application.MainApplication
import sh.tablet.bgclipboard.application.MainApplication.Companion.LOGTAG
import sh.tablet.bgclipboard.root.SillyService
import sh.tablet.bgclipboard.utils.factory.toElementString
import java.io.File

@Database(entities = [AppClipboardConfig::class], version = 1)
abstract class AppConfigDatabase : RoomDatabase() {
	abstract fun configDao(): AppClipboardConfigDao

	companion object {
		@Volatile
		@JvmField
		internal var INSTANCE: AppConfigDatabase? = null
		val DB
			get() = "clipboard_whitelist.db"
		val isSystem
			get() = Process.myUid() == Process.SYSTEM_UID

		@Volatile
		@JvmField
		var mdb: SupportSQLiteDatabase? = null

		private val str = "/data/system/$DB"

		@Synchronized
		@JvmStatic
		fun get(ctx: Context = Util.sys()): AppConfigDatabase {
			Log.d(LOGTAG, "uid = ${Process.myUid()}, tid = ${Thread.currentThread().id}")
			Log.d(
				LOGTAG,
				"[$DB@${mdb?.path}] isSystem = $isSystem; isOpen = ${INSTANCE?.isOpen}"
			)
			if (INSTANCE != null && !isSystem) {
				Log.d(LOGTAG, "<-")
				return INSTANCE!!
			}
			var bldr =
				Room.databaseBuilder(ctx, AppConfigDatabase::class.java, DB)
			if (!isSystem) {
				run {
					val fsMan = SillyService.getFs()
					val file = fsMan.getFile(str)
					if (file.exists())
						bldr = bldr.createFromFile(file)
				}
			} else {
				val file = File(str)
				Log.d(LOGTAG, "readable = ${file.canRead()}; writable = ${file.canWrite()}")
				file.setReadable(true)
				bldr = bldr.allowMainThreadQueries().createFromInputStream({ file.inputStream() },
					object : PrepackagedDatabaseCallback() {
						override fun onOpenPrepackagedDatabase(db: SupportSQLiteDatabase) {
							super.onOpenPrepackagedDatabase(db)
							mdb = db
						}
				})
				Log.d(LOGTAG, file.absolutePath)
				file.setExecutable(true)
			}

			INSTANCE = bldr.build()
			runBlocking(Dispatchers.IO) {
				INSTANCE!!.query("PRAGMA read_uncommitted = true;", arrayOf()).close()
				INSTANCE?.assertNotMainThread()
				val ab = INSTANCE!!.configDao().all()
				val a = ab.toTypedArray().toElementString()
				Log.d(LOGTAG, "-> $INSTANCE || $a[${ab.size}]")
			}
			Log.d(
				LOGTAG,
				"-> [$DB@${mdb?.path}] isSystem = $isSystem; isOpen = ${INSTANCE?.isOpen}"
			)
			return INSTANCE!!
		}
	}

	private class Callbacks : Callback() {
		override fun onOpen(db: SupportSQLiteDatabase) {
			super.onOpen(db)
			Log.d(LOGTAG, "opened")
			if (mdb == null) mdb = db
		}

		override fun onCreate(connection: SQLiteConnection) {
			super.onCreate(connection)
			Log.d(LOGTAG, "created")
		}
	}
}