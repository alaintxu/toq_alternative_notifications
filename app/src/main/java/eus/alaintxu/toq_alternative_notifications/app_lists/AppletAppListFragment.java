package eus.alaintxu.toq_alternative_notifications.app_lists;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eus.alaintxu.toq_alternative_notifications.MainActivity;
import eus.alaintxu.toq_alternative_notifications.R;

public class AppletAppListFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ArrayList<MyApplicationInfo> mApplications;
    private ListView lv;
    private AppListItemAdapter alia;
    private SharedPreferences mPrefs;
    private Boolean cb_all = true;
    private Boolean cb_same_as_notifications = false;


    public static AppletAppListFragment newInstance(int sectionNumber) {
        AppletAppListFragment fragment = new AppletAppListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AppletAppListFragment() {}


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getActivity().getSharedPreferences("ToqAN",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_applet_app_list, container, false);
    }
    @Override
    public void onActivityCreated(Bundle b){
        super.onActivityCreated(b);
        drawList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Boolean all = false;
        switch (item.getItemId()){
            case R.id.action_select_all:
                all = true;
            case R.id.action_select_none:
                setAppCheckboxes(all);
                savePreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadApplications() {
        PackageManager manager = getActivity().getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        // First Options (all & same as notifications)
        Boolean applet_same_as_notifications = mPrefs.getBoolean("applet_same_as_notifications",false);
        setSameAsCheckBox(applet_same_as_notifications);
        Boolean applet_all = mPrefs.getBoolean("applet_all",true);
        setAllCheckBox(applet_all);
        Boolean enabled = true;
        if (cb_all || cb_same_as_notifications){
            enabled = false;
        }

        // Apps
        if (apps != null) {
            final int count = apps.size();

            if (mApplications == null) {
                mApplications = new ArrayList<MyApplicationInfo>(count);
            }
            mApplications.clear();
            Set<String> pkgs = mPrefs.getStringSet("pkgs",null);

            for (int i = 0; i < count; i++) {
                MyApplicationInfo application = new MyApplicationInfo();
                ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(manager);
                application.pkg = info.activityInfo.applicationInfo.packageName;
                application.icon = info.activityInfo.loadIcon(manager);

                application.enabled = enabled;

                if (pkgs != null && pkgs.contains(application.pkg.toString()))
                    application.notify = true;
                else
                    application.notify = false;
                if(!pkgAllreadyExists(application.pkg)) {
                    mApplications.add(application);
                }
            }
        }
    }
    public void drawList(){
        lv = (ListView) getView().findViewById(R.id.appsListView);
        loadApplications();
        alia = new AppListItemAdapter(getActivity(), android.R.layout.simple_list_item_1, mApplications);
        lv.setAdapter(alia);
    }

    public void onCheckBoxClicked(View v){
        Boolean checked = ((CheckBox) v).isChecked();
        switch(v.getId()){
            case R.id.applet_all:
                setAllCheckBox(checked);
                if (checked)
                    setSameAsCheckBox(false);
                break;
            case R.id.applet_same_as_notifications:
                setSameAsCheckBox(checked);
                if (checked)
                    setAllCheckBox(false);
                break;
            default:
                setAllCheckBox(false);
                setSameAsCheckBox(false);
                int position = (int)v.getTag();
                mApplications.get(position).notify = checked;
                break;
        }

        Boolean enabled = true;
        if (cb_all || cb_same_as_notifications){
            enabled = false;
        }
        setAppCheckBoxEnabled(enabled);

        savePreferences();
    }

    public void setAppCheckboxes(Boolean checked){
        for (MyApplicationInfo app : mApplications){
            app.notify = checked;
        }
    }

    public void setAppCheckBoxEnabled(Boolean enabled){
        for (MyApplicationInfo app : mApplications){
            app.enabled = enabled;
        }
    }

    public void setAllCheckBox(Boolean checked){
        cb_all = checked;
        ((CheckBox)getView().findViewById(R.id.applet_all)).setChecked(checked);
    }
    public void setSameAsCheckBox(Boolean checked){
        cb_same_as_notifications = checked;
        ((CheckBox)getView().findViewById(R.id.applet_same_as_notifications)).setChecked(checked);
    }

    public void savePreferences(){
        SharedPreferences.Editor editor = mPrefs.edit();
        Set<String> pkgs = new HashSet<String>();
        for (MyApplicationInfo app : mApplications){
            if (app.notify) {
                pkgs.add(app.pkg.toString());
            }
        }
        editor.remove("pkgs");
        editor.putStringSet("pkgs",pkgs).commit();

        editor.remove("applet_all");
        editor.putBoolean("applet_all",cb_all).commit();

        editor.remove("applet_same_as_notifications");
        editor.putBoolean("applet_same_as_notifications",cb_same_as_notifications).commit();

        alia.notifyDataSetChanged();
    }

    public Boolean pkgAllreadyExists(CharSequence pkg){
        Boolean exists = false;

        for(MyApplicationInfo app : mApplications){
            if(app.pkg.equals(pkg)){
                exists = true;
                break;
            }
        }

        return exists;
    }
}

