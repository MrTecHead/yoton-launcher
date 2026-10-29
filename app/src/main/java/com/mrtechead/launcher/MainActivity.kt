package com.mrtechead.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    // ---- Edit these if your projector's system apps ever differ -------------
    private val hdmiPackage = "com.softwinner.awlivetv"
    private val hdmiClass = "com.softwinner.awlivetv.MainActivity"
    private val sourcePackage = "com.softwinner.awsource"
    private val stockPackage = "com.htc.luminaos"
    private val stockClass = "com.htc.luminaos.activity.MainActivity"
    // -------------------------------------------------------------------------

    private val cardW = 256
    private val cardH = 144
    private val radius = 18

    private lateinit var shortcutsRow: LinearLayout
    private lateinit var appsRow: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFF1B1035.toInt(), 0xFF0B1B3A.toInt())
            )
            clipChildren = false
            clipToPadding = false
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(48), dp(28), dp(48), dp(28))
            clipChildren = false
            clipToPadding = false
        }

        // Header
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        header.addView(TextView(this).apply {
            text = "Home"
            setTextColor(Color.WHITE)
            textSize = 34f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(TextClock(this).apply {
            format12Hour = "HH:mm"
            format24Hour = "HH:mm"
            setTextColor(0xCCFFFFFF.toInt())
            textSize = 28f
        })
        column.addView(header)

        column.addView(sectionTitle("Inputs"))
        shortcutsRow = makeRow(column)

        column.addView(sectionTitle("Apps"))
        appsRow = makeRow(column)

        root.addView(column)
        setContentView(root)

        buildShortcuts()
    }

    override fun onResume() {
        super.onResume()
        populateApps()
        if (currentFocus == null) shortcutsRow.getChildAt(0)?.requestFocus()
    }

    // Home screen: Back does nothing
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {}

    // ---- Shortcut tiles ------------------------------------------------------

    private fun buildShortcuts() {
        shortcutsRow.removeAllViews()

        addShortcut("HDMI", "Input source", 0xFF7C4DFF.toInt(), 0xFF3F1DCB.toInt()) {
            openComponent(hdmiPackage, hdmiClass, "HDMI")
        }

        if (packageManager.getLaunchIntentForPackage(sourcePackage) != null ||
            packageManager.getLeanbackLaunchIntentForPackage(sourcePackage) != null
        ) {
            addShortcut("Sources", "All inputs", 0xFF00B8D4.toInt(), 0xFF006C86.toInt()) {
                if (!launchPackage(sourcePackage)) toast("Couldn't open Sources")
            }
        }

        addShortcut("Original launcher", "Switch back", 0xFFFF8F00.toInt(), 0xFFC43E00.toInt()) {
            openComponent(stockPackage, stockClass, "Original launcher")
        }

        addShortcut("Settings", "System", 0xFF607D8B.toInt(), 0xFF2E3C43.toInt()) {
            if (!start(Intent(Settings.ACTION_SETTINGS))) toast("Couldn't open Settings")
        }
    }

    private fun addShortcut(title: String, subtitle: String, top: Int, bottom: Int, onClick: () -> Unit) {
        val bg = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(top, bottom))
        val tile = card(bg, onClick)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        box.addView(TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        box.addView(TextView(this).apply {
            text = subtitle
            setTextColor(0xB3FFFFFF.toInt())
            textSize = 14f
            gravity = Gravity.CENTER
        })
        tile.addView(box, FrameLayout.LayoutParams(-1, -1))
        shortcutsRow.addView(tile)
    }

    // ---- App tiles -----------------------------------------------------------

    private fun populateApps() {
        appsRow.removeAllViews()
        val pm = packageManager
        for (ri in loadApps()) {
            val label = ri.loadLabel(pm).toString()
            val banner = ri.activityInfo.loadBanner(pm)
            val intent = Intent(Intent.ACTION_MAIN)
                .setClassName(ri.activityInfo.packageName, ri.activityInfo.name)

            val bg = GradientDrawable().apply { setColor(0xFF2A2740.toInt()) }
            val tile = card(bg) {
                if (!start(intent)) toast("Couldn't open $label")
            }

            if (banner != null) {
                tile.addView(ImageView(this).apply {
                    setImageDrawable(banner)
                    scaleType = ImageView.ScaleType.FIT_XY
                }, FrameLayout.LayoutParams(-1, -1))
            } else {
                val box = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                }
                box.addView(ImageView(this).apply {
                    setImageDrawable(ri.loadIcon(pm))
                }, LinearLayout.LayoutParams(dp(64), dp(64)))
                box.addView(TextView(this).apply {
                    text = label
                    setTextColor(Color.WHITE)
                    textSize = 15f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    setPadding(dp(8), dp(6), dp(8), 0)
                })
                tile.addView(box, FrameLayout.LayoutParams(-1, -1))
            }
            appsRow.addView(tile)
        }
    }

    private fun loadApps(): List<ResolveInfo> {
        val pm = packageManager
        val leanback = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0
        )
        val normal = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
        val seen = HashSet<String>()
        return (leanback + normal)
            .filter { it.activityInfo.packageName != packageName && seen.add(it.activityInfo.packageName) }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
    }

    // ---- View helpers --------------------------------------------------------

    private fun card(bg: GradientDrawable, onClick: () -> Unit): FrameLayout {
        val r = dp(radius).toFloat()
        bg.cornerRadius = r
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(cardW), dp(cardH)).apply {
                setMargins(dp(12), dp(16), dp(12), dp(16))
            }
            background = bg
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, r)
                }
            }
            isFocusable = true
            isClickable = true
            setOnClickListener { onClick() }
            setOnFocusChangeListener { v, hasFocus ->
                val s = if (hasFocus) 1.1f else 1f
                v.animate().scaleX(s).scaleY(s).setDuration(150).start()
                v.elevation = if (hasFocus) dp(12).toFloat() else 0f
                v.foreground = if (hasFocus) GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                    setStroke(dp(3), Color.WHITE)
                    cornerRadius = r
                } else null
            }
        }
    }

    private fun makeRow(parent: LinearLayout): LinearLayout {
        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipChildren = false
            clipToPadding = false
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
        }
        scroll.addView(row)
        parent.addView(scroll, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        return row
    }

    private fun sectionTitle(text: String) = TextView(this).apply {
        this.text = text
        setTextColor(0xCCFFFFFF.toInt())
        textSize = 20f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        setPadding(dp(12), dp(24), 0, dp(4))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    // ---- Launching -----------------------------------------------------------

    private fun start(intent: Intent): Boolean = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }

    private fun launchPackage(pkg: String): Boolean {
        val i = packageManager.getLeanbackLaunchIntentForPackage(pkg)
            ?: packageManager.getLaunchIntentForPackage(pkg)
            ?: return false
        return start(i)
    }

    private fun openComponent(pkg: String, cls: String, label: String) {
        val ok = start(Intent().setClassName(pkg, cls)) || launchPackage(pkg)
        if (!ok) toast("Couldn't open $label")
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
