@file:SuppressLint("NotifyDataSetChanged")
package sh.tablet.bgclipboard.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import sh.tablet.bgclipboard.R
import sh.tablet.bgclipboard.application.MainApplication
import sh.tablet.bgclipboard.databinding.FragmentAppClipboardSettingsBinding
import sh.tablet.bgclipboard.root.SillyService
import sh.tablet.bgclipboard.ui.SpacerItemDecoration
import sh.tablet.bgclipboard.ui.adapters.ClipboardSettingsListAdapter
import sh.tablet.bgclipboard.ui.viewmodel.AppClipboardSettingsViewModel
import java.util.concurrent.Executor

class AppClipboardSettings : Fragment() {

	companion object {
		fun newInstance() = AppClipboardSettings()
	}

	private var _binding: FragmentAppClipboardSettingsBinding? = null
	private val binding: FragmentAppClipboardSettingsBinding
		get() = _binding!!
	private val viewModel: AppClipboardSettingsViewModel by viewModels()
	private lateinit var adapter: ClipboardSettingsListAdapter
	private lateinit var watcher: SearchTextWatcher

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}
	private val cb: () -> Unit = {
		this.onRefresh()
	}

	fun execute(fn: Runnable) {
		fn.run()
		viewModel.refreshPackages(cb)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAppClipboardSettingsBinding.inflate(inflater, container, false)
		watcher = SearchTextWatcher()
		requireActivity().runOnUiThread {
			setup()
		}
		return binding.root
	}

	private fun setup() {
		binding.appSearchBar.inflateMenu(R.menu.config_top_app_bar)
		binding.appSearchBar.setOnMenuItemClickListener {
			when(it.itemId)  {
				R.id.btnRefreshApps -> {
					viewModel.refreshPackages(cb)
					true
				}
				R.id.btnShowSystem -> {
					it.isChecked = !it.isChecked
					viewModel.showSystem.value = it.isChecked
					viewModel.refreshPackages(cb)
					true
				}
				else -> false
			}
		}
		binding.appToggleList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
		val decorator =
			SpacerItemDecoration(
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7F, resources.displayMetrics).toInt()
			)
		binding.appToggleList.addItemDecoration(decorator)
		adapter = ClipboardSettingsListAdapter(requireContext(), viewModel)
		binding.appToggleList.adapter = adapter
		binding.appSearchBar.addTextChangedListener(watcher)
		viewModel.showSystem.observe(viewLifecycleOwner) {
			if(SillyService.getIsBound()) {
				viewModel.refreshPackages()
				adapter.initialList.clear()
			}
		}
	}
	private fun onRefresh() {
		val adapter = binding.appToggleList.adapter as ClipboardSettingsListAdapter
		adapter.notifyDataSetChanged()
	}

	private inner class SearchTextWatcher : TextWatcher {
		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
		}

		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
		}

		override fun afterTextChanged(s: Editable?) {
			val adapter = binding.appToggleList.adapter as ClipboardSettingsListAdapter
			adapter.getFilter().filter(s?.toString() ?: "")
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}