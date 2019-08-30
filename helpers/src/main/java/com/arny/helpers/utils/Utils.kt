package com.arny.helpers.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.*
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.Spanned
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.amulyakhare.textdrawable.TextDrawable
import com.google.gson.Gson
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import java.io.File
import java.lang.reflect.Type
import java.util.*
import kotlin.math.roundToInt

/**
 *Created by Sedoy on 15.07.2019
 */


fun AppCompatActivity.replaceFragmentInActivity(
        fragment: Fragment, @IdRes frameId: Int,
        addToback: Boolean = false,
        tag: String? = null,
        onLoadFunc: () -> Unit? = {},
        animResourses: Pair<Int, Int>? = null
) {
    val tg = tag ?: fragment.javaClass.simpleName
    supportFragmentManager.transact {
        if (animResourses != null) {
            val slideIn = animResourses.first
            val slideOut = animResourses.second
            setCustomAnimations(slideIn, slideOut)
        }
        replace(frameId, fragment, tg)
        if (addToback) {
            addToBackStack(tag)
        }
    }
    onLoadFunc()
}

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commitAllowingStateLoss()
}


inline fun <reified T : Any> Activity.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivityForResult(intent, requestCode, options)
}

fun Activity.launchIntent(
        requestCode: Int = -1,
        options: Bundle? = null,
        init: Intent.() -> Unit = {}) {
    val intent = newIntent()
    intent.init()
    startActivityForResult(intent, requestCode, options)
}

fun Activity.launchIntent(
        options: Bundle? = null,
        init: Intent.() -> Unit = {}) {
    val intent = newIntent()
    intent.init()
    startActivity(intent, options)
}


inline fun <reified T : Any> Fragment.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {
    val context = this.context
    if (context != null) {
        val intent = newIntent<T>(context)
        intent.init()
        startActivityForResult(intent, requestCode, options)
    }
}


inline fun <reified T : Any> Fragment.launchActivity(
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {
    val context = this.context
    if (context != null) {
        val intent = newIntent<T>(context)
        intent.init()
        startActivity(intent, options)
    }
}

fun Fragment.launchIntent(
        requestCode: Int = -1,
        options: Bundle? = null,
        init: Intent.() -> Unit = {}) {
    val context = this.context
    if (context != null) {
        val intent = newIntent()
        intent.init()
        startActivityForResult(intent, requestCode, options)
    }
}

fun Fragment.launchIntent(
        options: Bundle? = null,
        init: Intent.() -> Unit = {}) {
    val context = this.context
    if (context != null) {
        val intent = newIntent()
        intent.init()
        startActivity(intent, options)
    }
}

inline fun <reified T : Any> Context.launchActivity(
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivity(intent, options)
}

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)
fun newIntent(): Intent = Intent()

fun Fragment.putExtras(init: Bundle.() -> Unit = {}) {
    val args = Bundle()
    args.init()
    this.arguments = args
}

fun  Fragment.putExtras(args: Bundle?) {
    this.arguments = args
}

fun Activity.putExtras(resultCode: Int? = null, clear: Boolean = true, init: Intent.() -> Unit = {}) {
    val i = if (clear) {
        Intent()
    } else {
        this.intent ?: Intent()
    }
    i.init()
    this.intent = i
    if (resultCode != null) {
        setResult(resultCode, this.intent)
    }
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

/*fun Activity.share(type: String, subject: String, body: String, shareTitle: String) {
    ShareCompat.IntentBuilder.from(this)
            .setType("message/rfc822")
            .addEmailTo(getString(R.string.support_email))
            .setSubject(getString(R.string.app_name))
            .setText("")
            .setChooserTitle(getString(R.string.send_email))
            .startChooser()
}*/

fun Activity.shareLocation(lat: Double, long: Double, label: String) {
    this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:<$lat>,<$long>?q=<$lat>,<$long>($label)")));
}

fun Activity.shareImage(uri: Uri) {
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/jpeg"
    }
    this.startActivity(Intent.createChooser(shareIntent, "Share with"));
}

fun Activity.shareFile(file: File) {
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        type = "image/*"
    }
    this.startActivity(Intent.createChooser(shareIntent, "Share with"));
}

enum class AnimType {
    DEFAULT, TOP_BOTTOM, LEFT_RIGHT
}

fun animateVisible(v: View, visible: Boolean, duration: Int, onComplete: () -> Unit? = {}, interpolator: TimeInterpolator? = null, type: AnimType = AnimType.DEFAULT) {
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
                    v.setVisible(visible)
                    onComplete.invoke()
                }
            })
        }
        AnimType.TOP_BOTTOM -> {
            v.pivotY = 0f
            if (visible) {
                v.scaleY = 0.0f
                v.setVisible(true)
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
                        v.setVisible(visible)
                        onComplete.invoke()
                    }
                })
            }
        }
        AnimType.LEFT_RIGHT -> {
            v.pivotX = 0f
            v.scaleX = if (visible) 0.0f else 1.0f
            v.setVisible(visible)
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

fun <T> Intent?.getExtra(extraName: String): T? {
    if (this.hasExtra(extraName)) {
        return this?.extras?.get(extraName) as? T
    }
    return null
}

fun <T> Activity?.getExtra(extraName: String): T? {
    val intent = this?.intent
    if (intent.hasExtra(extraName)) {
        return intent?.extras?.get(extraName) as? T
    }
    return null
}

fun <T> Fragment?.getExtra(extraName: String): T? {
    return this?.arguments?.get(extraName) as? T
}

fun <T> Bundle?.getExtra(extraName: String): T? {
    return this?.get(extraName) as? T
}

fun runOnUI(func: () -> Unit? = {}) = Handler(Looper.getMainLooper()).post { func.invoke() }

@JvmOverloads
fun runOnLooper(func: () -> Unit? = {}, looper: Looper? = Looper.getMainLooper()) = Handler(looper).post { func.invoke() }

fun View?.setVisible(visible: Boolean) {
    this?.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View?.setVisible(visible: Boolean, duration: Int, onComplete: () -> Unit?, interpolator: TimeInterpolator? = null, type: AnimType = AnimType.DEFAULT) {
    this?.let { animateVisible(it, visible, duration, onComplete, interpolator, type) }
}

fun TextView?.setString(text: String?): TextView? {
    this?.clearFocus()
    this?.tag = ""
    this?.text = text
    this?.tag = null
    return this
}

fun animateVisible(v: View, visible: Boolean, duration: Int) {
    val alpha = if (visible) 1.0f else 0.0f
    v.clearAnimation()
    v.animate()
            .alpha(alpha)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    v.setVisible(visible)
                }
            })
}

fun AppCompatActivity.backStackCnt(): Int {
    return supportFragmentManager.backStackEntryCount
}

fun AppCompatActivity.backStackClear() {
    return supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
}

fun AppCompatActivity.resetFragmentsInManager() {
    val fragments = supportFragmentManager.fragments
    for (curFrag in fragments) {
        if (curFrag != null) {
            supportFragmentManager.beginTransaction().remove(curFrag).commitAllowingStateLoss()
        }
    }
}

fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, @IdRes frameId: Int, tag: String? = null) {
    supportFragmentManager.transact {
        replace(frameId, fragment, tag)
    }
}

fun AppCompatActivity.addFragmentToActivity(fragment: Fragment,@IdRes frameId: Int, tag: String?) {
    supportFragmentManager.transact {
        add(frameId,fragment, tag)
    }
}

fun AppCompatActivity.popBackStack(immadiate: Boolean = true) {
    if (immadiate) {
        supportFragmentManager.popBackStackImmediate()
    } else {
        supportFragmentManager.popBackStack()
    }
}

fun checkPremission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun AppCompatActivity.getFragment(position: Int): Fragment? {
    return supportFragmentManager.fragments.getOrNull(position)
}

fun AppCompatActivity.fragmentBackStackCnt(): Int {
    return supportFragmentManager.backStackEntryCount
}

fun AppCompatActivity.fragmentsBackStack(): Fragment {
    val fragments = supportFragmentManager.fragments
    val size = fragments.size
    val fragment = fragments.get(size - 1)
    return fragment
}

fun AppCompatActivity.fragmentBackStackClear() {
    return supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
}

fun inflate(inflater: LayoutInflater, container: ViewGroup?, @LayoutRes resource: Int): View? {
    return inflater.inflate(resource, container, false)
}

fun AppCompatActivity.getFragmentInContainer(@IdRes containerId: Int): Fragment? {
    return supportFragmentManager.findFragmentById(containerId)
}

fun AppCompatActivity.getFragmentByTag(tag: String?): Fragment? {
    return supportFragmentManager.findFragmentByTag(tag)
}

fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: (ActionBar?.() -> Unit)? = null) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action?.let { it() }
    }
}

fun AppCompatActivity.setupActionBar(toolbar: Toolbar?, action: (ActionBar?.() -> Unit)? = null) {
    setSupportActionBar(toolbar)
    supportActionBar?.run {
        action?.let { it() }
    }
}

fun View.showSnackBar(message: String, duration: Int) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackBar(message: String, actionText: String, duration: Int? = null, @ColorInt actionColor: Int? = null, action: () -> Unit) {
    val snackBar = Snackbar.make(this, message, duration ?: Snackbar.LENGTH_INDEFINITE)
    snackBar.setAction(actionText) { action.invoke() }
    if (actionColor != null) {
        snackBar.setActionTextColor(actionColor)
    }
    snackBar.show()
}

fun Bundle?.dump(): String? {
    return Utility.dumpBundle(this)
}

fun Intent?.dump(): String? {
    return Utility.dumpIntent(this)
}

fun Cursor?.dump(): String? {
    return Utility.dumpCursor(this)
}

fun dumpCursorColumns(cur: Cursor): String {
    val count = cur.columnCount
    val builder = StringBuilder()
    for (i in 0..(count - 1)) {
        val columnName = cur.getColumnName(i)
        var curValue: String? = null
        try {
            curValue = "[String]" + cur.getString(i)?.toString()
        } catch (e: Exception) {
        }
        if (curValue == null) {
            try {
                curValue = "[Long]" + cur.getLong(i)?.toString()
            } catch (e: Exception) {
            }
        }
        if (curValue == null) {
            try {
                curValue = "[Int]" + cur.getInt(i)?.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        builder.append("$i:$columnName->$curValue\n")
    }
    return builder.toString()
}

fun getCursorColumns(cur: Cursor): HashMap<String, Any> {
    val count = cur.columnCount
    val mapColumns = hashMapOf<String, Any>()
    for (i in 0..(count - 1)) {
        val columnName = cur.getColumnName(i)
        var curValue: Any? = null
        try {
            curValue = cur.getString(i)
        } catch (e: Exception) {
        }
        if (curValue == null) {
            try {
                curValue = cur.getLong(i)
            } catch (e: Exception) {
            }
        }
        if (curValue == null) {
            try {
                curValue = cur.getInt(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (curValue != null) {
            mapColumns[columnName] = curValue
        } else {
            mapColumns[columnName] = "null"
        }
    }
    return mapColumns
}

private fun cursorConvert(cur: Cursor, i: Int, builder: StringBuilder) {
    val columnName = cur.getColumnName(i)
    var type: Int? = null
    try {
//        type = Utility.getCursorType(cur, i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val curValue = when (type) {
        0 -> "[String]${cur.getString(i)?.toString()}"
        1 -> "[Long]${cur.getLong(i)?.toString()}"
        3 -> "[Int]${cur.getInt(i)?.toString()}"
        else -> "${cur.getString(i)?.toString()}"
    }
    builder.append("$i:$columnName->$curValue\n")
}

@JvmOverloads
fun getGMDIcon(context: Context, gmd_icon: GoogleMaterial.Icon, size: Int, color: Int? = null): IconicsDrawable? {
    val icon = IconicsDrawable(context).icon(gmd_icon)
    icon.sizeDp(size)
    if (color != null) {
        icon.color(color)
    }
    return icon
}

@JvmOverloads
fun ContextThemeWrapper.showAlertDialog(title: String? = null, content: String? = null,
                                        positivePair: Pair<String, (() -> Unit)?>? = null,
                                        negativePair: Pair<String, (() -> Unit)?>? = null,
                                        cancelable: Boolean = true,
                                        style: Int? = null): AlertDialog {
    val builder = if (style != null) AlertDialog.Builder(this, style) else AlertDialog.Builder(this)
    title?.let { builder.setTitle(title) }
    content?.let { builder.setMessage(it) }
    positivePair?.let { builder.setPositiveButton(it.first) { _, which -> it.second?.invoke() } }
    negativePair?.let { builder.setNegativeButton(it.first) { _, _ -> it.second?.invoke() } }
    builder.setCancelable(cancelable)
    val dialog = builder.create()
    dialog.show()
    return dialog
}

fun checkContextTheme(context: Context): Boolean {
    val b = context is ContextThemeWrapper
    if (!b) {
        return false
    }
    return true
}


@JvmOverloads
fun alertDialog(context: Context?, title: String, content: String? = null, btnOkText: String? = "OK", btnCancelText: String? = null, cancelable: Boolean = false, onConfirm: () -> Unit? = {}, onCancel: () -> Unit? = {}, alert: Boolean = true, autoDissmiss: Boolean = true, btnNeutralText: String? = null, onNeutral: () -> Unit? = {}): MaterialDialog? {
    if (context == null) return null
    if (!checkContextTheme(context)) return null
    val builder = MaterialDialog.Builder(context)
    builder.title(title)
    builder.titleColor(if (alert) Color.RED else Color.BLACK)
    builder.cancelable(cancelable)
    builder.contentColor(Color.BLACK)
    if (btnOkText != null) {
        builder.positiveText(btnOkText)
        builder.onPositive { dialog, _ ->
            if (autoDissmiss) {
                dialog.dismiss()
            }
            onConfirm.invoke()
        }
    }
    if (btnCancelText != null) {
        builder.negativeText(btnCancelText)
        builder.onNegative { dialog, _ ->
            if (autoDissmiss) {
                dialog.dismiss()
            }
            onCancel.invoke()
        }
    }
    if (btnNeutralText != null) {
        builder.neutralText(btnNeutralText)
        builder.onNeutral { dialog, _ ->
            if (autoDissmiss) {
                dialog.dismiss()
            }
            onNeutral.invoke()
        }
    }
    if (!content.isNullOrBlank()) {
        builder.content(fromHtml(content))
    }
    val dlg = builder.build()
    dlg.show()
    return dlg

}


fun fromHtml(html: String): Spanned {
    return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(html);
    } else {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
    }
}

fun <T> getIntentExtra(intent: Intent?, extraName: String): T? {
    return intent?.extras?.get(extraName) as? T
}

fun <T> getBundleExtra(extras: Bundle?, extraName: String): T? {
    return extras?.get(extraName) as? T
}

@JvmOverloads
fun listDialog(context: Context, title: String, items: List<String>, cancelable: Boolean? = false, listDialogListener: ListDialogListener): MaterialDialog? {
    val dlg = MaterialDialog.Builder(context)
            .backgroundColor(Color.WHITE)
            .titleColor(Color.BLACK)
            .itemsColor(Color.BLACK)
            .title(title)
            .cancelable(cancelable ?: false)
            .items(items)
            .itemsCallback { _, _, position, text ->
                listDialogListener.onClick(position)
            }
            .build()
    dlg.show()
    return dlg
}

fun Cursor?.toList(onCursor: (c: Cursor) -> Unit) {
    if (this != null) {
        try {
            this.moveToPosition(-1);
            while (this.moveToNext()) {
                onCursor.invoke(this)
            }
        } catch (e: Exception) {
        } finally {
            this.close()
        }
    }
}

fun Cursor?.toItem(onCursor: (c: Cursor) -> Unit) {
    if (this != null) {
        try {
            if (this.moveToNext()) {
                onCursor.invoke(this)
            }
        } catch (e: Exception) {
        } finally {
            this.close()
        }
    }
}

fun Activity.createCustomLayoutDialog(@LayoutRes layout: Int, initView: View.() -> Unit, cancelable: Boolean = true): AlertDialog? {
    val builder = AlertDialog.Builder(this)
    builder.setView(LayoutInflater.from(this).inflate(layout, null, false).apply(initView))
    if (!cancelable) {
        builder.setCancelable(false)
    }
    val dialog = builder.create()
    dialog.show()
    return dialog
}

fun Context.getSizeDP(size: Int): Int {
    return (size * this.resources.displayMetrics.density).roundToInt()
}

@ColorInt
fun Context.getIntColor(@ColorRes res: Int): Int {
    return ContextCompat.getColor(this, res)
}

fun Context.getResDrawable(@DrawableRes res: Int): Drawable? {
    return ContextCompat.getDrawable(this, res)
}

fun ImageView?.setSrcTintColor(@DrawableRes src: Int, @ColorInt color: Int) {
    val drawable = this?.context?.let { ContextCompat.getDrawable(it, src) }
    if (drawable != null) {
        val wrapped = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrapped, color)
        this?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
}

fun getHexColor(color: Int): String {
    return String.format("#%06X", (0xFFFFFF and color))
}

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

private fun getServiceNotification(context: Context, cls: Class<*>, requestCode: Int, title: String, content: String, icon: Int): Notification {
    val notification: Notification
    val notificationIntent = Intent(context, cls)
    notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    val pendingIntent = PendingIntent.getActivity(context, requestCode, notificationIntent, 0)
    val notifbuild = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(context, createNotificationChannel(context))
    } else {
        Notification.Builder(context)
    }
    notifbuild.setSmallIcon(icon)// маленькая иконка
            .setAutoCancel(false)
            .setContentTitle(title)// Заголовок уведомления
            .setContentText(content) // Текст уведомления
    notifbuild.setContentIntent(pendingIntent)
    notification = notifbuild.build()
    return notification
}

fun createNotification(context: Context, cls: Class<*>, notifyId: Int, title: String, content: String = "", icon: Int, request: Int = 999) {
    val notification = getServiceNotification(context, cls, request, title, content, icon)
    val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    mNotificationManager?.notify(notifyId, notification)
}

@SuppressLint("RestrictedApi")
fun BottomNavigationView?.disableShiftMode() {
    val menuView = this?.getChildAt(0) as? BottomNavigationMenuView
    if (menuView != null) {
        try {
            val fld = "isShifting"//"mShiftingMode"
            val shiftingMode = menuView.javaClass.getDeclaredField(fld)
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView

                item.setShifting(false)
                // set once again checked value, so view will be updated

                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}

fun getIconDrawable(context: Context, color: Int, icon: GoogleMaterial.Icon, sizeDp: Int): Drawable {
    return IconicsDrawable(context)
            .icon(icon)
            .color(color)
            .sizeDp(sizeDp)
}

fun getTextDrawable(text: String, color: Int): TextDrawable {
    return TextDrawable.builder().buildRound(text, color)
}

fun <T> find(list: List<T>, c: T, comp: Comparator<T>): T? {
    return list.firstOrNull { comp.compare(c, it) == 0 }
}

@JvmOverloads
fun transliterate(message: String, toUpper: Boolean = false): String {
    val abcCyr = charArrayOf(' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    val abcLat = arrayOf(" ", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    val builder = StringBuilder()
    for (i in 0 until message.length) {
        for (x in abcCyr.indices) {
            if (message[i] == abcCyr[x]) {
                builder.append(abcLat[x])
            }
        }
    }
    var res = builder.toString()
    if (toUpper) {
        res = res.toUpperCase()
    }
    return res.trim()
}

fun <T> findPosition(list: List<T>, item: T): Int {
    return list.indexOf(item)
}

fun <T> findPosition(list: Array<T>, item: T): Int {
    return list.indexOf(item)
}

fun <T> getExcludeList(list: ArrayList<T>, items: List<T>, comparator: Comparator<T>): ArrayList<T> {
    val res = ArrayList<T>()
    for (t in list) {
        val pos = Collections.binarySearch(items, t, comparator)
        if (pos < 0) {
            res.add(t)
        }
    }
    return res
}

fun getSQLType(fieldType: String): String {
    val res = when {
        fieldType.equals("int", true) -> "INTEGER"
        fieldType.equals("integer", true) -> "INTEGER"
        fieldType.equals("float", true) -> "REAL"
        fieldType.equals("double", true) -> "REAL"
        fieldType.equals("string", true) -> "TEXT"
        fieldType.equals("char", true) -> "TEXT"
        fieldType.equals("byte", true) -> "TEXT"
        else -> "TEXT"
    }
    return res
}

/**
 * Универсальная функция окончаний
 * @param [count] число
 * @param [zero_other] слово с окончанием значения  [count] либо ноль,либо все остальные варианты включая от 11 до 19 (слов)
 * @param [one] слово с окончанием значения  [count]=1 (слово)
 * @param [two_four] слово с окончанием значения  [count]=2,3,4 (слова)
 */
fun getTermination(count: Int, zero_other: String, one: String, two_four: String): String {
    if (count % 100 in 11..19) {
        return count.toString() + " " + zero_other
    }
    return when (count % 10) {
        1 -> count.toString() + " " + one
        2, 3, 4 -> count.toString() + " " + two_four
        else -> count.toString() + " " + zero_other
    }
}

/**
 * safety String? to Double
 */
fun String?.setDouble(): Double {
    val source = this ?: ""
    if (source.isBlank() || source==".") {
        return 0.0
    }
    return source.toDouble()
}

fun String?.ifEmpty(default: String): String {
    val blank = this?.isBlank() ?: true
    return if (blank) default else this!!
}

/**
 * return items from second array wich not includes in first by custom diff in predicate
 * @param first ArrayList of  T
 * @param second Collection of  T
 * @param predicate function equals
 */
fun <T> arraysDiff(first: ArrayList<T>?, second: ArrayList<T>, fillAll: Boolean = false, predicate: (firstItem: T, secondItem: T) -> Boolean): ArrayList<T> {
    if (first.isNullOrEmpty()) return second
    if (first.isEmpty() && second.isNotEmpty()) return second
    val result = arrayListOf<T>()
    val secondTemp = arrayListOf<T>()
    secondTemp.addAll(second)
    for (f in first) {
        var equal = false
        for (s in second) {
            if (predicate.invoke(f, s)) {
                secondTemp.remove(s)
                equal = true
                break
            }
        }
        if (!equal) {
            result.add(f)
        }
    }
    if (fillAll) {
        result.addAll(secondTemp)
    }
    return result
}

fun <T> Collection<T>.filterList(predicate: (T) -> Boolean): ArrayList<T> {
    return ArrayList(this.filter(predicate))
}

fun <T : Any> Collection<T>?.dump(predicate: (cls: T) -> String?): String {
    return dumpArray(this,predicate)
}

fun <T : Any> dumpArray(collection: Collection<T>?, predicate: (cls: T) -> String?): String {
    var res = ""
    if (collection == null) {
        res += "Collection is null"
        return res
    }
    if (collection.isEmpty()) {
        res += "Collection is empty"
        return res
    }
    for (ind in collection.withIndex()) {
        val index = ind.index
        val value = ind.value
        if (index == 0) {
            res += "${value.javaClass.name}\n"
            res += predicate.invoke(value)
        } else {
            res += "\n"
            res += predicate.invoke(value)
        }
    }
    return res
}

/**
 * Универсальная функция окончаний
 * @param [count] число
 * @param [zero_other] слово с окончанием значения  [count] либо ноль,либо все остальные варианты включая от 11 до 19 (слов)
 * @param [one] слово с окончанием значения  [count]=1 (слово)
 * @param [two_four] слово с окончанием значения  [count]=2,3,4 (слова)
 */
fun getTermination(count: Int, zero_other: String, one: String, two_four: String, concat: Boolean = true): String {
    if (count % 100 in 11..19) {
        return if (concat) "$count $zero_other" else " $zero_other"
    }
    return when (count % 10) {
        1 -> if (concat) "$count $one" else one
        2, 3, 4 -> if (concat) "$count $two_four" else two_four
        else -> if (concat) "$count $zero_other" else zero_other
    }
}

fun String?.isEmpty(): Boolean = this.isNullOrBlank()

/**
 * Extended function to check empty
 */
fun Any?.empty(): Boolean {
    return when {
        this == null -> true
        this is String && this == "null" -> true
        this is String -> this.isBlank()
        this is Iterable<*> -> this.asIterable().none()
        this is Collection<*> -> this.isEmpty()
        else -> false
    }
}

fun <T> Collection<T>.copy(): ArrayList<T> {
    val newList = ArrayList<T>()
    newList.addAll(this)
    return newList
}

fun Any?.toJson(): String? {
    return if (this != null) Gson().toJson(this) else null
}

fun <T> Any?.fromJson(cls: Class<T>): T? {
    return Gson().fromJson(this.toString(), cls)
}

fun <T> Any?.fromJson(type: Type?): T? {
    return Gson().fromJson(this.toString(), type)
}

fun String?.parseLong(): Long? {
    return when {
        this == null -> null
        this.isBlank() -> null
        else -> {
            try {
                this.toLong()
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun String?.parseDouble(): Double? {
    return when {
        this == null -> null
        this.isBlank() -> null
        else -> {
            try {
                this.toDouble()
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun String?.parseInt(): Int? {
    return when {
        this == null -> null
        this.isBlank() -> null
        else -> {
            try {
                this.toInt()
            } catch (e: Exception) {
                null
            }
        }
    }
}