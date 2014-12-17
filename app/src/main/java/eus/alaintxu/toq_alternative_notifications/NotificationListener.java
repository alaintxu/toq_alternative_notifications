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
    private SharedPreferences mPrefs = null;
    private Map<CharSequence,CharSequence> appNames = null;
    private ToqInterface toqInterface = null;

    @Override
    public void onCreate(){
        Log.d("ToqAN", "NotificationListener created");
        super.onCreate();
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

        Set<String> pkgs = mPrefs.getStringSet("pkgs",null);

        if (pkgs != null && pkgs.contains(sbn.getPackageName())) {
            toqInterface.notifyToq(new ToqNotification(sbn,appNames));
        }
        updateDeckOfCardsWithStatusBarNotifications();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d("ToqAN", "Notification removed detected");
        checkInitializations();
        updateDeckOfCardsWithStatusBarNotifications();
    }

    private void updateDeckOfCardsWithStatusBarNotifications() {
        // Update DeckOfCards with Status Bar Notifications
        StatusBarNotification[] sbns = this.getActiveNotifications();
        toqInterface.updateDeckOfCardsWithNotifications(sbns,appNames);
    }

    private void checkInitializations(){
        if (mPrefs == null)
            mPrefs = getBaseContext().getSharedPreferences("ToqAN", 0);
        if (appNames == null)
            updateAppNames();
        if (toqInterface == null)
            toqInterface = ToqInterface.getInstance();

    }


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
