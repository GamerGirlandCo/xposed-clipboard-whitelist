@file:SuppressLint("NotifyDataSetChanged")

package sh.tablet.bgclipboard.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sh.tablet.bgclipboard.databinding.AppClipboardSettingsItemBinding
import sh.tablet.bgclipboard.data.AppClipboardConfig
import sh.tablet.bgclipboard.data.AppConfigDatabase
import sh.tablet.bgclipboard.ui.viewmodel.AppClipboardSettingsViewModel
import java.util.Locale

class ClipboardSettingsListAdapter(val ctx: Context, val vm: AppClipboardSettingsViewModel) :
	RecyclerView.Adapter<ClipboardSettingsListAdapter.ViewHolder>() {
	init {

	}

	val initialList = ArrayList<AppClipboardConfig>()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding = AppClipboardSettingsItemBinding.inflate(inflater, parent, false)
		return ViewHolder(binding, ctx, vm)
	}

	override fun getItemCount(): Int = vm.packages.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(vm.packages[position], position)
	}

	fun getFilter(): Filter = appFilter

	private val appFilter = object : Filter() {
		override fun performFiltering(constraint: CharSequence?): FilterResults {
			if (initialList.isEmpty())
				initialList.addAll(vm.packages)
			val filtered = ArrayList<AppClipboardConfig>()
			if (constraint.isNullOrEmpty()) {
				filtered.addAll(initialList)
			} else {
				val query = constraint.toString().trim().lowercase()
				initialList.forEach {
					if (it.name.lowercase(Locale.ROOT).contains(query)) {
						filtered.add(it)
					}
				}
			}
			val results = FilterResults()
			results.values = filtered
			return results
		}

		@Suppress("unchecked_cast")
		override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
			if (results?.values is ArrayList<*>) {
				val rv = results.values as ArrayList<AppClipboardConfig>
				vm.packages.clear()
				vm.packages.addAll(rv)
				notifyDataSetChanged()
			}
		}
	}

	class ViewHolder(
		private val binding: AppClipboardSettingsItemBinding,
		private val ctx: Context,
		private val vm: AppClipboardSettingsViewModel
	) :
		RecyclerView.ViewHolder(binding.root) {
		private lateinit var app: AppClipboardConfig
		private var position: Int? = null
		fun bind(a: AppClipboardConfig, pos: Int) {
			app = a
			position = pos
			binding.toggleSwitch.isChecked = a.enabled
			binding.appName.text = a.name
			binding.appId.text = a.id
			binding.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
				app.enabled = isChecked
				vm.viewModelScope.launch {
					val db = AppConfigDatabase.get(ctx).configDao()
					db.updateOne(app)
				}
			}
			try {
				binding.appIcon.setImageDrawable(ctx.packageManager.getApplicationIcon(a.id))
			} catch (e: PackageManager.NameNotFoundException) {
				// should never happen tbh...
				e.printStackTrace()
			}
		}
	}
}