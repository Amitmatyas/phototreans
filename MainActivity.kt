package com.example.phototransfer
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
class MainActivity : AppCompatActivity() {
private lateinit var btnTransfer: Button
private lateinit var btnSetup: Button
private lateinit var statusTextView: TextView
private val selectFolderLauncher = registerForActivityResult(
ActivityResultContracts.StartActivityForResult()
) { result ->
if (result.resultCode == Activity.RESULT_OK) {
val treeUri: Uri? = result.data?.data
if (treeUri != null) {
contentResolver.takePersistableUriPermission(
treeUri,
Intent.FLAG_GRANT_READ_URI_PERMISSION
)
saveSourceUri(treeUri)
". ! מעביר התמונות ש" = statusTextView.text
}
}
}
override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_main)
btnTransfer = findViewById(R.id.btnTransfer)
btnSetup = findViewById(R.id.btnSetup)
statusTextView = findViewById(R.id.statusTextView)
val savedUri = getSavedSourceUri()
if (savedUri != null) {
statusTextView.text = " ! -USB ."
} else {
".מעביר התמונות ש" = statusTextView.text
}
}
btnSetup.setOnClickListener {
val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
selectFolderLauncher.launch(intent)
btnTransfer.setOnClickListener {
val uri = getSavedSourceUri()
if (uri == null) {
} else {
startTransfer(uri)
Toast.makeText(this, "מעביר התמונות שמעביר התמונות שמעביר התמונות ש", Toast.LENGTH_LONG).show()
}
}
}
private fun startTransfer(treeUri: Uri) {
"!מעביר התמונות ש... מעביר התמונות ש" = statusTextView.text
btnTransfer.isEnabled = false
CoroutineScope(Dispatchers.IO).launch {
var counter = 0
val root = DocumentFile.fromTreeUri(this@MainActivity, treeUri)
if (root != null && root.isDirectory) {
root.listFiles().forEach { file ->
if (file.isFile && isImage(file.name)) {
if (copyFile(file)) counter++
}
}
}
withContext(Dispatchers.Main) {
btnTransfer.isEnabled = true
statusTextView.text = " !מעביר התמונות שמעביר התמונות ש $counter ."
Toast.makeText(this@MainActivity, " !מעביר התמונות שמעביר התמונות ש $counter ",
Toast.LENGTH_LONG).show()
}
}
}
private fun copyFile(source: DocumentFile): Boolean {
val values = ContentValues().apply {
put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
put(MediaStore.MediaColumns.MIME_TYPE, source.type)
put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Imported")
}
val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
values) ?: return false
return try {
val ins = contentResolver.openInputStream(source.uri)
val outs = contentResolver.openOutputStream(uri)
if (ins != null && outs != null) {
ins.copyTo(outs)
true
} else false
} catch (e: Exception) { false }
}
private fun isImage(name: String?): Boolean {
val ext = name?.lowercase() ?: return false
return ext.endsWith(".jpg") || ext.endsWith(".jpeg") || ext.endsWith(".png")
}
private fun saveSourceUri(uri: Uri) {
getPreferences(Context.MODE_PRIVATE).edit().putString("uri",
uri.toString()).apply()
}
private fun getSavedSourceUri(): Uri? {
val s = getPreferences(Context.MODE_PRIVATE).getString("uri", null) ?: return null
return Uri.parse(s)
}
}
