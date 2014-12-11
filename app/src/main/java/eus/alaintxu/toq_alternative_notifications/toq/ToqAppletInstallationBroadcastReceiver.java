package eus.alaintxu.toq_alternative_notifications.toq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.Constants;

import eus.alaintxu.toq_alternative_notifications.ToqAlternativeNotifications;


/**
 * Broadcast receiver for Toq app install intent.
 */
public class ToqAppletInstallationBroadcastReceiver extends BroadcastReceiver{

    
    /**
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    public void onReceive(Context context, Intent intent){

        Log.d(Constants.TAG, "ToqAppletInstallationBroadcastReceiver.onReceive - context: " + context + ", intent: " + intent);

        // Launch ToqAN activity to complete the install of the deck of cards applet
        Intent launchIntent= new Intent(context, ToqAlternativeNotifications.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 

        context.startActivity(launchIntent);
    }

}
