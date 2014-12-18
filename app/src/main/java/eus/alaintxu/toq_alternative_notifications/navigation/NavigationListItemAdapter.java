/*package eus.alaintxu.toq_alternative_notifications.navigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import eus.alaintxu.toq_alternative_notifications.R;

public class NavigationListItemAdapter extends ArrayAdapter<NavigationItem> {

    private ArrayList<NavigationItem> navItems;

    private Context context;

    public NavigationListItemAdapter(Context context, int textViewResourceId, ArrayList<NavigationItem> navItems) {
        super(context,textViewResourceId,getNavigationItems());
        navItems = ;
        this.navItems = navItems;
        this.context = context;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.navigation_list_item, null);
        }

        NavigationItem navItem = navItems.get(position);
        if (navItem != null) {
            ImageView iv = (ImageView) v.findViewById(R.id.nli_image);
            TextView tv = (TextView) v.findViewById(R.id.nli_text);

            if (iv != null) {
                iv.setImageDrawable(navItem.getDrawable());
            }
            if (tv != null) {
                tv.setText(navItem.getTitle());
            }
        }

        return v;
    }

    public static ArrayList<NavigationItem> getNavigationItems(Context context){
        ArrayList<NavigationItem> items = new ArrayList<NavigationItem>();
        items.add(new NavigationItem(
                context.getDrawable(R.drawable.toq_alternative_notifications),
                context.getString(R.string.section_main)
        ));
        items.add(new NavigationItem(
                context.getDrawable(android.R.drawable.ic_menu_agenda),
                context.getString(R.string.section_notifications)
        ));
        items.add(new NavigationItem(
                context.getDrawable(android.R.drawable.ic_menu_agenda),
                context.getString(R.string.section_applet)
        ));
        items.add(new NavigationItem(
                context.getDrawable(android.R.drawable.ic_menu_close_clear_cancel),
                context.getString(R.string.action_stop)
        ));

        return items;
    }

    public class NavigationItem {
        private Drawable drawable;
        private String title;

        public NavigationItem(){}
        public NavigationItem(Drawable drawable, String title){
            this.drawable = drawable;
            this.title    = title;
        }

        public Drawable getDrawable() {
            return drawable;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}*/
