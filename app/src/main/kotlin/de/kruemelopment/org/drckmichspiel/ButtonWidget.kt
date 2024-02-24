package de.kruemelopment.org.drckmichspiel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class ButtonWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_button)
            val clickIntent = Intent(context, MainActivity::class.java)
            clickIntent.putExtra("Drück mich", "default")
            val clickPI: PendingIntent =
                PendingIntent.getActivity(
                    context, 0,
                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            views.setOnClickPendingIntent(R.id.button29, clickPI)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
