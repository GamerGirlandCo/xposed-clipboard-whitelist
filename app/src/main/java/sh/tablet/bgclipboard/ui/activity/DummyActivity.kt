package sh.tablet.bgclipboard.ui.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.highcapable.yukihookapi.hook.xposed.parasitic.activity.base.ModuleAppActivity
import com.highcapable.yukihookapi.hook.xposed.parasitic.activity.base.ModuleAppCompatActivity
import sh.tablet.bgclipboard.R

class DummyActivity : ModuleAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
	fun doActualCreation() {
		onCreate(null)
	}
}