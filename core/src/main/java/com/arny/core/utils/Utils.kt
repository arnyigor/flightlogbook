package com.arny.core.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.os.StrictMode
import android.text.Spanned
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.gson.*
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.*
import kotlin.math.roundToInt

fun Context.getSystemLocale(): Locale? {
    val configuration = this.resources.configuration
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) configuration.locales.get(0) else configuration.locale
}

fun Fragment.toastError(message: String?) {
    ToastMaker.toastError(this.requireContext(), message)
}

fun Activity.shareText(text: String) {
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    this.startActivity(Intent.createChooser(shareIntent, "Share with"));
}

fun Activity.sendEmail(email: String, subject: String, body: String, shareTitle: String) {
    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.putExtra(Intent.EXTRA_TEXT, body)
//emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body); //If you are using HTML in your body text
    startActivity(Intent.createChooser(emailIntent, shareTitle))
}

fun Activity.shareFileWithType(uri: Uri, fileType: String) {
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = fileType
        if (Build.VERSION.SDK_INT >= Q) {
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
    }
    this.startActivity(Intent.createChooser(shareIntent, "Share with"));
}

enum class AnimType {
    DEFAULT, TOP_BOTTOM, LEFT_RIGHT
}

fun animateVisible(
    v: View,
    visible: Boolean,
    duration: Int,
    onComplete: () -> Unit? = {},
    interpolator: TimeInterpolator? = null,
    type: AnimType = AnimType.DEFAULT
) {
    v.clearAnimation()
    val animate = v.animate()
    if (interpolator != null) {
        animate.interpolator = interpolator
    }
    animate.duration = duration.toLong()
    when (type) {
        AnimType.DEFAULT -> {
            animate.alpha(if (visible) 1.0f else 0.0f)
            animate.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    v.isVisible = visible
                    onComplete.invoke()
                }
            })
        }
        AnimType.TOP_BOTTOM -> {
            v.pivotY = 0f
            if (visible) {
                v.scaleY = 0.0f
                v.isVisible = true
                animate.scaleY(1.0f)
                animate.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        onComplete.invoke()
                    }
                })
            } else {
                v.scaleY = 1.0f
                animate.scaleY(0.0f)
                animate.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        v.isVisible = visible
                        onComplete.invoke()
                    }
                })
            }
        }
        AnimType.LEFT_RIGHT -> {
            v.pivotX = 0f
            v.scaleX = if (visible) 0.0f else 1.0f
            v.isVisible = visible
            animate.scaleX(if (visible) 1.0f else 0.0f)
            animate.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    onComplete.invoke()
                }
            })
        }
    }
}

fun Intent?.hasExtra(extraName: String): Boolean {
    return this?.hasExtra(extraName) ?: false
}

inline fun <reified T> Intent?.getExtra(extraName: String): T? {
    if (this.hasExtra(extraName)) {
        return this?.extras?.get(extraName) as? T
    }
    return null
}

inline fun <reified T> Activity?.getExtra(extraName: String): T? {
    val intent = this?.intent
    if (intent.hasExtra(extraName)) {
        return intent?.extras?.get(extraName) as? T
    }
    return null
}

inline fun <reified T> Fragment?.getExtra(extraName: String): T? =
    this?.arguments?.get(extraName) as? T

inline fun <reified T> Bundle?.getExtra(extraName: String): T? = this?.get(extraName) as? T

fun animateVisible(v: View, visible: Boolean, duration: Int) {
    val alpha = if (visible) 1.0f else 0.0f
    v.clearAnimation()
    v.animate()
        .alpha(alpha)
        .setDuration(duration.toLong())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                v.isVisible = visible
            }
        })
}

fun View.showSnackBar(message: String?, duration: Int = Snackbar.LENGTH_SHORT) {
    message?.let {
        Snackbar.make(this, message, duration).show()
    }
}

fun TextView?.hideSoftKeyboard() {
    (this?.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { im ->
        im.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

@SuppressLint("ClickableViewAccessibility")
fun TextView.setDrawableRightClick(onClick: () -> Unit) {
    this.setOnTouchListener { v, event ->
        if (v is TextView) {
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = v.compoundDrawables.getOrNull(2)
                if (drawable != null && event.x >= (v.right - drawable.bounds.width())) {
                    onClick.invoke()
                    return@setOnTouchListener true
                }
            }
        }
        return@setOnTouchListener false
    }
}

@SuppressLint("ClickableViewAccessibility")
fun TextView.setDrawableLeftClick(onClick: () -> Unit) {
    this.setOnTouchListener { v, event ->
        if (v is TextView) {
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = v.compoundDrawables.getOrNull(0)
                if (drawable != null && event.rawX <= v.totalPaddingLeft) {
                    onClick.invoke()
                    return@setOnTouchListener true
                }
            }
        }
        return@setOnTouchListener false
    }
}

inline fun <T, R> T?.nonNullOrSkip(block: T.() -> R) {
    this?.run(block)
}

fun Context.getColorCompat(@ColorRes colorRes: Int): Int =
    ContextCompat.getColor(this, colorRes)

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? =
    AppCompatResources.getDrawable(this, drawableRes)

fun TextView?.setDrawableStartWithTint(@DrawableRes drawable: Int?, @ColorInt color: Int? = null) {
    nonNullOrSkip {
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawable?.let(context::getDrawableCompat)?.also { drawable ->
                color?.also { tintColor ->
                    drawable.mutate().colorFilter = PorterDuffColorFilter(
                        tintColor,
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            },
            compoundDrawablesRelative[1],
            compoundDrawablesRelative[2],
            compoundDrawablesRelative[3]
        )
    }
}

fun View.showSnackBar(
    message: String,
    actionText: String,
    duration: Int? = null,
    @ColorInt actionColor: Int? = null,
    action: () -> Unit
) {
    val snackBar = Snackbar.make(this, message, duration ?: Snackbar.LENGTH_INDEFINITE)
    snackBar.setAction(actionText) { action.invoke() }
    if (actionColor != null) {
        snackBar.setActionTextColor(actionColor)
    }
    snackBar.show()
}

fun Bundle?.dump(): String? = Utility.dumpBundle(this)

fun Intent?.dump(): String? = Utility.dumpIntent(this)

@JvmOverloads
fun getGMDIcon(
    context: Context,
    gmd_icon: GoogleMaterial.Icon,
    size: Int,
    color: Int? = null
): IconicsDrawable {
    val icon = IconicsDrawable(context).apply {
        icon = gmd_icon
    }
    icon.sizeDp = size
    if (color != null) {
        icon.colorInt = color
    }
    return icon
}

fun checkContextTheme(context: Context?): Boolean = context is ContextThemeWrapper

fun fromHtml(html: String): Spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)

fun isKeyboardVisible(context: Context): Boolean {
    val imm by lazy { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    val windowHeightMethod =
        InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
    val height = windowHeightMethod.invoke(imm) as Int
    return height > 100
}

fun Context.getSizeDP(size: Int): Int = (size * this.resources.displayMetrics.density).roundToInt()

@ColorInt
fun Context.getIntColor(@ColorRes res: Int): Int = ContextCompat.getColor(this, res)

fun Context.getResDrawable(@DrawableRes res: Int): Drawable? = ContextCompat.getDrawable(this, res)

fun ImageView?.setSrcTintColor(@DrawableRes src: Int, @ColorInt color: Int) {
    val drawable = this?.context?.let { ContextCompat.getDrawable(it, src) }
    if (drawable != null) {
        this?.setImageDrawable(drawable)
        val wrapped = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrapped, color)
        this?.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}

fun getHexColor(color: Int): String = String.format("#%06X", (0xFFFFFF and color))

@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationChannel(context: Context): String {
    val channelId = "my_service"
    val channelName = "My Background Service"
    val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
    chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    service?.createNotificationChannel(chan)
    return channelId
}

private fun getServiceNotification(
    context: Context,
    cls: Class<*>,
    requestCode: Int,
    title: String,
    content: String,
    icon: Int
)
        : Notification {
    val notification: Notification
    val notificationIntent = Intent(context, cls)
    notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    val pendingIntent = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
    val builder = getNotificationBuilder(context)
    builder.setSmallIcon(icon)// маленькая иконка
        .setAutoCancel(false)
        .setContentTitle(title)// Заголовок уведомления
        .setContentText(content) // Текст уведомления
    builder.setContentIntent(pendingIntent)
    notification = builder.build()
    return notification
}

private fun getNotificationBuilder(context: Context): Notification.Builder =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(context, createNotificationChannel(context))
    } else {
        Notification.Builder(context)
    }

fun createNotification(
    context: Context,
    cls: Class<*>,
    notifyId: Int,
    title: String,
    content: String = "",
    icon: Int,
    request: Int = 999
) {
    val notification = getServiceNotification(context, cls, request, title, content, icon)
    val mNotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    mNotificationManager?.notify(notifyId, notification)
}

fun getIconDrawable(
    context: Context,
    color: Int,
    icon: GoogleMaterial.Icon,
    sizeDp: Int
): Drawable = IconicsDrawable(context).apply {
    this.icon = icon
    this.colorInt = color
    this.sizeDp = sizeDp
}

fun getTextDrawable(text: String, color: Int): TextDrawable =
    TextDrawable.builder().buildRound(text, color)

/**
 * Extended function to check empty
 */
fun Any?.empty(): Boolean = when {
    this == null -> true
    this is String && this == "null" -> true
    this is String -> this.isBlank()
    this is Iterable<*> -> this.asIterable().none()
    this is Collection<*> -> this.isEmpty()
    else -> false
}

fun Any?.toJson(): String? = if (this != null) Gson().toJson(this) else null

fun <T> Any?.fromJson(cls: Class<T>): T? =
    GsonBuilder()
        .setLenient()
        .create()
        .fromJson(this.toString(), cls)

fun <T> Any?.fromJson(gson: Gson, cls: Class<T>): T? =
    gson.fromJson(this.toString(), cls)

fun <T> String?.fromJson(clazz: Class<*>, deserialize: (JsonElement) -> T): T {
    return GsonBuilder()
        .setLenient()
        .registerTypeAdapter(
            clazz,
            JsonDeserializer { json, _, _ -> deserialize.invoke(json) }
        )
        .create().fromJson<T>(this, clazz)
}

inline fun <reified T> JSONObject?.getValue(extraName: String): T? =
    if (this?.has(extraName) == true) {
        this.get(extraName) as? T
    } else {
        null
    }

fun <T> T.toJson(clazz: Class<*>, serialize: (src: T) -> JsonElement): String =
    GsonBuilder()
        .setLenient()
        .registerTypeAdapter(
            clazz,
            JsonSerializer { src: T, _, _ ->
                serialize(src)
            }
        )
        .create()
        .toJson(this)

fun <T> Any?.fromJson(type: Type?): T? {
    return Gson().fromJson(this.toString(), type)
}

fun String?.parseLong(): Long? = this?.toLongOrNull()

fun String?.parseDouble(): Double? = this?.toDoubleOrNull()

fun String?.parseInt(): Int? = this?.toIntOrNull()

fun String?.parseInt(default: Int = 0): Int = this?.toIntOrNull() ?: default