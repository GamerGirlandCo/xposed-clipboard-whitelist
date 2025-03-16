package sh.tablet.bgclipboard.utils.root

object Native {
	external fun seteuid(uid: Int): Boolean
	external fun setegid(gid: Int): Boolean
	init {
	    System.loadLibrary("setxuid")
	}
}