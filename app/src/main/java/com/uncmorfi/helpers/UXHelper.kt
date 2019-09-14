package com.uncmorfi.helpers

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.BaseDialogHelper
import com.uncmorfi.models.User

enum class SnackType {
    ERROR,
    LOADING,
    FINISH
}

enum class ReserveStatus {
    RESERVING,
    RESERVED,
    UNAVAILABLE,
    SOLDOUT,
    INVALID,
    REDOLOGIN,
}

enum class StatusCode {
    NO_ONLINE,
    CONNECT_ERROR,
    INTERNAL_ERROR,
    UPDATE_ERROR,

    UPDATING,
    UPDATE_SUCCESS,
    EMPTY_UPDATE,
    ALREADY_UPDATED,

    COPIED,
    INSERTED,
    DELETED,

    BUSY
}

fun View.snack(context: Context?, resId: Int, type: SnackType): Snackbar {
    val bar = Snackbar.make(this, resId, getLength(type))
    context?.let { setColored(it, bar, type) }
    bar.show()
    return bar
}

fun View.snack(context: Context?, msg: String, type: SnackType): Snackbar {
    val bar = Snackbar.make(this, msg, getLength(type))
    context?.let { setColored(it, bar, type) }
    bar.show()
    return bar
}

fun View.snack(context: Context?, code: StatusCode): Snackbar {
    val bar = Snackbar.make(this, getMsg(code), getLength(getType(code)))
    context?.let { setColored(it, bar, getType(code)) }
    bar.show()
    return bar
}

private fun getMsg(code: StatusCode): Int {
    return when (code) {
        StatusCode.NO_ONLINE -> R.string.snack_no_online
        StatusCode.CONNECT_ERROR -> R.string.snack_connect_error
        StatusCode.INTERNAL_ERROR -> R.string.snack_internal_error
        StatusCode.UPDATE_ERROR -> R.string.snack_update_error

        StatusCode.UPDATING -> R.string.snack_updating
        StatusCode.UPDATE_SUCCESS -> R.string.snack_update_success
        StatusCode.EMPTY_UPDATE -> R.string.snack_empty_update
        StatusCode.ALREADY_UPDATED -> R.string.snack_already_updated

        StatusCode.COPIED -> R.string.snack_copied

        else -> R.string.snack_error
    }
}

private fun getType(code: StatusCode): SnackType {
    return when (code) {
        StatusCode.NO_ONLINE,
        StatusCode.CONNECT_ERROR,
        StatusCode.INTERNAL_ERROR,
        StatusCode.UPDATE_ERROR -> SnackType.ERROR

        StatusCode.UPDATING -> SnackType.LOADING

        StatusCode.UPDATE_SUCCESS,
        StatusCode.EMPTY_UPDATE,
        StatusCode.ALREADY_UPDATED,
        StatusCode.COPIED -> SnackType.FINISH

        else -> SnackType.ERROR
    }
}

private fun getLength(type: SnackType): Int {
    return when (type) {
        SnackType.ERROR -> Snackbar.LENGTH_INDEFINITE
        SnackType.LOADING -> Snackbar.LENGTH_INDEFINITE
        SnackType.FINISH -> Snackbar.LENGTH_SHORT
    }
}

private fun setColored(context: Context, snackBar: Snackbar, type: SnackType) {
    val color = when (type) {
        SnackType.ERROR -> context.colorOf(R.color.accent)
        SnackType.LOADING -> context.colorOf(R.color.primary_text)
        SnackType.FINISH -> context.colorOf(R.color.primary_dark)
    }
    snackBar.view.setBackgroundColor(color)
}

fun SwipeRefreshLayout.init(f: () -> Unit) {
    this.setOnRefreshListener { f() }
    this.setProgressBackgroundColorSchemeResource(R.color.accent)
    this.setColorSchemeResources(R.color.white)
}

fun Context.colorOf(resId: Int) : Int {
    return ContextCompat.getColor(this, resId)
}

// Devuelvo true porque lo uso en los onOptionsItemSelected() de los fragments
fun Activity.startBrowser(uri: String) : Boolean {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    return true
}

// https://stackoverflow.com/a/17286547
fun Activity.openFacebook() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1978631742388364")))
    } catch (e: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/UNCmorfi")))
    }
}

fun Activity.shareText(subject: String, text: String, title: String = "UNCmorfi") : Boolean {
    val i = Intent(Intent.ACTION_SEND)
    i.type = "text/plain"
    i.putExtra(Intent.EXTRA_SUBJECT, subject)
    i.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(i, title))
    return true
}

fun Activity.sendEmail(to: String, subject: Int, text: Int) {
    val i = Intent(Intent.ACTION_SENDTO)
    i.data = Uri.parse("mailto:")
    i.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
    i.putExtra(Intent.EXTRA_SUBJECT, getString(subject))
    i.putExtra(Intent.EXTRA_TEXT, getString(text))

    if (i.resolveActivity(packageManager) != null)
        startActivity(i)
}

fun Activity.hideKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Context.copyToClipboard(label: String, data: String) {
    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip = ClipData.newPlainText(label, data)
}

fun EditText.onTextChanged(onTextChanged: (CharSequence) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
            onTextChanged.invoke(s)
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

fun Intent?.getUser() : User {
    return this?.getSerializableExtra(BaseDialogHelper.ARG_USER) as User
}

fun TextView.updateVisibility() {
    this.visibility = if (this.text.isNullOrEmpty()) GONE else VISIBLE
}