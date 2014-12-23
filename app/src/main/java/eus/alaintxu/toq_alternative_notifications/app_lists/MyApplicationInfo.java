package eus.alaintxu.toq_alternative_notifications.app_lists;

/**
 * Created by aperez on 15/12/14.
 */

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;

import eus.alaintxu.toq_alternative_notifications.R;

/**
 * Represents a launchable application. An application is made of a name (or title), an intent
 * and an icon.
 */
class MyApplicationInfo {
    final static int NO_ANIMATION = -1;
    /**
     * The application name.
     */
    private CharSequence title;
    /**
     * The application name.
     */
    private CharSequence pkg;

    /**
     * The intent used to start the application.
     */
    private Intent intent;

    /**
     * The application icon.
     */
    private Drawable icon;

    /**
     * If the app is selected for notifications.
     */
    private Boolean notify;

    /**
     * If the checkbox must be enabled or not.
     */
    private Boolean enabled;

    private int animation = R.anim.app_item_in;

    public MyApplicationInfo() {
        setTitle("No Title");
        setPkg("no.pkg");
        setIcon(null);
        setNotify(false);
        setEnabled(true);
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public CharSequence getPkg() {
        return pkg;
    }

    public void setPkg(CharSequence pkg) {
        this.pkg = pkg;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Boolean getNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int getAnimation() {
        int tmp = animation;
        animation = NO_ANIMATION;
        return tmp;
    }

    public void setAnimation(int animation) {
        this.animation = animation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MyApplicationInfo)) {
            return false;
        }

        MyApplicationInfo that = (MyApplicationInfo) o;
        return title.equals(that.title) &&
                intent.getComponent().getClassName().equals(
                        that.intent.getComponent().getClassName());
    }

    @Override
    public int hashCode() {
        int result;
        result = (title != null ? title.hashCode() : 0);
        final String name = intent.getComponent().getClassName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public static ArrayList<MyApplicationInfo> reorderApplicationListAlphabetically(ArrayList<MyApplicationInfo> inDataset) {
        ArrayList<MyApplicationInfo> alphabeticalDataset = new ArrayList<MyApplicationInfo>();
        for (MyApplicationInfo inApp : inDataset) {
            Boolean found = false;
            for (MyApplicationInfo alphaApp : alphabeticalDataset) {
                if (alphaApp != null && inApp != null) {
                    //Lowercase not to be case sensitive ordering app list.
                    String alphaAppTitle = alphaApp.getTitle().toString().toLowerCase();
                    String inAppTitle = inApp.getTitle().toString().toLowerCase();
                    if (alphaAppTitle.compareTo(inAppTitle) > 0) {
                        int index = alphabeticalDataset.indexOf(alphaApp);
                        alphabeticalDataset.add(index, inApp);
                        found = true;
                        break;
                    }
                }
            }
            if (inApp != null && !found) {
                alphabeticalDataset.add(inApp);
            }
        }
        return alphabeticalDataset;
    }

    public static ArrayList<MyApplicationInfo> reorderApplicationListNotifyFirst(ArrayList<MyApplicationInfo> inDataset) {

        ArrayList<MyApplicationInfo> noApps = new ArrayList<MyApplicationInfo>();
        ArrayList<MyApplicationInfo> yesApps = new ArrayList<MyApplicationInfo>();
        ArrayList<MyApplicationInfo> outDataset = new ArrayList<MyApplicationInfo>();
        for (MyApplicationInfo app : inDataset) {
            if (app != null) {
                if (app.getNotify()) {
                    yesApps.add(app);
                } else {
                    noApps.add(app);
                }
            }
        }
        for (MyApplicationInfo app : yesApps) {
            outDataset.add(app);
        }
        for (MyApplicationInfo app : noApps) {
            outDataset.add(app);
        }
        return outDataset;
    }

    public static ArrayList<MyApplicationInfo> reorderApplicationListCompletely(ArrayList<MyApplicationInfo> inDataset) {
        ArrayList<MyApplicationInfo> alphabetically;
        ArrayList<MyApplicationInfo> ordered;
        alphabetically = reorderApplicationListAlphabetically(inDataset);
        ordered = reorderApplicationListNotifyFirst(alphabetically);
        //ordered = reorderApplicationListNotifyFirst(inDataset);
        return ordered;
    }

    public static Boolean pkgAllreadyExists(CharSequence pkg,ArrayList<MyApplicationInfo> mDataset){
        Boolean exists = false;

        for(MyApplicationInfo app : mDataset){
            if(app!=null && app.getPkg().equals(pkg)){
                exists = true;
                break;
            }
        }

        return exists;
    }
}

