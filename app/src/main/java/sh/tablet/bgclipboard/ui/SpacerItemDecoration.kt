package sh.tablet.bgclipboard.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacerItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

	@Override
	public override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		if (parent.adapter != null) {
			if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
				outRect.bottom = verticalSpaceHeight
			}
		}
	}
}