package sh.tablet.bgclipboard.ui.fragment

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.YukiHookAPI
import sh.tablet.bgclipboard.BuildConfig
import sh.tablet.bgclipboard.R
import sh.tablet.bgclipboard.databinding.FragmentDeactivatedBinding

/**
 * A simple [Fragment] subclass.
 * Use the [DeactivatedStatus.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeactivatedStatus : Fragment() {
	// TODO: Rename and change types of parameters
	private var _binding: FragmentDeactivatedBinding? = null
	private val binding: FragmentDeactivatedBinding
		get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		refreshModuleStatus()
		binding.mainTextVersion.text = getString(R.string.module_version, BuildConfig.VERSION_NAME)
		binding.hideIconInLauncherSwitch.isChecked = isLauncherIconShowing.not()
		binding.hideIconInLauncherSwitch.setOnCheckedChangeListener { button, isChecked ->
			if (button.isPressed) hideOrShowLauncherIcon(isChecked)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_deactivated, container, false)
	}

	private fun refreshModuleStatus() {
		binding.mainLinStatus.setCardBackgroundColor(
			when {
				YukiHookAPI.Status.isModuleActive -> R.color.success
				else -> R.color.colorGrayLighter
			}
		)
		binding.mainImgStatus.setImageResource(
			when {
				YukiHookAPI.Status.isModuleActive -> R.mipmap.ic_success
				else -> R.mipmap.ic_warn
			}
		)
		binding.mainTextStatus.text = getString(
			when {
				YukiHookAPI.Status.isModuleActive -> R.string.module_is_activated
				else -> R.string.module_not_activated
			}
		)
		binding.mainTextApiWay.isVisible = YukiHookAPI.Status.isModuleActive
		binding.mainTextApiWay.text = if (YukiHookAPI.Status.Executor.apiLevel > 0)
			"Activated by ${YukiHookAPI.Status.Executor.name} API ${YukiHookAPI.Status.Executor.apiLevel}"
		else "Activated by ${YukiHookAPI.Status.Executor.name}"
	}
	private fun hideOrShowLauncherIcon(isShow: Boolean) {
		requireContext().packageManager?.setComponentEnabledSetting(
			ComponentName(requireContext().packageName, "${BuildConfig.APPLICATION_ID}.Home"),
			if (isShow) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
			PackageManager.DONT_KILL_APP
		)
	}

	private val isLauncherIconShowing
		get() = requireContext().packageManager?.getComponentEnabledSetting(
			ComponentName(requireContext().packageName, "${BuildConfig.APPLICATION_ID}.Home")
		) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED

	companion object {
		/**
		 * Use this factory method to create a new instance of
		 * this fragment using the provided parameters.
		 *
		 * @return A new instance of fragment DeactivatedFragment.
		 */
		@JvmStatic
		fun newInstance() =
			DeactivatedStatus()
	}
}