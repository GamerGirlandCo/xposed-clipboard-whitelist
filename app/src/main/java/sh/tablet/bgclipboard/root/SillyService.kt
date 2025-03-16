package sh.tablet.bgclipboard.root

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import sh.tablet.bgclipboard.ISillyService
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import sh.tablet.bgclipboard.BuildConfig
import sh.tablet.bgclipboard.Util
import sh.tablet.bgclipboard.application.MainApplication
import sh.tablet.bgclipboard.utils.root.Native
import kotlin.concurrent.thread

class SillyService : RootService() {
	override fun onBind(intent: Intent): IBinder {
		bound = true
		Log.i(MainApplication.LOGTAG, "BOUND !!!")
		return ipc ?: SillyIPC()
	}
	override fun onUnbind(intent: Intent) = true

	override fun onRebind(intent: Intent) {
		super.onRebind(intent)
		if(!bound)
			bound = true
		Log.i(MainApplication.LOGTAG, "rebound?")
	}

	override fun onCreate() {
		super.onCreate()
		Log.d(MainApplication.LOGTAG, "service created !")
	}

	class SillyIPC : ISillyService.Stub() {
		init {
			ipc = this
		}

		override fun getFs() = FileSystemManager.getService()
		override fun getThis() = this
		override fun setegid(gid: Int): Boolean = Native.setegid(gid)

		override fun seteuid(uid: Int): Boolean = Native.seteuid(uid)

	}

	class AIDLConnection : ServiceConnection {
		@Volatile
		internal lateinit var fs: FileSystemManager
		var svc: ISillyService? = null
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			svc = ISillyService.Stub.asInterface(service)
			syncThings(svc!!)
			aidlConnection = this
		}

		@Synchronized
		private fun syncThings(ipc: ISillyService) {
			fs = FileSystemManager.getRemote(ipc.fs)
			SillyService.fs = this.fs
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			aidlConnection = null
		}
	}

	companion object {
		@Volatile
		private var t = Thread.currentThread()
		@Volatile
		@JvmField
		internal var bound = false

		@Volatile
		private var theOtherLooper: Looper? = null

		@Synchronized
		fun getFs(): FileSystemManager {
			val deferred = CompletableDeferred<FileSystemManager>()
			t = Thread.currentThread()
			if (fs == null) {
				val handler = CompletableDeferred<Handler>()
				if (Looper.myLooper() != Looper.getMainLooper()) {
					handler.complete(Handler(Looper.getMainLooper()))
				} else {
					t = thread(true) {
						Looper.prepare()
						theOtherLooper = Looper.myLooper()
						handler.complete(Handler(theOtherLooper!!))
						Looper.loop()
					}
				}
				fs = runBlocking {
					val ctx = Util.cur().applicationContext
					handler.await().post {
						deferred.complete(fs ?: FileSystemManager.getRemote(ipc!!.fs))
						theOtherLooper?.quitSafely()
					}
					deferred.await()
				}
			}
			return fs as FileSystemManager
		}

		@Synchronized
		fun getIsBound() = bound

		@Synchronized
		fun getBinder() = ipc

		@JvmField
		@Volatile
		internal var fs: FileSystemManager? = null

		@Volatile
		@JvmField
		internal var ipc: SillyIPC? = null

		@Volatile
		private var aidlConnection: AIDLConnection? = null

		@Synchronized
		fun getConn() = aidlConnection
	}
}