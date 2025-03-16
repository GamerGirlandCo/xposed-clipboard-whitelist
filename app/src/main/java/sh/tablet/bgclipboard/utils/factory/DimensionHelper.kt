package sh.tablet.bgclipboard.utils.factory

import android.content.Context

fun Float.toAbsInt(ctx: Context): Int {
	val scale = ctx.resources.displayMetrics.density
	return (this * scale + 0.5f).toInt()
}