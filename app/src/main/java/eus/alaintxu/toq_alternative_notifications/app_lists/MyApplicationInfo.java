package eus.alaintxu.toq_alternative_notifications.app_lists;

/**
 * Created by aperez on 15/12/14.
 */

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Represents a launchable application. An application is made of a name (or title), an intent
 * and an icon.
 */
class MyApplicationInfo {
    /**
     * The application name.
     */
    CharSequence title;
    /**
     * The application name.
     */
    CharSequence pkg;

    /**
     * The intent used to start the application.
     */
    Intent intent;

    /**
     * The application icon.
     */
    Drawable icon;

    /**
     * If the app is selected for notifications.
     */
    Boolean notify;

    /**
     * If the checkbox must be enabled or not.
     */
    boolean enabled;

    /**
     * When set to true, indicates that the icon has been resized.
     */
    boolean filtered;

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
}
