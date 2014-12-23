package eus.alaintxu.toq_alternative_notifications;

import android.app.ActionBar;
import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.widget.TextView;

import eus.alaintxu.toq_alternative_notifications.app_lists.AppletAppRecyclerFragment;
import eus.alaintxu.toq_alternative_notifications.app_lists.NotificationAppRecyclerFragment;
import eus.alaintxu.toq_alternative_notifications.navigation.NavigationDrawerFragment;
import eus.alaintxu.toq_alternative_notifications.toq.ToqInterface;
import eus.alaintxu.toq_alternative_notifications.toq.ToqNotification;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private ToqInterface toqInterface = null;
    private Fragment currentFragment = null;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ToqAN", "ToqAN.onCreate");

        // enable transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_main);

        // Set up Navigation drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    }

    protected void onStart(){
        super.onStart();

        Log.d("ToqAN", "ToqAN Main activity started");

        // Initialize toq interface
        toqInterface = ToqInterface.getInstance();
        toqInterface.initToqInterface(this);
        toqInterface.start();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position){
            case 1:
                currentFragment = NotificationAppRecyclerFragment.newInstance(position+1);
                break;
            case 2:
                currentFragment = AppletAppRecyclerFragment.newInstance(position + 1);
                break;
            case 3:
                String url = "http://alaintxu.github.io/toq_alternative_notifications";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                break;
            case 4:
                stopAndExit();
                break;
            default:
                currentFragment = MainFragment.newInstance(position + 1);
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, currentFragment);
        //ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        ft.commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.section_main);
                break;
            case 2:
                mTitle = getString(R.string.section_notifications);
                break;
            case 3:
                mTitle = getString(R.string.section_applet);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_select_all:
            case R.id.action_select_none:
            case R.id.action_update_list:
                currentFragment.onOptionsItemSelected(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * checks if it has permissions to read notifications
     */
    public void checkNotificationPermissions(){
        // Send user to Notification settings
        Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
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
        final TextView tv = (TextView) findViewById(viewId);
        int visibility;
        if (tv.getVisibility()==TextView.VISIBLE){
            visibility = TextView.GONE;
        }else{
            visibility = TextView.VISIBLE;
        }
        tv.setVisibility(visibility);
        /*LinearLayout stepll = (LinearLayout)tv.getParent();
        LinearLayout steplistll = (LinearLayout)stepll.getParent();
        ScrollView steplistsv = (ScrollView)steplistll.getParent();

        stepll.animate();
        steplistll.animate();
        steplistsv.animate();*/
    }

    /**
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
                checkConnection();
                break;
        }
    }

    private void checkConnection() {
        try {
            ToqNotification toqNotification = new ToqNotification();
            toqNotification.setWhen(System.currentTimeMillis());
            toqNotification.setTitle(getString(R.string.test_notification_title));
            toqNotification.setAppName("ToqAN");
            toqNotification.setText(getString(R.string.test_notification_text));
            toqInterface.notifyToq(toqNotification);
        }catch (Exception e){
            Log.e("ToqAN","Unabled to sent test notification to the smartwatch");
        }
        try {
            NotificationListener nl = NotificationListener.getInstance();
            if (nl != null) {
                nl.updateDeckOfCardsWithStatusBarNotifications();
            } else {
                toqInterface.setStatus(getResources().getString(R.string.notification_service_null));
                Log.e("ToqAN","Notification Listener Service not started.");
                return;
            }
        }catch (Exception e){
            Log.e("ToqAN","Unabled to update status bar notifications from button.");
            return;
        }
        toqInterface.setStatus(getResources().getString(R.string.status_connected));
    }

    public void onCheckBoxClicked(View v){
        if (currentFragment.getClass().equals(NotificationAppRecyclerFragment.class)){
            ((NotificationAppRecyclerFragment)currentFragment).onCheckBoxClicked(v);
        }else if (currentFragment.getClass().equals(AppletAppRecyclerFragment.class)){
            ((AppletAppRecyclerFragment)currentFragment).onCheckBoxClicked(v);
        }
    }
    private void stopAndExit(){
        toqInterface.destroy();
        /* I don't know how to kill NotificationListener service */
        finish();
    }

}
