package eus.alaintxu.toq_alternative_notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Set;

import eus.alaintxu.toq_alternative_notifications.toq.ToqInterface;
import eus.alaintxu.toq_alternative_notifications.toq.ToqNotification;

/**
 * Created by aperez on 5/12/14.
 */
public class NotificationListener extends NotificationListenerService {
    private SharedPreferences mPrefs;

    @Override
    public IBinder onBind(Intent intent){
        IBinder mIBinder = super.onBind(intent);
        mPrefs = getBaseContext().getSharedPreferences("ToqAN", 0);
        return mIBinder;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("ToqAN", "onNotificationPosted");

        // Get Singleton ToqInterface Object
        ToqInterface toqInterface = ToqInterface.getInstance();

        Set<String> pkgs = mPrefs.getStringSet("pkgs",null);
        if (pkgs != null && pkgs.contains(sbn.getPackageName())) {
            // If pkg is in whitelist, send Notification
            toqInterface.notifyToq(new ToqNotification(sbn));
        }
        // Update DeckOfCards with Status Bar Notifications
        StatusBarNotification[] sbns = this.getActiveNotifications();
        toqInterface.updateDeckOfCardsWithNotifications(sbns);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d("ToqAN","onNotificationRemoved");
        // Get Singleton ToqInterface Object
        ToqInterface toqInterface = ToqInterface.getInstance();

        // Update DeckOfCards with Status Bar Notifications
        StatusBarNotification[] sbns = this.getActiveNotifications();
        toqInterface.updateDeckOfCardsWithNotifications(sbns);
    }
}
