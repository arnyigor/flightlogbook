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
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_FORCED
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import com.amulyakhare.textdrawable.TextDrawable
import com.arny.core.R
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import java.lang.reflect.Type
import java.util.*
import kotlin.math.roundToInt

fun AppCompatActivity.replaceFragment(
    fragment: Fragment, @IdRes frameId: Int,
    addToback: Boolean = false,
    tag: String? = null,
    onLoadFunc: () -> Unit? = {}
) {
    val tg = tag ?: fragment.javaClass.simpleName
    supportFragmentManager.transact {
        setCustomAnimations(
            R.anim.anim_slide_in_left,
            R.anim.anim_slide_out_left,
            R.anim.anim_slide_in_right,
            R.anim.anim_slide_out_right
        )
        replace(frameId, fragment, tg)
        if (addToback) {
            addToBackStack(tag)
        }
    }
    onLoadFunc()
}

fun Context.getSystemLocale(): Locale? {
    val configuration = this.resources.configuration
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) configuration.locales.get(0) else configuration.locale
}

/**
 * Runs a FragmentTransaction, then calls commit().
 */
inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commitAllowingStateLoss()
}

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivityForResult(intent, requestCode, options)
    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
}

fun Activity.launchIntent(
    requestCode: Int = -1,
    options: Bundle? = null,
    init: Intent.() -> Unit = {}
) {
    val intent = newIntent()
    intent.init()
    startActivityForResult(intent, requestCode, options)
}

fun Activity.launchIntent(
    options: Bundle? = null,
    init: Intent.() -> Unit = {}
) {
    val intent = newIntent()
    intent.init()
    startActivity(intent, options)
}

inline fun <reified T : Any> Fragment.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val context = this.context
    if (context != null) {
        val intent = newIntent<T>(context)
        intent.init()
        startActivityForResult(intent, requestCode, options)
        this.activity?.overridePendingTransition(
            R.anim.anim_slide_in_left,
            R.anim.anim_slide_out_left
        )
    }
}

inline fun <reified T : Any> Fragment.launchActivity(
    options: Bundle? = null,
    animResourses: Pair<Int, Int>? = null,
    useStandartTransition: Boolean = true,
    noinline init: Intent.() -> Unit = {}
) {
    val context = this.context
    if (context != null) {
        val intent = newIntent<T>(context)
        intent.init()
        startActivity(intent, options)
        if (animResourses != null) {
            this.activity?.overridePendingTransition(animResourses.first, animResourses.second)
        } else if (useStandartTransition) {
            this.activity?.overridePendingTransition(
                R.anim.anim_slide_in_left,
                R.anim.anim_slide_out_left
            )
        }
    }
}

fun Fragment.launchIntent(
    requestCode: Int = -1,
    options: Bundle? = null,
    init: Intent.() -> Unit = {}
) {
    val context = this.context
    if (context != null) {
        val intent = newIntent()
        intent.init()
        startActivityForResult(intent, requestCode, options)
    }
}

fun Fragment.launchIntent(
    options: Bundle? = null,
    init: Intent.() -> Unit = {}
) {
    val context = this.context
    if (context != null) {
        val intent = newIntent()
        intent.init()
        startActivity(intent, options)
    }
}

inline fun <reified T : Any> Context.launchActivity(
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
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

fun Fragment.putExtras(args: Bundle?) {
    this.arguments = args
}

fun Fragment.toastError(message: String?) {
    ToastMaker.toastError(this.requireContext(), message)
}

fun Activity.toastError(message: String?) {
    ToastMaker.toastError(this, message)
}

fun Activity.putExtras(
    resultCode: Int? = null,
    clear: Boolean = true,
    init: Intent.() -> Unit = {}
) {
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
    this.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:<$lat>,<$long>?q=<$lat>,<$long>($label)")
        )
    );
}

fun Activity.shareImage(uri: Uri) {
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/jpeg"
    }
    this.startActivity(Intent.createChooser(shareIntent, "Share with"));
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

fun AppCompatActivity.resetFragmentsInManager() {
    val fragments = supportFragmentManager.fragments
    for (curFrag in fragments) {
        if (curFrag != null) {
            supportFragmentManager.beginTransaction().remove(curFrag).commitAllowingStateLoss()
        }
    }
}

fun AppCompatActivity.replaceFragmentInActivity(
    fragment: Fragment,
    @IdRes frameId: Int,
    tag: String? = null
) {
    supportFragmentManager.transact {
        replace(frameId, fragment, tag)
    }
}

fun AppCompatActivity.addFragmentToActivity(fragment: Fragment, @IdRes frameId: Int, tag: String?) {
    supportFragmentManager.transact {
        add(frameId, fragment, tag)
    }
}

fun AppCompatActivity.popBackStack(immadiate: Boolean = true) {
    if (immadiate) {
        supportFragmentManager.popBackStackImmediate()
    } else {
        supportFragmentManager.popBackStack()
    }
}

fun AppCompatActivity.getFragment(position: Int): Fragment? {
    return supportFragmentManager.fragments.getOrNull(position)
}

fun AppCompatActivity.fragmentBackStackCnt(): Int {
    return supportFragmentManager.backStackEntryCount
}

fun AppCompatActivity.fragmentBackStack() {
    if (supportFragmentManager.backStackEntryCount > 0) {
        supportFragmentManager.popBackStack()
    } else {
        this.onBackPressed()
    }
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

fun AppCompatActivity.setupActionBar(
    @IdRes toolbarId: Int,
    action: (ActionBar?.() -> Unit)? = null
) {
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

fun View.showSnackBar(message: String?, duration: Int = Snackbar.LENGTH_SHORT) {
    message?.let {
        Snackbar.make(this, message, duration).show()
    }
}

fun Context?.showSoftKeyboard(textView: TextView, show: Boolean) {
    (this?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { im ->
        if (show) {
            im.showSoftInput(textView, SHOW_FORCED)
        } else {
            im.hideSoftInputFromWindow(textView.windowToken, 0)
        }
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

fun Bundle?.dump(): String? {
    return Utility.dumpBundle(this)
}

fun Intent?.dump(): String? {
    return Utility.dumpIntent(this)
}

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

fun fromHtml(html: String): Spanned {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

inline fun <reified T> getIntentExtra(intent: Intent?, extraName: String): T? {
    return intent?.extras?.get(extraName) as? T
}

inline fun <reified T> getBundleExtra(extras: Bundle?, extraName: String): T? {
    return extras?.get(extraName) as? T
}

fun isKeyboardVisible(context: Context): Boolean {
    val imm by lazy { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    val windowHeightMethod =
        InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
    val height = windowHeightMethod.invoke(imm) as Int
    return height > 100
}

fun Context.getSizeDP(size: Int): Int {
    return (size * this.resources.displayMetrics.density).roundToInt()
}

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
    val pendingIntent = PendingIntent.getActivity(context, requestCode, notificationIntent, 0)
    val notifbuild = getNotifBuilder(context)
    notifbuild.setSmallIcon(icon)// маленькая иконка
        .setAutoCancel(false)
        .setContentTitle(title)// Заголовок уведомления
        .setContentText(content) // Текст уведомления
    notifbuild.setContentIntent(pendingIntent)
    notification = notifbuild.build()
    return notification
}

private fun getNotifBuilder(context: Context): Notification.Builder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(context, createNotificationChannel(context))
    } else {
        Notification.Builder(context)
    }
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
                item.setChecked(item.itemData?.isChecked == true)
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
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
 * safety String? to Double
 */
fun String?.setDouble(): Double {
    val source = this ?: ""
    if (source.isBlank() || source == ".") {
        return 0.0
    }
    return source.toDouble()
}

fun <T : Any> Collection<T>?.dump(predicate: (cls: T) -> String?): String =
    dumpArray(this, predicate)

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
fun getTermination(
    count: Int,
    zero_other: String,
    one: String,
    two_four: String,
    concat: Boolean = true
): String {
    if (count % 100 in 11..19) {
        return if (concat) "$count $zero_other" else " $zero_other"
    }
    return when (count % 10) {
        1 -> if (concat) "$count $one" else one
        2, 3, 4 -> if (concat) "$count $two_four" else two_four
        else -> if (concat) "$count $zero_other" else zero_other
    }
}

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

fun String?.parseInt(default: Int = 0): Int {
    return when {
        this == null -> default
        this.isBlank() -> default
        else -> {
            try {
                this.toInt()
            } catch (e: Exception) {
                default
            }
        }
    }
}