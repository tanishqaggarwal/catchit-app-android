package com.tanishqaggarwal.catchit.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.tanishqaggarwal.catchit.R;
import com.tanishqaggarwal.catchit.ui.TrackNowActivity;

/**
 * Created by Tanishq on 5/9/2016.
 */
public class BusIsAlmostHereReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        notify(context);
    }

    public void notify(Context context) {
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, new Intent(context, TrackNowActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        String message = "Your late bus should almost be here. Tap this to track it in real time.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.ic_stat_bus_is_here)
                .setTicker(message)
                .setContentTitle("Your late bus is almost here")
                .setContentText(message)
                .addAction(R.drawable.ic_stat_bus_is_here, "Action Button", pIntent)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.notify(0, builder.build());
    }
}