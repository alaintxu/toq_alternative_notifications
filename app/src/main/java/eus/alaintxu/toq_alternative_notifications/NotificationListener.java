package eus.alaintxu.toq_alternative_notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eus.alaintxu.toq_alternative_notifications.toq.ToqInterface;
import eus.alaintxu.toq_alternative_notifications.toq.ToqNotification;

/**
 * Created by aperez on 5/12/14.
 */
public class NotificationListener extends NotificationListenerService {
    private static NotificationListener instance;
    private SharedPreferences mPrefs = null;
    private Map<CharSequence,CharSequence> appNames = null;
    private ToqInterface toqInterface = null;

    public static NotificationListener getInstance(){
        return instance;
    }
    @Override
    public void onCreate(){
        Log.d("ToqAN", "NotificationListener created");
        super.onCreate();
        instance = this;
    }

    @Override
    public IBinder onBind(Intent i){
        Log.d("ToqAN", "NotificationListener Binded");
        return super.onBind(i);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("ToqAN", "New notification detected");
        checkInitializations();

        // Get packages in the whitelist
        Set<String> pkgs = mPrefs.getStringSet("pkgs",null);

        // if current notification package is in whitelist, notify it.
        if (pkgs != null && pkgs.contains(sbn.getPackageName())) {
            toqInterface.notifyToq(new ToqNotification(sbn,appNames));
        }

        // Update Deck of Cards
        updateDeckOfCardsWithStatusBarNotifications();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d("ToqAN", "Notification removed detected");
        checkInitializations();

        // Update Deck of Cards
        updateDeckOfCardsWithStatusBarNotifications();
    }

    /**
     * Updates DeckOfCards with Status Bar Notifications.
     * It does not check whitelist, the deck of cards shows
     * all notifications.
     */
    private void updateDeckOfCardsWithStatusBarNotifications() {
        StatusBarNotification[] sbns = this.getActiveNotifications();
        toqInterface.updateDeckOfCardsWithNotifications(sbns,appNames);
    }

    /**
     * Initializates private variables and updates app names.
     */
    private void checkInitializations(){
        if (mPrefs == null) {
            mPrefs = getBaseContext().getSharedPreferences("ToqAN", 0);
        }
        /*
         * Always update appNames, you never know when the user has
         * installed/uninstalled an app.
         */
        updateAppNames();

        if (toqInterface == null) {
            toqInterface = ToqInterface.getInstance();
        }
    }

    /**
     * Gets all installed apps and saves in Package->AppName mapping.
     */
    public void updateAppNames(){
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appNames = new HashMap<CharSequence,CharSequence>();

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        for (ResolveInfo app : apps){
            CharSequence pkg = app.activityInfo.applicationInfo.packageName;
            CharSequence appName = app.loadLabel(manager);
            appNames.put(pkg,appName);
        }
    }
}
