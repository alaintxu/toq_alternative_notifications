package eus.alaintxu.toq_alternative_notifications;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import eus.alaintxu.toq_alternative_notifications.toq.ToqInterface;
import eus.alaintxu.toq_alternative_notifications.toq.ToqNotification;

/**
 * Created by aperez on 5/12/14.
 */
public class NotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("ToqAN","onNotificationPosted");
        // Get Singleton ToqInterface Object
        ToqInterface toqInterface = ToqInterface.getInstance();

        // Send Notification
        toqInterface.notifyToq(new ToqNotification(sbn));

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
