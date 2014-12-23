package eus.alaintxu.toq_alternative_notifications.app_lists;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eus.alaintxu.toq_alternative_notifications.MainActivity;
import eus.alaintxu.toq_alternative_notifications.R;

public class NotificationAppRecyclerFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<MyApplicationInfo> mDataset;
    //private MyApplicationInfo[] mDataset;
    private SharedPreferences mPrefs;



    public static NotificationAppRecyclerFragment newInstance(int sectionNumber) {
        NotificationAppRecyclerFragment fragment = new NotificationAppRecyclerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public NotificationAppRecyclerFragment() {}
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Animation
        Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        container.startAnimation(animation);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.notifications_app_list, container, false);
    }


    @Override
    public void onActivityCreated(Bundle b){
        super.onActivityCreated(b);
        mPrefs = getActivity().getSharedPreferences("ToqAN",0);
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.notifications_app_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        loadApplications();

        reorderDataset();
    }



    private void loadApplications() {
        try {
            PackageManager manager = getActivity().getPackageManager();

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
            Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

            // Apps
            if (apps != null) {
                final int count = apps.size();
                mDataset = new ArrayList<MyApplicationInfo>();

                Set<String> pkgs = mPrefs.getStringSet("pkgs", null);

                for (int i = 0; i < count; i++) {
                    MyApplicationInfo application = new MyApplicationInfo();
                    ResolveInfo info = apps.get(i);

                    application.setTitle(info.loadLabel(manager));
                    application.setPkg(info.activityInfo.applicationInfo.packageName);
                    application.setIcon(info.activityInfo.loadIcon(manager));

                    application.setEnabled(true);

                    if (pkgs != null && pkgs.contains(application.getPkg().toString()))
                        application.setNotify(true);
                    else
                        application.setNotify(false);
                    if (!MyApplicationInfo.pkgAllreadyExists(application.getPkg(),mDataset)) {
                        mDataset.add(application);
                    }
                }
            }
        }catch (Exception e){
            Log.e("ToqAN", "Error loading application list: " + e.getMessage());
        }
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
                reorderDataset();
                return true;
            case R.id.action_update_list:
                reorderDataset();
                mAdapter.notifyDataSetChanged();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCheckBoxClicked(View v){
        Boolean checked = ((CheckBox) v).isChecked();
        CharSequence pkg = (CharSequence)v.getTag();
        setNotify(pkg,checked);
        setAnimation(pkg,R.anim.checkbox_changed);
        savePreferences();
        mAdapter.notifyDataSetChanged();
    }



    private void setNotify(CharSequence pkg, Boolean checked) {
        for (MyApplicationInfo app : mDataset){
            if (app != null && app.getPkg().equals(pkg)){
                app.setNotify(checked);
            }
        }
    }

    private void setAnimation(CharSequence pkg, int animation) {
        for (MyApplicationInfo app : mDataset){
            if (app != null && app.getPkg().equals(pkg)){
                app.setAnimation(animation);
            }
        }
    }

    public void setAppCheckboxes(Boolean checked){
        for (MyApplicationInfo app : mDataset){
            if (app!=null) {
                app.setNotify(checked);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void savePreferences(){
        try {
            SharedPreferences.Editor editor = mPrefs.edit();
            Set<String> pkgs = new HashSet<String>();
            for (MyApplicationInfo app : mDataset) {
                if (app!=null && app.getNotify()) {
                    pkgs.add(app.getPkg().toString());
                }
            }
            editor.remove("pkgs");
            editor.putStringSet("pkgs", pkgs).commit();
        }catch (Exception e){
            Log.e("ToqAN","Error saving preferences: "+e.getMessage());
        }
    }




    public void reorderDataset(){
        mDataset = MyApplicationInfo.reorderApplicationListCompletely(mDataset);

        // Update Adapter
        mAdapter = new appCardAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
}
