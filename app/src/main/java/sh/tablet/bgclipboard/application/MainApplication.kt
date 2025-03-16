package sh.tablet.bgclipboard.application

import android.app.Application
import android.content.Context
import android.content.ContextParams
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import com.topjohnwu.superuser.ipc.RootService
import sh.tablet.bgclipboard.Util.pkg
import sh.tablet.bgclipboard.root.SillyService
import java.util.concurrent.Executor

class MainApplication : ModuleApplication() {

	override fun onCreate() {
		super.onCreate()
		/**
		 * 跟随系统夜间模式
		 * Follow system night mode
		 */
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
		aidlConn = SillyService.AIDLConnection()
		// Your code here.
	}
	fun doBind(ctx: Context, executor: Executor) {
		val i = Intent(ctx, SillyService::class.java)
		i.addCategory(RootService.CATEGORY_DAEMON_MODE)
		RootService.bind(i, executor, aidlConn)
	}
	fun destroy() {
	}


	companion object {
		@Volatile
		private lateinit var aidlConn: SillyService.AIDLConnection
		init {
		    System.loadLibrary("setxuid")
		}
		const val LOGTAG = "TABLET::ClipboardWhitelist"

		fun createIntent(ctx: Context): Intent {
			val i = Intent(ctx, SillyService::class.java)
			i.addCategory(RootService.CATEGORY_DAEMON_MODE)
			return i
		}
		fun getConn() = aidlConn
	}
}