package com.uncmorfi.shared

import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.uncmorfi.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class SnackType {
    ERROR,
    LOADING,
    FINISH
}

enum class ReserveStatus {
    RESERVING,
    CONSULTING,
    RESERVED,
    UNAVAILABLE,
    SOLDOUT,
    INVALID,
    REDOLOGIN,
    CACHED,
    NOCACHED,
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

    USER_INSERTED,
    USER_DELETED,
    USER_NOT_FOUND,

    BUSY
}

fun View.visible(show: Boolean) {
    visibility = if (show) VISIBLE else GONE
}

fun View.invisible(isInvisible: Boolean) {
    visibility = if (isInvisible) INVISIBLE else VISIBLE
}

fun View.snack(resId: Int, type: SnackType): Snackbar {
    val bar = Snackbar.make(this, resId, getLength(type))
    setColored(this.context, bar, type)
    bar.show()
    return bar
}

fun View.snack(msg: String, type: SnackType): Snackbar {
    val bar = Snackbar.make(this, msg, getLength(type))
    setColored(this.context, bar, type)
    bar.show()
    return bar
}

fun View.snack(code: StatusCode?): Snackbar? {
    return when (code) {
        StatusCode.NO_ONLINE -> this.snack(R.string.snack_no_online, SnackType.ERROR)
        StatusCode.CONNECT_ERROR -> this.snack(R.string.snack_connect_error, SnackType.ERROR)
        StatusCode.INTERNAL_ERROR -> this.snack(R.string.snack_internal_error, SnackType.ERROR)
        StatusCode.UPDATE_ERROR -> this.snack(R.string.snack_update_error, SnackType.ERROR)

        StatusCode.UPDATE_SUCCESS -> this.snack(R.string.snack_update_success, SnackType.FINISH)
        StatusCode.EMPTY_UPDATE -> this.snack(R.string.snack_empty_update, SnackType.FINISH)
        StatusCode.ALREADY_UPDATED -> this.snack(R.string.snack_already_updated, SnackType.FINISH)

        StatusCode.COPIED -> this.snack(R.string.snack_copied, SnackType.FINISH)

        StatusCode.USER_INSERTED -> this.snack(R.string.snack_new_user_success, SnackType.FINISH)
        StatusCode.USER_DELETED -> this.snack(R.string.snack_delete_user_done, SnackType.FINISH)
        StatusCode.USER_NOT_FOUND -> this.snack(R.string.snack_new_user_not_found, SnackType.FINISH)

        else -> null
    }
}

fun View.snack(code: ReserveStatus): Snackbar? {
    return when (code) {
        ReserveStatus.RESERVING -> this.snack(R.string.snack_reserving, SnackType.LOADING)
        ReserveStatus.CONSULTING -> this.snack(R.string.snack_consulting, SnackType.LOADING)
        ReserveStatus.RESERVED -> this.snack(R.string.snack_reserved, SnackType.FINISH)
        ReserveStatus.UNAVAILABLE -> this.snack(R.string.snack_unavailable, SnackType.ERROR)
        ReserveStatus.SOLDOUT -> this.snack(R.string.snack_soldout, SnackType.ERROR)
        ReserveStatus.INVALID -> this.snack(R.string.snack_invalid, SnackType.ERROR)
        ReserveStatus.REDOLOGIN -> this.snack(R.string.snack_redologin, SnackType.ERROR)
        else -> null
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
    snackBar.view.setBackgroundResource(R.drawable.rounded_corners)
    val drawable = snackBar.view.background as GradientDrawable
    drawable.setColor(color)
}

fun SwipeRefreshLayout.init(f: () -> Unit) {
    this.setOnRefreshListener { f() }
    this.setProgressBackgroundColorSchemeResource(R.color.accent)
    this.setColorSchemeResources(R.color.white)
}

fun Context.colorOf(resId: Int): Int {
    return ContextCompat.getColor(this, resId)
}

// Devuelvo true porque lo uso en los onOptionsItemSelected() de los fragments
fun Activity.startBrowser(uri: String): Boolean {
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

fun Activity.shareText(subject: String, text: String, title: String = "UNCmorfi"): Boolean {
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

fun Intent.toPendingIntent(context: Context): PendingIntent {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    return PendingIntent.getActivity(
        context,
        System.currentTimeMillis().toInt(),
        this,
        FLAG_IMMUTABLE
    )
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
    clipboard.setPrimaryClip(ClipData.newPlainText(label, data))
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

fun Context?.isOnline(): Boolean {
    return (this?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        .activeNetworkInfo?.isConnected ?: false
}

fun TextView.updateVisibility() {
    this.visibility = if (this.text.isNullOrEmpty()) GONE else VISIBLE
}

inline fun <T> LifecycleOwner.observe(flow : Flow<T>, state : Lifecycle.State = Lifecycle.State.RESUMED, crossinline body: (T) -> Unit){
    lifecycleScope.launch {
        repeatOnLifecycle(state){
            flow.collect{ body(it) }
        }
    }
}