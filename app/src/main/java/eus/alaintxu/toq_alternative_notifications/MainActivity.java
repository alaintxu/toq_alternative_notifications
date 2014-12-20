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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import eus.alaintxu.toq_alternative_notifications.app_lists.AppletAppListFragment;
import eus.alaintxu.toq_alternative_notifications.app_lists.NotificationsAppListFragment;
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

        // Initialize toq interface
        toqInterface = ToqInterface.getInstance();
        toqInterface.initToqInterface(this);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    }

    protected void onStart(){

        super.onStart();

        Log.d("ToqAN", "ToqAN.onStart");

        toqInterface.start();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position){
            case 1:
                currentFragment = NotificationsAppListFragment.newInstance(position +1);
                break;
            case 2:
                currentFragment = AppletAppListFragment.newInstance(position +1);
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
            case 4:
                mTitle = getString(R.string.action_stop);
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
            /*case R.id.action_about:
                String url = "http://alaintxu.github.io/toq_alternative_notifications";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                break;*/
            case R.id.action_select_all:
            case R.id.action_select_none:
                currentFragment.onOptionsItemSelected(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    /*public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Animation
            //Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
            //container.startAnimation(animation);
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }*/


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
        LinearLayout stepll = (LinearLayout)tv.getParent();
        LinearLayout steplistll = (LinearLayout)stepll.getParent();
        ScrollView steplistsv = (ScrollView)steplistll.getParent();

        /*stepll.animate();
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
                ToqNotification toqNotification = new ToqNotification();
                toqNotification.setWhen(System.currentTimeMillis());
                toqNotification.setTitle(getString(R.string.test_notification_title));
                toqNotification.setAppName("ToqAN");
                toqNotification.setText(getString(R.string.test_notification_text));
                toqInterface.notifyToq(toqNotification);
                break;
        }
    }

    public void onCheckBoxClicked(View v){
        if (currentFragment.getClass().equals(NotificationsAppListFragment.class)){
            ((NotificationsAppListFragment)currentFragment).onCheckBoxClicked(v);
        }else if (currentFragment.getClass().equals(AppletAppListFragment.class)){
            ((AppletAppListFragment)currentFragment).onCheckBoxClicked(v);
        }
    }
    private void stopAndExit(){
        toqInterface.destroy();
        /* I don't know how to kill NotificationListener service */
        finish();
    }

}
