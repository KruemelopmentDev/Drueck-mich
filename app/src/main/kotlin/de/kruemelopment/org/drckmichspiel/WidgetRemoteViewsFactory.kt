package de.kruemelopment.org.drckmichspiel

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import java.io.File
import java.util.Collections

class WidgetRemoteViewsFactory internal constructor(private val context: Context) :
    RemoteViewsFactory {
    private val childs: MutableList<String?> = ArrayList()

    init {
        updateWidgetListview()
    }

    private fun updateWidgetListview() {
        childs.clear()
        val file = File(context.getExternalFilesDir(null).toString() + "")
        val array = file.list()!!
        if (array.isEmpty()) childs.add(context.getString(R.string.no_dms)) else {
            Collections.addAll(childs, *array)
            var i = 0
            while (i < childs.size) {
                if (!childs[i]!!.contains(".drmch")) {
                    childs.removeAt(i)
                    i -= 1
                }
                i++
            }
        }
        if (childs.isEmpty()) childs.add(context.getString(R.string.no_dms))
        Collections.sort(childs, java.lang.String.CASE_INSENSITIVE_ORDER)
    }

    override fun onCreate() {
        updateWidgetListview()
    }

    override fun onDataSetChanged() {
        updateWidgetListview()
    }

    override fun onDestroy() {}
    override fun getCount(): Int {
        return childs.size
    }

    override fun getViewAt(i: Int): RemoteViews {
        val remoteViews: RemoteViews
        if (childs[i] == context.getString(R.string.no_dms)) {
            remoteViews = RemoteViews(context.packageName, R.layout.widgetlistitem)
            remoteViews.setTextViewText(R.id.textView5, childs[i])
            val `in` = Intent(context, MainActivity::class.java)
            remoteViews.setOnClickFillInIntent(R.id.textView5, `in`)
            remoteViews.setOnClickFillInIntent(R.id.ralla, `in`)
        } else {
            remoteViews = RemoteViews(context.packageName, R.layout.widgetlistitem)
            remoteViews.setTextViewText(R.id.textView5, childs[i]!!.replace(".drmch", ""))
            val `in` = Intent()
            `in`.putExtra("Drück mich", childs[i])
            remoteViews.setOnClickFillInIntent(R.id.textView5, `in`)
            remoteViews.setOnClickFillInIntent(R.id.ralla, `in`)
        }
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return childs.size
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }
}
