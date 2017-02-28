package com.uportMobile;

/**
 * Created by aldi on 1/30/17.
 */
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.FirebaseInstanceId;
import android.util.Log;

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        System.out.println("Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    public static String returnToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }
}