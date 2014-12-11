package eus.alaintxu.toq_alternative_notifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import eus.alaintxu.toq_alternative_notifications.toq.ToqInterface;
import eus.alaintxu.toq_alternative_notifications.toq.ToqNotification;


/**
 * Main activity
 */
public class ToqAlternativeNotifications extends Activity{
    ToqInterface toqInterface = null;

    public void onCreate(Bundle icicle){
                
        super.onCreate(icicle);

        Log.d("ToqAN", "ToqAN.onCreate");

        setContentView(R.layout.main);

        toqInterface = ToqInterface.getInstance();
        toqInterface.initToqInterface(this);
    }

    protected void onStart(){

        super.onStart();

        Log.d("ToqAN", "ToqAN.onStart");

        toqInterface.start();
    }
    
    /*
    checks if it has permissions to read notifications
     */
    public void checkNotificationPermissions(){
        /*ContentResolver contentResolver = this.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = this.getPackageName();
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)){
            Toast.makeText(this.getApplicationContext(),"Notification disabled",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this.getApplicationContext(),"Notification enabled",Toast.LENGTH_LONG).show();
        }*/

        // Send user to Notification settings
        Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);
    }

    /*
     * onClick action listener for every button
     */
    public void uiButtonClicked(View v){
        switch (v.getId()) {
            case R.id.doc_install_button:
                toqInterface.installDeckOfCards();
                break;
            case R.id.doc_uninstall_button:
                toqInterface.uninstallDeckOfCards();
                break;
            case R.id.doc_check_permissions:
                checkNotificationPermissions();
                break;
            case R.id.send_notification_button:
                ToqNotification toqNotification = new ToqNotification();
                toqNotification.setWhen(System.currentTimeMillis());
                toqNotification.setTitle(getString(R.string.test_notification_title));
                toqNotification.setText(getString(R.string.test_notification_text));
                toqInterface.notifyToq(toqNotification);
                break;
            case R.id.end_service_button:
                toqInterface.destroy();

                /* I don't know how to kill NotificationListener service */

                finish();
                break;
        }
    }
}