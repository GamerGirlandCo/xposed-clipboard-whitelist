package sh.tablet.bgclipboard.hook

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.util.Log
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.android.ContextImplClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import kotlinx.coroutines.runBlocking
import sh.tablet.bgclipboard.BuildConfig
import sh.tablet.bgclipboard.application.MainApplication.Companion.LOGTAG
import sh.tablet.bgclipboard.data.AppConfigDatabase
import sh.tablet.bgclipboard.utils.factory.toElementString
import java.io.File


@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
	override fun onInit() = configs {
		debugLog {
			isDebug = BuildConfig.DEBUG
			tag = LOGTAG
		}
	}
	val db: AppConfigDatabase
			get() = AppConfigDatabase.get()
	@SuppressLint("SdCardPath", "WrongConstant")
	override fun onHook() = encase {
		/*loadZygote {
			hookTheFuckingData()
		}*/
		loadSystem {
			hookTheFuckingData()
			val clipboardService = "com.android.server.clipboard.ClipboardService".toClass()
			Log.d(LOGTAG, "HOOKING ${clipboardService.name}")
			clipboardService.method {
				name = "clipboardAccessAllowed"
				paramCount = 7
			}.hook(YukiHookPriority.HIGHEST) {
				replaceAny {
					val inx: Any = instance()
					val rctx = clipboardService.method {
						name = "getContext"
					}.ignored().get(inx).invoke<Context>() ?: systemContext
					Log.d(LOGTAG, rctx.javaClass.name)
					val op = this.args[0] as Int
					val callingPackage = this.args[1] as String
					val callingUid = this.args[3] as Int
					Log.d(LOGTAG, "app '$callingPackage' ($callingUid) is requesting clipboard access...")

					val defFocus = inx.javaClass.method {
						name = "isDefaultDeviceAndUidFocused"
					}.ignored().get(inx).invoke<Boolean>(args[5], callingUid)!!
					val vdevFocus = inx.javaClass.method {
						name = "isVirtualDeviceAndUidFocused"
					}.ignored().get(inx).invoke<Boolean>(args[5], callingUid)!!
					val sysWinFocus = inx.javaClass.method {
						name = "isInternalSysWindowAppWithWindowFocus"
					}.ignored().get(inx).invoke<Boolean>(callingPackage)!!
					val defaultIme = inx.javaClass.method {
						name = "isDefaultIme"
						paramCount = 2
					}.ignored().get(inx).invoke<Boolean>(args[4], callingPackage)!!
					val focusCheck = defFocus.or(vdevFocus).or(sysWinFocus)
					if(op == OP_READ_CLIPBOARD)
					runBlocking {
						val app = db.configDao().findById(callingPackage)
						val enabled = app?.enabled ?: BuildConfig.DEBUG
						Log.d(LOGTAG, "app=${app.toString()}")
						val ret = enabled || callingUid == 1000 || focusCheck || defaultIme
						val verb = if (ret) "allowed" else "denied"
						Log.i(LOGTAG, "[${Process.myUid()}] $verb clipboard access to package $callingPackage")
						ret
					} else true
				}
			}
		}
	}

	val OP_READ_CLIPBOARD = AppOpsManager::class.java.field {
		name = "OP_READ_CLIPBOARD"
	}.ignored().get().int()
	val OP_WRITE_CLIPBOARD = AppOpsManager::class.java.field {
		name = "OP_WRITE_CLIPBOARD"
	}.ignored().get().int()

	private fun PackageParam.hookTheFuckingData() {
		val cimpl = ContextImplClass
		cimpl.method {
			name = "getDataDir"
		}.hook(YukiHookPriority.HIGHEST) {
			replaceAny {
				val pkgName = cimpl.method {
					name = "getPackageName"
				}.ignored().get(instance).invoke<String>()
				if(pkgName == "android")  {
					val f = File("/data/system")
					val db = File("/data/system/db")
					if(!db.exists())
						db.mkdir()
					db
				} else {
					invokeOriginal(*args)
				}
			}
		}
		// allow our context to communicate with the host app's context
	}

	companion object {
		const val TAG: String = "ContextImpl"
	}

}