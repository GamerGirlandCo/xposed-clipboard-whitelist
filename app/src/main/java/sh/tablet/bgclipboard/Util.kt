@file:SuppressLint("StaticFieldLeak")
package sh.tablet.bgclipboard

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ActivityThreadClass
import com.highcapable.yukihookapi.hook.type.android.ContextImplClass

object Util {
	private val sysCtx = ActivityThreadClass.method { name = "currentActivityThread" }.ignored().get().call()?.let {
		ActivityThreadClass.method { name = "getSystemContext" }.ignored().get(it).invoke<Context>()!!
//			tmp.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY.or(Context.CONTEXT_INCLUDE_CODE))
		/*ContextImplClass.method {
			name = "createApplicationContextAsUser"
			paramCount = 3
		}.ignored().get(tmp).invoke<Context>(
			pi,
			Context.CONTEXT_INCLUDE_CODE.or(Context.CONTEXT_IGNORE_SECURITY),
			UserHandle.getUserHandleForUid(pi.uid)
		)*/
	}
	fun sys() = sysCtx as Context
	fun pkg() = sysCtx!!.createPackageContext(BuildConfig.APPLICATION_ID, 0)
	fun cur(): Application {
		return ActivityThreadClass.method { name = "currentApplication" }.ignored().get().invoke<Application>()!!
	}
	fun extractContext(cls: Class<*>, instance: Any): Context? {
		return cls.field {
			name = "mContext"
		}.ignored().get(instance).any() as Context?
	}
	fun createApplicationContextAsUser(pkgName: String, flags: Int): Context {
		val appInfo = sysCtx!!.packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA.or(PackageManager.GET_GIDS))
		return ContextImplClass.method {
			name = "createApplicationContext"
			paramCount = 2
		}.get(sysCtx).invoke(appInfo, flags)!!
	}
}

