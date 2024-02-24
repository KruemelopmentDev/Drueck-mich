package de.kruemelopment.org.drckmichspiel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class WigetListe : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val rv = RemoteViews(context.packageName, R.layout.wiget_liste)
            val intent = Intent(context, WidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
            rv.setRemoteAdapter(R.id.widgetlistview, intent)
            val clickIntent = Intent(context, MainActivity::class.java)
            val clickPI: PendingIntent? =
                PendingIntent.getActivity(
                    context, 0,
                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            rv.setPendingIntentTemplate(R.id.widgetlistview, clickPI)
            appWidgetManager.updateAppWidget(appWidgetId, rv)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}
