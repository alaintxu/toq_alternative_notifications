package eus.alaintxu.toq_alternative_notifications.toq;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.Constants;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.Card;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManager;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCards;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCardsException;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteResourceStore;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteToqNotification;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.DeckOfCardsLauncherIcon;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.util.ParcelableUtil;

import java.io.InputStream;
import java.util.Map;

import eus.alaintxu.toq_alternative_notifications.NotificationListener;
import eus.alaintxu.toq_alternative_notifications.R;

/**
 * Created by aperez on 9/12/14.
 * This Class interacts with Toq Smartwatch
 */
public class ToqInterface {
    // Singleton instance
    private static ToqInterface instance = null; // Singleton instance

    // Android classes
    Activity activity = null;

    // Toq classes
    private DeckOfCardsManager deckOfCardsManager;
    private DeckOfCardsManagerListener deckOfCardsManagerListener;
    private DeckOfCardsEventListener deckOfCardsEventListener;
    private ToqAppStateBroadcastReceiver toqAppStateReceiver;
    private RemoteResourceStore resourceStore;
    private RemoteDeckOfCards deckOfCards;

    private final static String DEMO_PREFS_FILE= "toqan_prefs_file";
    private final static String DECK_OF_CARDS_KEY= "toqan_deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY= "toqan_deck_of_cards_version_key";

    //UI
    private TextView statusTextView;
    protected ToqInterface(){
        // Singleton class, use static getInstance instead
        // this method exists only to defeat instantiation.
    }

    public static ToqInterface getInstance(){
        if(instance == null){
            instance = new ToqInterface();
        }

        return instance;
    }

    private void setActivity(Activity activity){
        this.activity = activity;
    }

    public void initToqInterface(Activity activity){
        setActivity(activity);
        // Get the reference to the deck of cards manager
        deckOfCardsManager= DeckOfCardsManager.getInstance(activity.getApplicationContext());

        // Create listeners
        deckOfCardsManagerListener= new DeckOfCardsManagerListenerImpl();
        deckOfCardsEventListener= new DeckOfCardsEventListenerImpl();

        // Create the state receiver
        toqAppStateReceiver= new ToqAppStateBroadcastReceiver();

        // Init
        init();
        initUI();
    }


    public void updateDeckOfCardsWithNotifications(StatusBarNotification[] sbns,Map<CharSequence,CharSequence> appNames) {
        ListCard listCard = deckOfCards.getListCard();
        deleteListCard(listCard);

        if(sbns != null) {
            int nofnotifications = sbns.length;

            for (int i = 0; i < nofnotifications; i++) {
                ToqNotification toqNotification = new ToqNotification(sbns[i],appNames);
                try {
                    if (toqNotification.getTitle() != "")
                        listCard.add(toqNotification.getNotificationSimpleTextCard(i));
                } catch (Exception e) {
                    Log.e("ToqAN", "Error on updateDeckOfCardsWithNotifications: ", e);
                }
            }
        }

        genericUpdateDeckOfCards();


    }

    private void deleteListCard(ListCard listCard){
        int nofcards         = listCard.size();
        for(int i=0;i<nofcards;i++) {
            listCard.remove(0);
        }
    }

    public void notifyToq(ToqNotification toqNotification){
        Log.d("ToqAN", "ToqInterface.notifyToq");
        RemoteToqNotification notification= new RemoteToqNotification(activity, toqNotification.getNotificationTextCard());

        try{
            deckOfCardsManager.sendNotification(notification);
        }
        catch (RemoteDeckOfCardsException e){
            //Toast.makeText(activity, activity.getString(R.string.error_sending_notification), Toast.LENGTH_SHORT).show();
            Log.e("ToqAN", "ToqAN.sendNotification - error sending notification", e);
        }
    }


    // Old functions

    public void start(){
        // Add the listeners
        deckOfCardsManager.addDeckOfCardsManagerListener(deckOfCardsManagerListener);
        deckOfCardsManager.addDeckOfCardsEventListener(deckOfCardsEventListener);

        // Register toq app state receiver
        registerToqAppStateReceiver();

        // If not connected, try to connect
        if (!deckOfCardsManager.isConnected()){

            setStatus(activity.getString(R.string.status_connecting));

            Log.d("ToqAN", "ToqAN.onStart - not connected, connecting...");

            try{
                deckOfCardsManager.connect();
            }
            catch (RemoteDeckOfCardsException e){
                Log.d("ToqAN", "ToqAN.onStart - error connecting to Toq app service", e);
            }

        }
        else{
            Log.d("ToqAN", "ToqAN.onStart - already connected");
            setStatus(activity.getString(R.string.status_connected));
        }
    }

    public void destroy(){

        // Unregister toq app state receiver
        unregisterStateReceiver();

        // Remove listeners
        deckOfCardsManager.removeDeckOfCardsManagerListener(deckOfCardsManagerListener);
        deckOfCardsManager.removeDeckOfCardsEventListener(deckOfCardsEventListener);

        deckOfCardsManager.disconnect();
    }

    /*
     * Private classes
     */


    // Handle service connection lifecycle and installation events
    private class DeckOfCardsManagerListenerImpl implements DeckOfCardsManagerListener{

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onConnected()
         */
        public void onConnected(){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    setStatus(activity.getString(R.string.status_connected));
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onDisconnected()
         */
        public void onDisconnected(){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    setStatus(activity.getString(R.string.status_disconnected));
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onInstallationSuccessful()
         */
        public void onInstallationSuccessful(){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    setStatus(activity.getString(R.string.status_installation_successful));
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onInstallationDenied()
         */
        public void onInstallationDenied(){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    setStatus(activity.getString(R.string.status_installation_denied));
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onUninstalled()
         */
        public void onUninstalled(){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    setStatus(activity.getString(R.string.status_uninstalled));
                }
            });
        }

    }


    // Handle card events triggered by the user interacting with a card in the installed deck of cards
    private class DeckOfCardsEventListenerImpl implements DeckOfCardsEventListener{

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardOpen(java.lang.String)
         */
        public void onCardOpen(final String cardId){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(activity, activity.getString(R.string.event_card_open) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardVisible(java.lang.String)
         */
        public void onCardVisible(final String cardId){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(activity, activity.getString(R.string.event_card_visible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardInvisible(java.lang.String)
         */
        public void onCardInvisible(final String cardId){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(activity, activity.getString(R.string.event_card_invisible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardClosed(java.lang.String)
         */
        public void onCardClosed(final String cardId){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(activity, activity.getString(R.string.event_card_closed) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption){
            try {
                ListCard listCard = deckOfCards.getListCard();
                SimpleTextCard card = (SimpleTextCard) listCard.get(cardId);
                String infoText = card.getInfoText();

                if (menuOption.equals("Dismiss")) {
                    NotificationListener.getInstance().cancelNotification(infoText);
                }
            }catch (Exception e) {
                Log.e("ToqAN",e.getMessage());
            }
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption, final String quickReplyOption){
            activity.runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(activity, activity.getString(R.string.event_menu_option_selected) + cardId + " [" + menuOption + ":" + quickReplyOption +
                            "]", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    // Toq app state receiver
    private class ToqAppStateBroadcastReceiver extends BroadcastReceiver {

        /**
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        public void onReceive(Context context, Intent intent){

            String action= intent.getAction();

            if (action == null){
                Log.w("ToqAN", "ToqAN.ToqAppStateBroadcastReceiver.onReceive - action is null, returning");
                return;
            }

            Log.d("ToqAN", "ToqAN.ToqAppStateBroadcastReceiver.onReceive - action: " + action);

            // If watch is now connected, refresh UI
            if (action.equals(Constants.TOQ_WATCH_CONNECTED_INTENT)){
                Toast.makeText(activity, activity.getString(R.string.intent_toq_watch_connected), Toast.LENGTH_SHORT).show();
            }
            // Else if watch is now disconnected, disable UI
            else if (action.equals(Constants.TOQ_WATCH_DISCONNECTED_INTENT)){
                Toast.makeText(activity, activity.getString(R.string.intent_toq_watch_disconnected), Toast.LENGTH_SHORT).show();
            }

        }

    }


    /*
     * Private API
     */

    // Register state receiver
    private void registerToqAppStateReceiver(){
        IntentFilter intentFilter= new IntentFilter();
        intentFilter.addAction(Constants.BLUETOOTH_ENABLED_INTENT);
        intentFilter.addAction(Constants.BLUETOOTH_DISABLED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_PAIRED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_UNPAIRED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_CONNECTED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_DISCONNECTED_INTENT);
        activity.getApplicationContext().registerReceiver(toqAppStateReceiver, intentFilter);
    }


    // Unregister state receiver
    private void unregisterStateReceiver(){
        activity.getApplicationContext().unregisterReceiver(toqAppStateReceiver);
    }


    // Set status bar message
    private void setStatus(String msg){
        statusTextView.setText(msg);
    }


    // Initialise
    private void init(){

        // Create the resourse store for icons and images
        resourceStore= new RemoteResourceStore();

        DeckOfCardsLauncherIcon whiteIcon;
        DeckOfCardsLauncherIcon colorIcon;

        // Get the launcher icons
        try{

            whiteIcon= new DeckOfCardsLauncherIcon("white.launcher.icon", getBitmap("white.png"), DeckOfCardsLauncherIcon.WHITE);
            colorIcon= new DeckOfCardsLauncherIcon("color.launcher.icon", getBitmap("color.png"), DeckOfCardsLauncherIcon.COLOR);

        }
        catch (Exception e){
            Toast.makeText(activity, activity.getString(R.string.error_initialising_icons), Toast.LENGTH_SHORT).show();
            Log.e("ToqAN", "ToqAN.init - error occurred parsing the icons", e);
            return;
        }

        // Try to retrieve a stored deck of cards
        try{

            // If there is no stored deck of cards or it is unusable, then create new and store
            if ((deckOfCards= getStoredDeckOfCards()) == null){
                deckOfCards= createDeckOfCards();
                storeDeckOfCards();
            }

        }
        catch (Throwable th){
            Log.w("ToqAN", "ToqAN.init - error occurred retrieving the stored deck of cards: " + th.getMessage());
            deckOfCards= null; // Reset to force recreate
        }

        // Make sure in usable state
        if (deckOfCards == null){
            deckOfCards= createDeckOfCards();
        }

        // Set the custom launcher icons, adding them to the resource store
        deckOfCards.setLauncherIcons(resourceStore, new DeckOfCardsLauncherIcon[]{whiteIcon, colorIcon});
    }


    // Get stored deck of cards if one exists
    private RemoteDeckOfCards getStoredDeckOfCards() throws Exception{

        if (!isValidDeckOfCards()){
            Log.w("ToqAN", "ToqAN.getStoredDeckOfCards - stored deck of cards not valid for this version of the demo, recreating...");
            return null;
        }

        SharedPreferences prefs= activity.getSharedPreferences(DEMO_PREFS_FILE, Context.MODE_PRIVATE);
        String deckOfCardsStr= prefs.getString(DECK_OF_CARDS_KEY, null);

        if (deckOfCardsStr == null){
            return null;
        }
        else{
            return ParcelableUtil.unmarshall(deckOfCardsStr, RemoteDeckOfCards.CREATOR);
        }

    }

    // Store deck of cards
    private void storeDeckOfCards() throws Exception{
        SharedPreferences prefs= activity.getSharedPreferences(DEMO_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= prefs.edit();
        editor.putString(DECK_OF_CARDS_KEY, ParcelableUtil.marshall(deckOfCards));
        editor.putInt(DECK_OF_CARDS_VERSION_KEY, Constants.VERSION_CODE);
        editor.commit();
    }


    // Check if the stored deck of cards is valid for this version of the demo
    private boolean isValidDeckOfCards(){

        SharedPreferences prefs= activity.getSharedPreferences(DEMO_PREFS_FILE, Context.MODE_PRIVATE);
        int deckOfCardsVersion= prefs.getInt(DECK_OF_CARDS_VERSION_KEY, 0);

        if (deckOfCardsVersion < Constants.VERSION_CODE){
            return false;
        }

        return true;
    }


    // Create some cards with example content
    private RemoteDeckOfCards createDeckOfCards(){

        ListCard listCard= new ListCard();

        return new RemoteDeckOfCards(activity, listCard);
    }


    // Initialise the UI
    private void initUI(){
        // Status
        statusTextView= (TextView)activity.findViewById(R.id.status_text);
        statusTextView.setText("Initialised");
    }


    // Install deck of cards applet
    public void installDeckOfCards(){

        Log.d("ToqAN", "ToqAN.installDeckOfCards");

        updateDeckOfCardsWithNotifications(null,null);

        try{
            deckOfCardsManager.installDeckOfCards(deckOfCards, resourceStore);
        }
        catch (RemoteDeckOfCardsException e){
            Toast.makeText(activity, activity.getString(R.string.error_installing_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e("ToqAN", "ToqAN.installDeckOfCards - error installing deck of cards applet", e);
        }

        try{
            storeDeckOfCards();
        }
        catch (Exception e){
            Log.e("ToqAN", "ToqAN.installDeckOfCards - error storing deck of cards applet", e);
        }

    }

    // Update deck of cards applet
    private void genericUpdateDeckOfCards(){

        Log.d("ToqAN", "ToqAN.genericUpdateDeckOfCards");

        try{
            deckOfCardsManager.updateDeckOfCards(deckOfCards, resourceStore);
        }
        catch (RemoteDeckOfCardsException e){
            //Toast.makeText(activity, activity.getString(R.string.error_updating_deck_of_cards), Toast.LENGTH_SHORT).show();
            //Log.e("ToqAN", "ToqAN.genericUpdateDeckOfCards - error updating deck of cards applet", e);
        }

        try{
            storeDeckOfCards();
        }
        catch (Exception e){
            Log.e("ToqAN", "ToqAN.genericUpdateDeckOfCards - error storing deck of cards applet", e);
        }

    }



    // Uninstall deck of cards applet
    public void uninstallDeckOfCards(){

        Log.d("ToqAN", "ToqAN.uninstallDeckOfCards");

        try{
            deckOfCardsManager.uninstallDeckOfCards();
        }
        catch (RemoteDeckOfCardsException e){
            Toast.makeText(activity, activity.getString(R.string.error_uninstalling_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e("ToqAN", "ToqAN.uninstallDeckOfCards - error uninstalling deck of cards applet applet", e);
        }

    }


    // Read an image from assets and return as a bitmap
    private Bitmap getBitmap(String fileName) throws Exception{

        try{
            InputStream is= activity.getAssets().open(fileName);
            return BitmapFactory.decodeStream(is);
        }
        catch (Exception e){
            throw new Exception("An error occurred getting the bitmap: " + fileName, e);
        }

    }


    /*
    Concatenate string from Toq interface
     */
    public static String concatStrings(String[] textStrs){

        StringBuilder buffy= new StringBuilder();

        for (int i= 0; i < textStrs.length; i++){

            buffy.append(textStrs[i]);

            if (i < (textStrs.length - 1)){
                buffy.append("\n");
            }
        }

        return buffy.toString();
    }

    /*
    Split string to Toq interface
     */
    public static String[] splitString(String textStr){
        return textStr.split("\n");
    }
}
