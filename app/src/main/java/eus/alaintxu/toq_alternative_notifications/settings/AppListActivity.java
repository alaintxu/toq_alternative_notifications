package eus.alaintxu.toq_alternative_notifications.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_list_actions,menu);
        return super.onCreateOptionsMenu(menu);
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
                if(!pkgAllreadyExists(application.pkg)) {
                    mApplications.add(application);
                }
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
        Boolean checked = ((CheckBox) v).isChecked();
        int position = (int)v.getTag();
        mApplications.get(position).notify = checked;
        savePreferences();
    }

    public void setAppCheckboxes(Boolean checked){
        for (MyApplicationInfo app : mApplications){
            app.notify = checked;
        }
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
