package eus.alaintxu.toq_alternative_notifications.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eus.alaintxu.toq_alternative_notifications.R;

/**
 * Created by aperez on 15/12/14.
 */
public class AppListActivity extends Activity {
    private ArrayList<MyApplicationInfo> mApplications;
    private ListView lv;
    private AppListItemAdapter alia;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings);
        mPrefs = getSharedPreferences("ToqAN",0);
        drawList();
    }

    private void loadApplications() {
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

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
                if (pkgs != null && pkgs.contains(application.pkg.toString()))
                    application.notify = true;
                else
                    application.notify = false;

                mApplications.add(application);
            }
        }
    }
    public void drawList(){
        lv = (ListView) findViewById(R.id.appsListView);
        loadApplications();
        alia = new AppListItemAdapter(this, android.R.layout.simple_list_item_1, mApplications);
        lv.setAdapter(alia);
    }

    public void onCheckBoxClicked(View v){
        Boolean all = false;
        Boolean checked = ((CheckBox) v).isChecked();
        switch (v.getId()) {
            case R.id.all_apps_checkbox:
                all = true;
            case R.id.none_apps_checkbox:
                if (checked) {
                    // check/uncheck every app
                    setAppCheckboxes(all);
                    alia.notifyDataSetChanged();
                }
                setAllCheckbox(all);
                setNoneCheckbox(!all);
                break;
            default:
                int position = (int)v.getTag();
                mApplications.get(position).notify = checked;
                if (checked) {
                    // If any app is checked, uncheck "None" checkbox
                    setNoneCheckbox(false);
                } else {
                    // If any app is unchecked, uncheck "All" checkbox
                    setAllCheckbox(false);
                }
                break;
        }
        savePreferences();
    }

    public void setAppCheckboxes(Boolean checked){
        for (MyApplicationInfo app : mApplications){
            app.notify = checked;
        }
    }
    public void setAllCheckbox(Boolean checked) {
        CheckBox all = (CheckBox)findViewById(R.id.all_apps_checkbox);
        all.setChecked(checked);
    }
    public void setNoneCheckbox(Boolean checked){
        CheckBox none = (CheckBox)findViewById(R.id.none_apps_checkbox);
        none.setChecked(checked);
    }

    public void savePreferences(){
        SharedPreferences.Editor editor = mPrefs.edit();
        Set<String> pkgs = new HashSet<String>();
        for (MyApplicationInfo app : mApplications){
            if (app.notify) {
                pkgs.add(app.pkg.toString());
            }
        }
        editor.putStringSet("pkgs",pkgs).commit();

    }
}
