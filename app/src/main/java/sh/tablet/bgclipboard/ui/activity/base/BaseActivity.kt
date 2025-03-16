@file:Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package sh.tablet.bgclipboard.ui.activity.base

import android.os.Build
import android.os.Bundle
import android.os.FileUtils
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.viewbinding.ViewBinding
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.LayoutInflaterClass
import com.highcapable.yukihookapi.hook.xposed.parasitic.activity.base.ModuleAppCompatActivity
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sh.tablet.bgclipboard.R
import sh.tablet.bgclipboard.application.MainApplication
import sh.tablet.bgclipboard.application.MainApplication.Companion.LOGTAG
import sh.tablet.bgclipboard.data.AppConfigDatabase
import sh.tablet.bgclipboard.root.SillyService
import sh.tablet.bgclipboard.utils.factory.isNotSystemInDarkMode
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.PosixFileAttributeView

abstract class BaseActivity<VB : ViewBinding> : ModuleAppCompatActivity() {

	/**
	 * Get the binding layout object
	 *
	 * 获取绑定布局对象
	 */
	lateinit var binding: VB

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = current().generic()?.argument()?.method {
			name = "inflate"
			param(LayoutInflaterClass)
		}?.get()?.invoke<VB>(layoutInflater) ?: error("binding failed")
		setContentView(binding.root)
		/**
		 * Hide Activity title bar
		 * 隐藏系统的标题栏
		 */
		actionBar?.hide()
		/**
		 * Init immersive status bar
		 * 初始化沉浸状态栏
		 */
		WindowCompat.getInsetsController(window, window.decorView).apply {
			isAppearanceLightStatusBars = isNotSystemInDarkMode
			isAppearanceLightNavigationBars = isNotSystemInDarkMode
		}
		ResourcesCompat.getColor(resources, R.color.colorThemeBackground, null).also {
			window?.statusBarColor = it
			window?.navigationBarColor = it
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) window?.navigationBarDividerColor = it
		}
		/**
		 * Init children
		 * 装载子类
		 */
		onCreate()
	}

	/**
	 * Callback [onCreate] method
	 *
	 * 回调 [onCreate] 方法
	 */
	abstract fun onCreate()
	override fun finish() {
		super.finish()
		doOnSave("finish")
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		doOnSave("saveInstanceState")
	}

	override fun onPause() {
		super.onPause()
		doOnSave("pause")
	}

	override fun onDestroy() {
		super.onDestroy()
		doOnSave("destroy")
		AppConfigDatabase.get(baseContext).close()
	}

	protected fun doOnSave(typ: String) {
		Log.d(LOGTAG, "in '$typ'")
		val db = AppConfigDatabase.get(baseContext)
		val rfs = SillyService.getFs()

		val arr: Array<Any?> = arrayOf()
		val pragmaQuery = runBlocking(Dispatchers.IO) {
//			db.query("PRAGMA wal_checkpoint(TRUNCATE)", arr)
			db.query("vacuum", arrayOf())
		}
		pragmaQuery.close()
		/*val vacuumQuery = runBlocking(Dispatchers.IO) {
			db.mdb?.query("VACUUM")
		}
		vacuumQuery?.close()*/
		val dbFile = getDatabasePath(AppConfigDatabase.DB)
		val inFile = FileInputStream(dbFile)
		val origOut = rfs.getFile("/data/system/${AppConfigDatabase.DB}")
		run {
			origOut.delete()
			origOut.createNewFile()
			Shell.cmd("chown system:system ${origOut.absolutePath}").exec()
		}
		val outFile = origOut.newOutputStream()
		FileUtils.copy(inFile, outFile)
//		Log.d(LOGTAG, "checkpointed = ${(pragmaQuery.getInt(2) >= 0)}")
		inFile.close()
		outFile.close()
	}
}