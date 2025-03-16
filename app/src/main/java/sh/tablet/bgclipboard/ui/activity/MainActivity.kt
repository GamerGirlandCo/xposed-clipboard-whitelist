@file:Suppress("SetTextI18n")

package sh.tablet.bgclipboard.ui.activity

import com.highcapable.yukihookapi.YukiHookAPI
import sh.tablet.bgclipboard.R
import sh.tablet.bgclipboard.application.MainApplication
import sh.tablet.bgclipboard.databinding.ActivityMainBinding
import sh.tablet.bgclipboard.ui.activity.base.BaseActivity
import sh.tablet.bgclipboard.ui.fragment.AppClipboardSettings
import sh.tablet.bgclipboard.ui.fragment.DeactivatedStatus
import java.util.concurrent.Executor

class MainActivity : BaseActivity<ActivityMainBinding>() {

	override fun onCreate() {
		setContentView(R.layout.activity_main)
		val settingsFragment = if(YukiHookAPI.Status.isModuleActive) AppClipboardSettings.newInstance() else null
		val ctx = application as MainApplication
		ctx.doBind(ctx.applicationContext) {
			if(settingsFragment != null)
				settingsFragment.execute(it)
			else it.run()
		}
		if(YukiHookAPI.Status.isModuleActive) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.settingsOrStatus, settingsFragment!!)
				.commitNow()
		} else {
			supportFragmentManager.beginTransaction()
				.replace(R.id.settingsOrStatus, DeactivatedStatus.newInstance())
				.commitNow()
		}
	}
}