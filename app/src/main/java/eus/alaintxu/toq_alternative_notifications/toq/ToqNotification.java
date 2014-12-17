package eus.alaintxu.toq_alternative_notifications.toq;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.MenuOption;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.NotificationTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * Created by aperez on 11/12/14.
 */
public class ToqNotification {
    private int id;
    private String pkg        = "";
    private String appName    = "";
    private String tag        = "";
    private String key        = "";
    private String tickerText = "";
    private String category   = "";
    private int icon          = 0;
    private String title      = "";
    private String text       = "";
    private long when         = 0;

    public ToqNotification(){}

    public ToqNotification(StatusBarNotification sbn,Map<CharSequence,CharSequence> appNames){
        try {
            Notification n = sbn.getNotification();

            setId(sbn.getId());
            setPkg(sbn.getPackageName());

            CharSequence appName = appNames.get(pkg);
            if (appName!=null)
                setAppName(appNames.get(getPkg()).toString());
            else
                setAppName("");
            setTag(sbn.getTag());
            setKey(sbn.getKey());
            if(n!=null && n.tickerText!=null)
                setTickerText(n.tickerText.toString());
            setCategory(n.category);
            setIcon(n.icon);


            Bundle b = n.extras;
            if(b!=null) {
                setTitle((String) b.get("android.title"));
                Object textObject = b.get("android.bigText");
                if(textObject==null)
                    textObject = b.get("android.text");
                if(textObject!=null) {
                    if(textObject instanceof SpannableString){
                        setText(textObject.toString());
                    }else if(textObject instanceof String){
                        setText((String) textObject);
                    }
                }
                setWhen(n.when);
            }

        }catch (Exception e){
            Log.e("ToqAN","Error creating ToqNotification",e);
        }
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getAppName() {
        if (appName.equals(""))
            return "System";
        else
            return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTickerText() {
        return tickerText;
    }

    public void setTickerText(String tickerText) {
        this.tickerText = tickerText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getWhen() {
        return when;
    }

    public void setWhen(long when) {
        this.when = when;
    }

    private String getDateString(){
        // Date
        String dateString = "\n";
        if(getWhen()>0) {
            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(getWhen());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
            dateString = sdf.format(c.getTime());
        }
        return dateString;
    }

    public NotificationTextCard getNotificationTextCard(){
        return new NotificationTextCard(
                getWhen(),
                getTitle(),
                getNotificationText()
        );
    }

    public SimpleTextCard getNotificationSimpleTextCard(int id){
        MenuOption[] options = getMenuOptions();
        // Create SimpleTextCard
        SimpleTextCard simpleTextCard= new SimpleTextCard(""+id);
        simpleTextCard.setHeaderText(getAppName());
        simpleTextCard.setTitleText(getTitle());
        simpleTextCard.setMessageText(getMessageText());
        simpleTextCard.setInfoText(getKey());
        if (options==null || options.length<=0) {
            simpleTextCard.setReceivingEvents(false);
        }else{
            simpleTextCard.setReceivingEvents(true);
            simpleTextCard.setMenuOptionObjs(options);
        }
        simpleTextCard.setShowDivider(false);

        return simpleTextCard;
    }
    /* Package depending functions */

    public MenuOption[] getMenuOptions(){
        MenuOption[] options;
        /*if (getPkg().equals("com.google.android.talk")){
            // Hangout message
            options = getHangoutMenuOptions();
        }
        else */
        if (appName.equals("")){
            // Usually system apps, do not add options menu.
            options = null;
        }else{
            options = new MenuOption[]{
                    new MenuOption("Dismiss", false)
            };
        }
        return options;
    }

    private String[] getNotificationText(){
        return getGenericNotificationText();
    }

    private String[] getMessageText() {
        String[] messageText;
        if (pkg.equals("com.google.android.talk")){
            // Different messages for different packages.
            messageText = getHangoutMessageText();
        }else{
            messageText = getGenericMessageText();
        }
        return messageText;
    }
    /* Generic functions */

    public String[] getGenericNotificationText() {
        String text = "";
        text       += getText()+"\n \n";
        text       += getAppName();

        return ToqInterface.splitString(text);
    }

    private String[] getGenericMessageText(){
        String messageText;
        messageText = getDateString() + '\n';
        messageText += getTickerText() + '\n';
        messageText += getText();
        return ToqInterface.splitString(messageText);
    }

    /* Hangout functions */
    /*private MenuOption[] getHangoutMenuOptions(){
        MenuOption[] options = new MenuOption[]{
            new MenuOption("Dismiss", false),
            new MenuOption("Respond", true)
        };

        return options;
    }*/

    private String[] getHangoutMessageText(){
        String messageText;
        messageText = getDateString() + '\n';
        // No tickerText
        messageText += getText();
        return ToqInterface.splitString(messageText);
    }
}
