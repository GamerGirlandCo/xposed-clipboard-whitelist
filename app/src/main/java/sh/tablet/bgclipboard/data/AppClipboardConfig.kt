package sh.tablet.bgclipboard.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "app_config")
data class AppClipboardConfig(
    @PrimaryKey var id: String,
    @ColumnInfo(name = "name", collate = ColumnInfo.NOCASE) var name: String,
    @ColumnInfo(name = "enabled") var enabled: Boolean = false,
    @ColumnInfo(name = "is_system") val isSystem: Boolean,
) : Serializable {
	override fun toString(): String {
		return "(id=$id name=$name enabled=$enabled system=$isSystem)"
	}
}
