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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eus.alaintxu.toq_alternative_notifications.MainActivity;
import eus.alaintxu.toq_alternative_notifications.R;

public class NotificationsAppListFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ArrayList<MyApplicationInfo> mApplications;
    private ListView lv;
    private AppListItemAdapter alia;
    private SharedPreferences mPrefs;


    public static NotificationsAppListFragment newInstance(int sectionNumber) {
        NotificationsAppListFragment fragment = new NotificationsAppListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public NotificationsAppListFragment() {}


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPrefs = activity.getSharedPreferences("ToqAN",0);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Animation
        Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        container.startAnimation(animation);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications_app_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle b){
        super.onActivityCreated(b);
        lv = (ListView) getView().findViewById(R.id.appsListView);
        lv.setHeaderDividersEnabled(true);
        View headerView = getActivity().getLayoutInflater().inflate(R.layout.fragment_notifications_app_list_header, null);
        lv.addHeaderView(headerView);
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
                application.enabled = true;
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
        loadApplications();
        alia = new AppListItemAdapter(getActivity(), android.R.layout.simple_list_item_1, mApplications);
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
        alia.notifyDataSetChanged();
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
