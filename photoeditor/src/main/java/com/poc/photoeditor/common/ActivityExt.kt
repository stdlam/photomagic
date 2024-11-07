import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.TransactionTooLargeException
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.poc.photoeditor.provider.ui.base.BaseActivity
import com.simplemobiletools.commons.databinding.DialogTitleBinding
import com.simplemobiletools.commons.dialogs.AppSideloadedDialog
import com.simplemobiletools.commons.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.commons.dialogs.CustomIntervalPickerDialog
import com.simplemobiletools.commons.dialogs.DonateDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.dialogs.SecurityDialog
import com.simplemobiletools.commons.dialogs.UpgradeToProDialog
import com.simplemobiletools.commons.dialogs.WhatsNewDialog
import com.simplemobiletools.commons.dialogs.WritePermissionDialog
import com.simplemobiletools.commons.dialogs.WritePermissionDialog.Mode
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.buildDocumentUriSdk30
import com.simplemobiletools.commons.extensions.canManageMedia
import com.simplemobiletools.commons.extensions.copySingleFileSdk30
import com.simplemobiletools.commons.extensions.createAndroidDataOrObbUri
import com.simplemobiletools.commons.extensions.createAndroidSAFFile
import com.simplemobiletools.commons.extensions.createDocumentUriUsingFirstParentTreeUri
import com.simplemobiletools.commons.extensions.createFirstParentTreeUriUsingRootTree
import com.simplemobiletools.commons.extensions.createSAFFileSdk30
import com.simplemobiletools.commons.extensions.deleteAndroidSAFDirectory
import com.simplemobiletools.commons.extensions.deleteDocumentWithSAFSdk30
import com.simplemobiletools.commons.extensions.deleteFromMediaStore
import com.simplemobiletools.commons.extensions.doesThisOrParentHaveNoMedia
import com.simplemobiletools.commons.extensions.ensurePublicUri
import com.simplemobiletools.commons.extensions.getAndroidSAFUri
import com.simplemobiletools.commons.extensions.getAndroidTreeUri
import com.simplemobiletools.commons.extensions.getAppIconColors
import com.simplemobiletools.commons.extensions.getCanAppBeUpgraded
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.getDocumentFile
import com.simplemobiletools.commons.extensions.getDoesFilePathExist
import com.simplemobiletools.commons.extensions.getFileUrisFromFileDirItems
import com.simplemobiletools.commons.extensions.getFilenameExtension
import com.simplemobiletools.commons.extensions.getFilenameFromPath
import com.simplemobiletools.commons.extensions.getFirstParentLevel
import com.simplemobiletools.commons.extensions.getFirstParentPath
import com.simplemobiletools.commons.extensions.getFormattedSeconds
import com.simplemobiletools.commons.extensions.getGenericMimeType
import com.simplemobiletools.commons.extensions.getInternalStoragePath
import com.simplemobiletools.commons.extensions.getIsPathDirectory
import com.simplemobiletools.commons.extensions.getMimeType
import com.simplemobiletools.commons.extensions.getParentPath
import com.simplemobiletools.commons.extensions.getProperBackgroundColor
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.getSomeDocumentFile
import com.simplemobiletools.commons.extensions.getStoreUrl
import com.simplemobiletools.commons.extensions.getTimePickerDialogTheme
import com.simplemobiletools.commons.extensions.getUriMimeType
import com.simplemobiletools.commons.extensions.hasProperStoredAndroidTreeUri
import com.simplemobiletools.commons.extensions.hasProperStoredDocumentUriSdk30
import com.simplemobiletools.commons.extensions.hasProperStoredFirstParentUri
import com.simplemobiletools.commons.extensions.hasProperStoredTreeUri
import com.simplemobiletools.commons.extensions.internalStoragePath
import com.simplemobiletools.commons.extensions.isAProApp
import com.simplemobiletools.commons.extensions.isAccessibleWithSAFSdk30
import com.simplemobiletools.commons.extensions.isBlackAndWhiteTheme
import com.simplemobiletools.commons.extensions.isDefaultDialer
import com.simplemobiletools.commons.extensions.isMediaFile
import com.simplemobiletools.commons.extensions.isOrWasThankYouInstalled
import com.simplemobiletools.commons.extensions.isPathOnInternalStorage
import com.simplemobiletools.commons.extensions.isPathOnOTG
import com.simplemobiletools.commons.extensions.isPathOnSD
import com.simplemobiletools.commons.extensions.isRecycleBinPath
import com.simplemobiletools.commons.extensions.isRestrictedSAFOnlyRoot
import com.simplemobiletools.commons.extensions.isRestrictedWithSAFSdk30
import com.simplemobiletools.commons.extensions.isSDCardSetAsDefaultStorage
import com.simplemobiletools.commons.extensions.launchActivityIntent
import com.simplemobiletools.commons.extensions.needsStupidWritePermissions
import com.simplemobiletools.commons.extensions.renameAndroidSAFDocument
import com.simplemobiletools.commons.extensions.renameDocumentSdk30
import com.simplemobiletools.commons.extensions.rescanAndDeletePath
import com.simplemobiletools.commons.extensions.rescanPath
import com.simplemobiletools.commons.extensions.rescanPaths
import com.simplemobiletools.commons.extensions.scanFileRecursively
import com.simplemobiletools.commons.extensions.scanFilesRecursively
import com.simplemobiletools.commons.extensions.scanPathRecursively
import com.simplemobiletools.commons.extensions.scanPathsRecursively
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.showFileCreateError
import com.simplemobiletools.commons.extensions.toFileDirItem
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.toggleAppIconColor
import com.simplemobiletools.commons.extensions.trySAFFileDelete
import com.simplemobiletools.commons.extensions.updateInMediaStore
import com.simplemobiletools.commons.extensions.updateLastModified
import com.simplemobiletools.commons.extensions.updateSDCardPath
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.CREATE_DOCUMENT_SDK_30
import com.simplemobiletools.commons.helpers.EXTRA_SHOW_ADVANCED
import com.simplemobiletools.commons.helpers.IS_FROM_GALLERY
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import com.simplemobiletools.commons.helpers.MyContentProvider
import com.simplemobiletools.commons.helpers.OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
import com.simplemobiletools.commons.helpers.OPEN_DOCUMENT_TREE_FOR_SDK_30
import com.simplemobiletools.commons.helpers.OPEN_DOCUMENT_TREE_OTG
import com.simplemobiletools.commons.helpers.OPEN_DOCUMENT_TREE_SD
import com.simplemobiletools.commons.helpers.PERMISSION_CALL_PHONE
import com.simplemobiletools.commons.helpers.PERMISSION_READ_STORAGE
import com.simplemobiletools.commons.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.commons.helpers.REAL_FILE_PATH
import com.simplemobiletools.commons.helpers.REQUEST_EDIT_IMAGE
import com.simplemobiletools.commons.helpers.REQUEST_SET_AS
import com.simplemobiletools.commons.helpers.SIDELOADING_FALSE
import com.simplemobiletools.commons.helpers.SIDELOADING_TRUE
import com.simplemobiletools.commons.helpers.SILENT
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isOnMainThread
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.commons.models.AlarmSound
import com.simplemobiletools.commons.models.Android30RenameFormat
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.commons.models.SharedTheme
import com.simplemobiletools.commons.views.MyTextView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.TreeSet
import com.simplemobiletools.commons.R

fun Activity.showDonateOrUpgradeDialog() {
    if (getCanAppBeUpgraded()) {
        UpgradeToProDialog(this)
    } else if (!isOrWasThankYouInstalled()) {
        DonateDialog(this)
    }
}

fun Activity.isAppInstalledOnSDCard(): Boolean = try {
    val applicationInfo = packageManager.getPackageInfo(packageName, 0).applicationInfo
    (applicationInfo.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE
} catch (e: Exception) {
    false
}

fun BaseActivity.isShowingSAFDialog(path: String): Boolean {
    return if ((!isRPlus() && isPathOnSD(path) && !isSDCardSetAsDefaultStorage() && (baseConfig.sdTreeUri.isEmpty() || !hasProperStoredTreeUri(false)))) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                WritePermissionDialog(this, Mode.SdCard) {
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

@SuppressLint("InlinedApi")
fun BaseActivity.isShowingSAFDialogSdk30(path: String): Boolean {
    return if (isAccessibleWithSAFSdk30(path) && !hasProperStoredFirstParentUri(path)) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                val level = getFirstParentLevel(path)
                WritePermissionDialog(this, Mode.OpenDocumentTreeSDK30(path.getFirstParentPath(this, level))) {
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, createFirstParentTreeUriUsingRootTree(path))
                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

@SuppressLint("InlinedApi")
fun BaseActivity.isShowingSAFCreateDocumentDialogSdk30(path: String): Boolean {
    return if (!hasProperStoredDocumentUriSdk30(path)) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                WritePermissionDialog(this, Mode.CreateDocumentSDK30) {
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        type = DocumentsContract.Document.MIME_TYPE_DIR
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, buildDocumentUriSdk30(path.getParentPath()))
                        putExtra(Intent.EXTRA_TITLE, path.getFilenameFromPath())
                        try {
                            startActivityForResult(this, CREATE_DOCUMENT_SDK_30)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, CREATE_DOCUMENT_SDK_30)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

fun BaseActivity.isShowingAndroidSAFDialog(path: String): Boolean {
    return if (isRestrictedSAFOnlyRoot(path) && (getAndroidTreeUri(path).isEmpty() || !hasProperStoredAndroidTreeUri(path))) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                ConfirmationAdvancedDialog(this, "", R.string.confirm_storage_access_android_text, R.string.ok, R.string.cancel) { success ->
                    if (success) {
                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            putExtra(EXTRA_SHOW_ADVANCED, true)
                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, createAndroidDataOrObbUri(path))
                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB)
                                checkedDocumentPath = path
                                return@apply
                            } catch (e: Exception) {
                                type = "*/*"
                            }

                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB)
                                checkedDocumentPath = path
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                            } catch (e: Exception) {
                                toast(R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

fun BaseActivity.isShowingOTGDialog(path: String): Boolean {
    return if (!isRPlus() && isPathOnOTG(path) && (baseConfig.OTGTreeUri.isEmpty() || !hasProperStoredTreeUri(true))) {
        showOTGPermissionDialog(path)
        true
    } else {
        false
    }
}

fun BaseActivity.showOTGPermissionDialog(path: String) {
    runOnUiThread {
        if (!isDestroyed && !isFinishing) {
            WritePermissionDialog(this, Mode.Otg) {
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    try {
                        startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                        checkedDocumentPath = path
                        return@apply
                    } catch (e: Exception) {
                        type = "*/*"
                    }

                    try {
                        startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                        checkedDocumentPath = path
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }
}

fun Activity.launchViewIntent(url: String) {
    hideKeyboard()
    ensureBackgroundThread {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            try {
                startActivity(this)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_browser_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.sharePathIntent(path: String, applicationId: String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(EXTRA_STREAM, newUri)
            type = getUriMimeType(path, newUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            grantUriPermission("android", newUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                startActivity(Intent.createChooser(this, getString(R.string.share_via)))
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: RuntimeException) {
                if (e.cause is TransactionTooLargeException) {
                    toast(R.string.maximum_share_reached)
                } else {
                    showErrorToast(e)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.sharePathsIntent(paths: List<String>, applicationId: String) {
    ensureBackgroundThread {
        if (paths.size == 1) {
            sharePathIntent(paths.first(), applicationId)
        } else {
            val uriPaths = ArrayList<String>()
            val newUris = paths.map {
                val uri = getFinalUriFromPath(it, applicationId) ?: return@ensureBackgroundThread
                uriPaths.add(uri.path!!)
                uri
            } as ArrayList<Uri>

            var mimeType = uriPaths.getMimeType()
            if (mimeType.isEmpty() || mimeType == "*/*") {
                mimeType = paths.getMimeType()
            }

            Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putParcelableArrayListExtra(EXTRA_STREAM, newUris)

                try {
                    startActivity(Intent.createChooser(this, getString(R.string.share_via)))
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: RuntimeException) {
                    if (e.cause is TransactionTooLargeException) {
                        toast(R.string.maximum_share_reached)
                    } else {
                        showErrorToast(e)
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }
}

fun Activity.setAsIntent(path: String, applicationId: String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        Intent().apply {
            action = Intent.ACTION_ATTACH_DATA
            setDataAndType(newUri, getUriMimeType(path, newUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(this, getString(R.string.set_as))

            try {
                startActivityForResult(chooser, REQUEST_SET_AS)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.shareTextIntent(text: String) {
    ensureBackgroundThread {
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)

            try {
                startActivity(Intent.createChooser(this, getString(R.string.share_via)))
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: RuntimeException) {
                if (e.cause is TransactionTooLargeException) {
                    toast(R.string.maximum_share_reached)
                } else {
                    showErrorToast(e)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.openEditorIntent(path: String, forceChooser: Boolean, applicationId: String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        Intent().apply {
            action = Intent.ACTION_EDIT
            setDataAndType(newUri, getUriMimeType(path, newUri))
            if (!isRPlus() || (isRPlus() && (hasProperStoredDocumentUriSdk30(path) || Environment.isExternalStorageManager()))) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val parent = path.getParentPath()
            val newFilename = "${path.getFilenameFromPath().substringBeforeLast('.')}_1"
            val extension = path.getFilenameExtension()
            val newFilePath = File(parent, "$newFilename.$extension")

            val outputUri = if (isPathOnOTG(path)) newUri else getFinalUriFromPath("$newFilePath", applicationId)
            if (!isRPlus()) {
                val resInfoList = packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            if (!isRPlus()) {
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            }

            putExtra(REAL_FILE_PATH, path)

            try {
                val chooser = Intent.createChooser(this, getString(R.string.edit_with))
                startActivityForResult(if (forceChooser) chooser else this, REQUEST_EDIT_IMAGE)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.openPathIntent(
    path: String,
    forceChooser: Boolean,
    applicationId: String,
    forceMimeType: String = "",
    extras: HashMap<String, Boolean> = HashMap()
) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        val mimeType = if (forceMimeType.isNotEmpty()) forceMimeType else getUriMimeType(path, newUri)
        Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(newUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (applicationId == "com.simplemobiletools.gallery.pro" || applicationId == "com.simplemobiletools.gallery.pro.debug") {
                putExtra(IS_FROM_GALLERY, true)
            }

            for ((key, value) in extras) {
                putExtra(key, value)
            }

            putExtra(REAL_FILE_PATH, path)

            try {
                val chooser = Intent.createChooser(this, getString(R.string.open_with))
                startActivity(if (forceChooser) chooser else this)
            } catch (e: ActivityNotFoundException) {
                if (!tryGenericMimeType(this, mimeType, newUri)) {
                    toast(R.string.no_app_found)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.launchViewContactIntent(uri: Uri) {
    Intent().apply {
        action = ContactsContract.QuickContact.ACTION_QUICK_CONTACT
        data = uri
        launchActivityIntent(this)
    }
}

fun BaseActivity.launchCallIntent(recipient: String, handle: PhoneAccountHandle? = null) {
    handlePermission(PERMISSION_CALL_PHONE) {
        val action = if (it) Intent.ACTION_CALL else Intent.ACTION_DIAL
        Intent(action).apply {
            data = Uri.fromParts("tel", recipient, null)

            if (handle != null) {
                putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
            }

            if (isDefaultDialer()) {
                val packageName = if (baseConfig.appId.contains(".debug", true)) "com.simplemobiletools.dialer.debug" else "com.simplemobiletools.dialer"
                val className = "com.simplemobiletools.dialer.activities.DialerActivity"
                setClassName(packageName, className)
            }

            launchActivityIntent(this)
        }
    }
}

fun Activity.launchSendSMSIntent(recipient: String) {
    Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.fromParts("smsto", recipient, null)
        launchActivityIntent(this)
    }
}

fun Activity.showLocationOnMap(coordinates: String) {
    val uriBegin = "geo:${coordinates.replace(" ", "")}"
    val encodedQuery = Uri.encode(coordinates)
    val uriString = "$uriBegin?q=$encodedQuery&z=16"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    launchActivityIntent(intent)
}

fun Activity.getFinalUriFromPath(path: String, applicationId: String): Uri? {
    val uri = try {
        ensurePublicUri(path, applicationId)
    } catch (e: Exception) {
        showErrorToast(e)
        return null
    }

    if (uri == null) {
        toast(R.string.unknown_error_occurred)
        return null
    }

    return uri
}

fun Activity.tryGenericMimeType(intent: Intent, mimeType: String, uri: Uri): Boolean {
    var genericMimeType = mimeType.getGenericMimeType()
    if (genericMimeType.isEmpty()) {
        genericMimeType = "*/*"
    }

    intent.setDataAndType(uri, genericMimeType)

    return try {
        startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

fun BaseActivity.checkWhatsNew(releases: List<Release>, currVersion: Int) {
    if (baseConfig.lastVersion == 0) {
        baseConfig.lastVersion = currVersion
        return
    }

    val newReleases = arrayListOf<Release>()
    releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

    if (newReleases.isNotEmpty()) {
        WhatsNewDialog(this, newReleases)
    }

    baseConfig.lastVersion = currVersion
}

fun Activity.scanFileRecursively(file: File, callback: (() -> Unit)? = null) {
    applicationContext.scanFileRecursively(file, callback)
}

fun Activity.scanPathRecursively(path: String, callback: (() -> Unit)? = null) {
    applicationContext.scanPathRecursively(path, callback)
}

fun Activity.scanFilesRecursively(files: List<File>, callback: (() -> Unit)? = null) {
    applicationContext.scanFilesRecursively(files, callback)
}

fun Activity.scanPathsRecursively(paths: List<String>, callback: (() -> Unit)? = null) {
    applicationContext.scanPathsRecursively(paths, callback)
}

fun Activity.rescanPath(path: String, callback: (() -> Unit)? = null) {
    applicationContext.rescanPath(path, callback)
}


fun Activity.createTempFile(file: File): File? {
    return if (file.isDirectory) {
        createTempDir("temp", "${System.currentTimeMillis()}", file.parentFile)
    } else {
        if (isRPlus()) {
            // this can throw FileSystemException, lets catch and handle it at the place calling this function
            kotlin.io.path.createTempFile(file.parentFile.toPath(), "temp", "${System.currentTimeMillis()}").toFile()
        } else {
            createTempFile("temp", "${System.currentTimeMillis()}", file.parentFile)
        }
    }
}

fun Activity.hideKeyboard() {
    if (isOnMainThread()) {
        hideKeyboardSync()
    } else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync()
        }
    }
}

fun Activity.hideKeyboardSync() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.showKeyboard(et: EditText) {
    et.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun AppCompatActivity.getFileOutputStream(fileDirItem: FileDirItem, allowCreatingNewFile: Boolean = false, callback: (outputStream: OutputStream?) -> Unit) {
    val targetFile = File(fileDirItem.path)
    when {
        isRestrictedWithSAFSdk30(fileDirItem.path) -> {
            callback.invoke(
                try {
                    val fileUri = getFileUrisFromFileDirItems(arrayListOf(fileDirItem))
                    applicationContext.contentResolver.openOutputStream(fileUri.first(), "wt")
                } catch (e: Exception) {
                    null
                } ?: createCasualFileOutputStream(this, targetFile)
            )
        }
        else -> {
            callback.invoke(createCasualFileOutputStream(this, targetFile))
        }
    }
}

private fun createCasualFileOutputStream(activity: AppCompatActivity, targetFile: File): OutputStream? {
    if (targetFile.parentFile?.exists() == false) {
        targetFile.parentFile?.mkdirs()
    }

    return try {
        FileOutputStream(targetFile)
    } catch (e: Exception) {
        activity.showErrorToast(e)
        null
    }
}

fun Activity.handleHiddenFolderPasswordProtection(callback: () -> Unit) {
    if (baseConfig.isHiddenPasswordProtectionOn) {
        SecurityDialog(this, baseConfig.hiddenPasswordHash, baseConfig.hiddenProtectionType) { _, _, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

fun Activity.handleAppPasswordProtection(callback: (success: Boolean) -> Unit) {
    if (baseConfig.isAppPasswordProtectionOn) {
        SecurityDialog(this, baseConfig.appPasswordHash, baseConfig.appProtectionType) { _, _, success ->
            callback(success)
        }
    } else {
        callback(true)
    }
}

fun Activity.handleDeletePasswordProtection(callback: () -> Unit) {
    if (baseConfig.isDeletePasswordProtectionOn) {
        SecurityDialog(this, baseConfig.deletePasswordHash, baseConfig.deleteProtectionType) { _, _, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

fun Activity.handleLockedFolderOpening(path: String, callback: (success: Boolean) -> Unit) {
    if (baseConfig.isFolderProtected(path)) {
        SecurityDialog(this, baseConfig.getFolderProtectionHash(path), baseConfig.getFolderProtectionType(path)) { _, _, success ->
            callback(success)
        }
    } else {
        callback(true)
    }
}

fun Activity.updateSharedTheme(sharedTheme: SharedTheme) {
    try {
        val contentValues = MyContentProvider.fillThemeContentValues(sharedTheme)
        applicationContext.contentResolver.update(MyContentProvider.MY_CONTENT_URI, contentValues, null, null)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Activity.setupDialogStuff(
    view: View,
    dialog: AlertDialog.Builder,
    titleId: Int = 0,
    titleText: String = "",
    cancelOnTouchOutside: Boolean = true,
    callback: ((alertDialog: AlertDialog) -> Unit)? = null
) {
    if (isDestroyed || isFinishing) {
        return
    }

    val textColor = getProperTextColor()
    val backgroundColor = getProperBackgroundColor()
    val primaryColor = getProperPrimaryColor()
    if (view is ViewGroup) {
        updateTextColors(view)
    } else if (view is MyTextView) {
        view.setColors(textColor, primaryColor, backgroundColor)
    }

    if (dialog is MaterialAlertDialogBuilder) {
        dialog.create().apply {
            if (titleId != 0) {
                setTitle(titleId)
            } else if (titleText.isNotEmpty()) {
                setTitle(titleText)
            }

            setView(view)
            setCancelable(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
            getButton(Dialog.BUTTON_POSITIVE)?.setTextColor(primaryColor)
            getButton(Dialog.BUTTON_NEGATIVE)?.setTextColor(primaryColor)
            getButton(Dialog.BUTTON_NEUTRAL)?.setTextColor(primaryColor)
            callback?.invoke(this)
        }
    } else {
        var title: DialogTitleBinding? = null
        if (titleId != 0 || titleText.isNotEmpty()) {
            title = DialogTitleBinding.inflate(layoutInflater, null, false)
            title.dialogTitleTextview.apply {
                if (titleText.isNotEmpty()) {
                    text = titleText
                } else {
                    setText(titleId)
                }
                setTextColor(textColor)
            }
        }

        // if we use the same primary and background color, use the text color for dialog confirmation buttons
        val dialogButtonColor = if (primaryColor == baseConfig.backgroundColor) {
            textColor
        } else {
            primaryColor
        }

        dialog.create().apply {
            setView(view)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCustomTitle(title?.root)
            setCanceledOnTouchOutside(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(dialogButtonColor)

            val bgDrawable = when {
                isBlackAndWhiteTheme() -> resources.getDrawable(R.drawable.black_dialog_background, theme)
                baseConfig.isUsingSystemTheme -> resources.getDrawable(R.drawable.dialog_you_background, theme)
                else -> resources.getColoredDrawableWithColor(R.drawable.dialog_bg, baseConfig.backgroundColor)
            }

            window?.setBackgroundDrawable(bgDrawable)
            callback?.invoke(this)
        }
    }
}

fun Activity.getAlertDialogBuilder() = if (baseConfig.isUsingSystemTheme) {
    MaterialAlertDialogBuilder(this)
} else {
    AlertDialog.Builder(this)
}

fun Activity.showPickSecondsDialogHelper(
    curMinutes: Int, isSnoozePicker: Boolean = false, showSecondsAtCustomDialog: Boolean = false, showDuringDayOption: Boolean = false,
    cancelCallback: (() -> Unit)? = null, callback: (seconds: Int) -> Unit
) {
    val seconds = if (curMinutes == -1) curMinutes else curMinutes * 60
    showPickSecondsDialog(seconds, isSnoozePicker, showSecondsAtCustomDialog, showDuringDayOption, cancelCallback, callback)
}

fun Activity.showPickSecondsDialog(
    curSeconds: Int, isSnoozePicker: Boolean = false, showSecondsAtCustomDialog: Boolean = false, showDuringDayOption: Boolean = false,
    cancelCallback: (() -> Unit)? = null, callback: (seconds: Int) -> Unit
) {
    hideKeyboard()
    val seconds = TreeSet<Int>()
    seconds.apply {
        if (!isSnoozePicker) {
            add(-1)
            add(0)
        }
        add(1 * MINUTE_SECONDS)
        add(5 * MINUTE_SECONDS)
        add(10 * MINUTE_SECONDS)
        add(30 * MINUTE_SECONDS)
        add(60 * MINUTE_SECONDS)
        add(curSeconds)
    }

    val items = ArrayList<RadioItem>(seconds.size + 1)
    seconds.mapIndexedTo(items) { index, value ->
        RadioItem(index, getFormattedSeconds(value, !isSnoozePicker), value)
    }

    var selectedIndex = 0
    seconds.forEachIndexed { index, value ->
        if (value == curSeconds) {
            selectedIndex = index
        }
    }

    items.add(RadioItem(-2, getString(R.string.custom)))

    if (showDuringDayOption) {
        items.add(RadioItem(-3, getString(R.string.during_day_at_hh_mm)))
    }

    RadioGroupDialog(this, items, selectedIndex, showOKButton = isSnoozePicker, cancelCallback = cancelCallback) {
        when (it) {
            -2 -> {
                CustomIntervalPickerDialog(this, showSeconds = showSecondsAtCustomDialog) {
                    callback(it)
                }
            }
            -3 -> {
                TimePickerDialog(
                    this, getTimePickerDialogTheme(),
                    { view, hourOfDay, minute -> callback(hourOfDay * -3600 + minute * -60) },
                    curSeconds / 3600, curSeconds % 3600, baseConfig.use24HourFormat
                ).show()
            }
            else -> {
                callback(it as Int)
            }
        }
    }
}

fun BaseActivity.getAlarmSounds(type: Int, callback: (ArrayList<AlarmSound>) -> Unit) {
    val alarms = ArrayList<AlarmSound>()
    val manager = RingtoneManager(this)
    manager.setType(type)

    try {
        val cursor = manager.cursor
        var curId = 1
        val silentAlarm = AlarmSound(curId++, getString(R.string.no_sound), SILENT)
        alarms.add(silentAlarm)

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            var uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
            val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            if (!uri.endsWith(id)) {
                uri += "/$id"
            }

            val alarmSound = AlarmSound(curId++, title, uri)
            alarms.add(alarmSound)
        }
        callback(alarms)
    } catch (e: Exception) {
        if (e is SecurityException) {
            handlePermission(PERMISSION_READ_STORAGE) {
                if (it) {
                    getAlarmSounds(type, callback)
                } else {
                    showErrorToast(e)
                    callback(ArrayList())
                }
            }
        } else {
            showErrorToast(e)
            callback(ArrayList())
        }
    }
}

fun Activity.checkAppSideloading(): Boolean {
    val isSideloaded = when (baseConfig.appSideloadingStatus) {
        SIDELOADING_TRUE -> true
        SIDELOADING_FALSE -> false
        else -> isAppSideloaded()
    }

    baseConfig.appSideloadingStatus = if (isSideloaded) SIDELOADING_TRUE else SIDELOADING_FALSE
    if (isSideloaded) {
        showSideloadingDialog()
    }

    return isSideloaded
}

fun Activity.isAppSideloaded(): Boolean {
    return try {
        getDrawable(R.drawable.ic_camera_vector)
        false
    } catch (e: Exception) {
        true
    }
}

fun Activity.showSideloadingDialog() {
    AppSideloadedDialog(this) {
        finish()
    }
}

fun Activity.onApplyWindowInsets(callback: (WindowInsetsCompat) -> Unit) {
    window.decorView.setOnApplyWindowInsetsListener { view, insets ->
        callback(WindowInsetsCompat.toWindowInsetsCompat(insets))
        view.onApplyWindowInsets(insets)
        insets
    }
}