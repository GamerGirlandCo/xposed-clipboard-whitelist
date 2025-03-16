package sh.tablet.bgclipboard.ui.component.bettersearchbar

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.search.SearchBar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import sh.tablet.bgclipboard.R

class BetterSearchBar :
	SearchBar {
		constructor(ctx: Context) : super(ctx)
	constructor(ctx: Context, attrs: AttributeSet? = null) : super(
		ctx, attrs
	) {

	}

	constructor (ctx: Context, attrs: AttributeSet? = null, dsa: Int) : super(
		ctx, attrs, dsa
	)

	private var isTyping = false
	private var actualView: TextInputLayout
	private var textBox: TextInputEditText?

	init {

		actualView = inflate(context, R.layout.better_search_bar, null).findViewById(R.id.textBoxWrapper)
		textBox = actualView.findViewById(R.id.textBox)
		actualView.hint = hint
		centerView = actualView
		(centerView as TextInputLayout).alpha = 1F
	}


	fun addTextChangedListener(w: TextWatcher) {
		textBox?.addTextChangedListener(w)
	}

	fun removeTextChangedListener(w: TextWatcher) {
		textBox?.removeTextChangedListener(w)
	}

	override fun getText(): CharSequence {
		return textBox?.editableText ?: ""
	}

	override fun setText(text: CharSequence?) {
		text?.let {
			textBox?.text = SpannableStringBuilder(text)
		}
	}

	override fun getHint(): CharSequence? {
		return textBox?.hint
	}

	override fun setHint(hint: CharSequence?) {
		textBox?.hint = hint
	}
}