package eus.alaintxu.toq_alternative_notifications;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import eus.alaintxu.toq_alternative_notifications.settings.AppListActivity;
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
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_apps:
                openSettings();
                return true;
            case R.id.action_about:
                String url = "http://alaintxu.github.io/toq_alternative_notifications";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            case R.id.action_stop:
                toqInterface.destroy();

                /* I don't know how to kill NotificationListener service */

                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        // Send user to Notification settings
        Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);
    }

    public void openSettings(){
        Intent intent = new Intent(this, AppListActivity.class);
        startActivity(intent);
    }

    public void collapseView(View v){
        int view_id = -1;
        switch (v.getId()){
            case R.id.steps_install_deck_of_card_button:
                view_id = R.id.steps_install_deck_of_card;
                break;
            case R.id.steps_connection_button:
                view_id = R.id.steps_connection;
                break;
            case R.id.steps_permissions_button:
                view_id = R.id.steps_permissions;
                break;
            case R.id.steps_duplicate_notifications_button:
                view_id = R.id.steps_duplicate_notifications;
                break;
            case R.id.steps_uninstall_deck_of_card_button:
                view_id = R.id.steps_uninstall_deck_of_card;
                break;
        }

        if (view_id != -1){
            toggleVisibility(view_id);
        }
    }

    private void toggleVisibility(int viewId) {
        TextView tv = (TextView) this.findViewById(viewId);
        if (tv.getVisibility()==TextView.VISIBLE){
            tv.setVisibility(TextView.GONE);
        }else{
            tv.setVisibility(TextView.VISIBLE);
        }
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
            case R.id.check_connection_button:
                ToqNotification toqNotification = new ToqNotification();
                toqNotification.setWhen(System.currentTimeMillis());
                toqNotification.setTitle(getString(R.string.test_notification_title));
                toqNotification.setAppName("ToqAN");
                toqNotification.setText(getString(R.string.test_notification_text));
                toqInterface.notifyToq(toqNotification);
                break;
        }
    }
}