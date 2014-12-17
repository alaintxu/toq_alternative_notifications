package eus.alaintxu.toq_alternative_notifications.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import eus.alaintxu.toq_alternative_notifications.R;

/**
 * Created by aperez on 15/12/14.
 */
public class AppListItemAdapter extends ArrayAdapter<MyApplicationInfo> {
    private ArrayList<MyApplicationInfo> apps;

    private Context context;
    private SharedPreferences mPrefs;

    public AppListItemAdapter(Context context, int textViewResourceId, ArrayList<MyApplicationInfo> apps){
        super(context,textViewResourceId,apps);
        this.apps = apps;
        this.context = context;
        mPrefs = context.getSharedPreferences("ToqAN", 0);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent){
        if(v == null){
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.applistitem,null);
        }

        MyApplicationInfo app = apps.get(position);
        if(app != null){
            CheckBox appCB = (CheckBox) v.findViewById(R.id.appCB);

            TextView appTitle = (TextView) v.findViewById(R.id.appTitleTV);
            TextView appPkg = (TextView) v.findViewById(R.id.appPackageTv);
            ImageView appIcon = (ImageView) v.findViewById(R.id.appIconIV);

             if (appCB != null){
                 appCB.setTag(position);
                 appCB.setChecked(app.notify);
             }
            if (appTitle != null){
                appTitle.setText(app.title);
            }

            if (appPkg != null){
                appPkg.setText(app.pkg);
            }

            if (appIcon != null){
                appIcon.setImageDrawable(app.icon);
            }
        }

        return v;
    }
}