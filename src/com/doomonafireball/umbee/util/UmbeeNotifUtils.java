package com.doomonafireball.umbee.util;

import com.doomonafireball.umbee.R;
import com.doomonafireball.umbee.activity.StartupActivity;
import com.doomonafireball.umbee.model.NoaaByDay;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * User: derek Date: 6/5/12 Time: 12:16 PM
 */
public class UmbeeNotifUtils {

    public static void createNotification(Context context, NoaaByDay nbd) {
        SharedPrefsManager mSPM = SharedPrefsManager.getInstance();

        int icon = R.drawable.icon_notif;
        long when = System.currentTimeMillis();
        int type = mSPM.getAlertType();
        int morPrecip = 0;
        int evePrecip = 0;
        try {
            if (mSPM.getNotifyTomorrow()) {
                morPrecip = nbd.mPop.probabilities.get(1).first;
                evePrecip = nbd.mPop.probabilities.get(1).second;
            } else {
                morPrecip = nbd.mPop.probabilities.get(0).first;
                evePrecip = nbd.mPop.probabilities.get(0).second;
            }
        } catch (IndexOutOfBoundsException e) {
            // User has not gotten any data yet!
            morPrecip = 100;
            evePrecip = 100;
        }
        int highestPercentage = Math.max(morPrecip, evePrecip);
        CharSequence ticker = "", title = "", text = "";
        switch (type) {
            case Refs.ALERT_SIMPLE:
                int threshold = 50;
                if (mSPM.getCustomThreshold()) {
                    threshold = mSPM.getSingleThreshold();
                }
                if (highestPercentage > threshold) {
                    ticker = context.getString(R.string.umbee_thinks_yes);
                    title = context.getString(R.string.umbee_thinks_yes);
                } else {
                    ticker = context.getString(R.string.umbee_thinks_no);
                    title = context.getString(R.string.umbee_thinks_no);
                }
                text = getSimpleMorningText(context, morPrecip)
                        + "\n"
                        + getSimpleEveningText(context, evePrecip);
                break;
            case Refs.ALERT_COMPLEX:
                int t1 = 25;
                int t2 = 50;
                int t3 = 75;
                if (mSPM.getCustomThreshold()) {
                    t1 = mSPM.getTripleThreshold1();
                    t2 = mSPM.getTripleThreshold2();
                    t3 = mSPM.getTripleThreshold3();
                }
                if (highestPercentage > t3) {
                    // Definitely
                    ticker = context.getString(R.string.umbee_thinks_yes);
                    title = context.getString(R.string.umbee_thinks_yes);
                } else if (highestPercentage > t2) {
                    // Probably
                    ticker = context.getString(R.string.umbee_thinks_probs);
                    title = context.getString(R.string.umbee_thinks_probs);
                } else if (highestPercentage > t1) {
                    // Maybe
                    ticker = context.getString(R.string.umbee_thinks_maybe);
                    title = context.getString(R.string.umbee_thinks_maybe);
                } else {
                    // Nope
                    ticker = context.getString(R.string.umbee_thinks_no);
                    title = context.getString(R.string.umbee_thinks_no);
                }
                text = getSimpleMorningText(context, morPrecip)
                        + "\n"
                        + getSimpleEveningText(context, evePrecip);
                break;
            case Refs.ALERT_PERCENT:
                // Not in use
                break;
            default:
                int threshold2 = mSPM.getSingleThreshold();
                if (highestPercentage > threshold2) {
                    ticker = context.getString(R.string.umbee_thinks_yes);
                    title = context.getString(R.string.umbee_thinks_yes);
                } else {
                    ticker = context.getString(R.string.umbee_thinks_no);
                    title = context.getString(R.string.umbee_thinks_no);
                }
                text = getSimpleMorningText(context, morPrecip)
                        + "\n"
                        + getSimpleEveningText(context, evePrecip);
                break;
        }

        PreFroyoNotificationStyleDiscover pfnsd = new PreFroyoNotificationStyleDiscover(context);

        PendingIntent pi = PendingIntent
                .getActivity(context, 0, new Intent(context, StartupActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.two_line_notif);
        contentView.setImageViewResource(R.id.image, R.drawable.icon_notif);
        contentView.setTextViewText(R.id.title, title);
        contentView.setTextViewText(R.id.text, text);
        contentView.setTextColor(R.id.title, pfnsd.getTitleColor());
        contentView.setTextColor(R.id.text, pfnsd.getTextColor());
        contentView.setFloat(R.id.title, "setTextSize", pfnsd.getTitleSize());
        contentView.setFloat(R.id.text, "setTextSize", pfnsd.getTextSize());

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle("" + morPrecip + "%, " + evePrecip + "%")
                        .setContentText(ticker)
                        .setContent(contentView)
                        .setContentIntent(pi);

        // Get an instance of the NotificationManager service
        // This is the Android Waer implementation!
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // This is the non-Android Wear implementation!
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(1, notificationBuilder.build());

        //Notification notif = new Notification(icon, ticker, when);
        //notif.setLatestEventInfo(ctx, title, text, pi);
        //notif.contentView = contentView;
        //notif.contentIntent = pi;
        //notif.flags |= Notification.FLAG_AUTO_CANCEL;
        //mNM.notify(1, notif);
    }

    public static String getSimpleMorningText(Context ctx, int percentage) {
        return String.format(ctx.getString(R.string.dynamic_simple_morning), percentage);
    }

    public static String getSimpleEveningText(Context ctx, int percentage) {
        return String.format(ctx.getString(R.string.dynamic_simple_evening), percentage);
    }

}
