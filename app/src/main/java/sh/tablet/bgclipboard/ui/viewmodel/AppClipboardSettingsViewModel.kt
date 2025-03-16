package sh.tablet.bgclipboard.ui.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo.FLAG_HAS_CODE
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import sh.tablet.bgclipboard.application.MainApplication
import sh.tablet.bgclipboard.data.AppClipboardConfig
import sh.tablet.bgclipboard.data.AppConfigDatabase

class AppClipboardSettingsViewModel(val app: Application) : AndroidViewModel(app) {
	val showSystem: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
	private val flags: MutableLiveData<Int> = MutableLiveData(FLAG_HAS_CODE)
	val query: MutableLiveData<String> = MutableLiveData("")
	var packages = mutableListOf<AppClipboardConfig>()

	private fun fetchPersistedPackages(): Deferred<ArrayList<AppClipboardConfig>> {
		return viewModelScope.async(Dispatchers.IO, start = CoroutineStart.DEFAULT) {
			val ret = ArrayList<AppClipboardConfig>()
			val db = AppConfigDatabase.get(app).configDao()
			val toAdd = if (showSystem.value == true) db.all() else db.allUser()
			ret.addAll(toAdd)
			ret
		}
	}

	public fun refreshPackages(cb: () -> Unit = { }) {

		val rawPackageList = ArrayList(app.packageManager.getInstalledApplications(flags.value!!))
		Log.d(MainApplication.LOGTAG, packages.toString())
		val uninstalled = packages.filter { b ->
			rawPackageList.firstOrNull { c ->
				b.id == c.packageName
			} == null
		}
		val db = AppConfigDatabase.get(app).configDao()
		viewModelScope.launch {
			packages = fetchPersistedPackages().await()
		}
		for (pkg in uninstalled) {
			viewModelScope.launch {
				db.delete(pkg)
			}
		}
		val notInDB = rawPackageList.filter { b ->
			packages.firstOrNull { c ->
				c.id == b.packageName
			} == null
		}.map {
			AppClipboardConfig(
				it.packageName, it.loadLabel(app.packageManager).toString(), false, it.flags.and(FLAG_SYSTEM) != 0
			)
		}
		packages.addAll(notInDB)
		viewModelScope.launch {
			db.insertAll(notInDB)
		}
		cb()
	}
}