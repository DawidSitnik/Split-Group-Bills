package com.example.sitnik.onetoonechat;


import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


//tutaj w NotificationCompat mozemy zmienic obrazek powiadomienia
public class MessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                //.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("New Friend Request")
                .setContentText("You have received your friend request")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMnr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMnr.notify(mNotificationId, mBuilder.build());




    }

}
