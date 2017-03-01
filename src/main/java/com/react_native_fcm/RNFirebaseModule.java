package com.react_native_fcm;

/**
 * Created by aldi on 2/28/17.
 */
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.firebase.iid.FirebaseInstanceId;

import android.util.Log;

public class RNFirebaseModule extends ReactContextBaseJavaModule{
  private final static String TAG = RNFirebaseModule.class.getCanonicalName();

  public RNFirebaseModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "RNFirebaseModule";
  }

  //Parameters: Context = main application context
  @ReactMethod
  public void getFCMToken(Promise promise) {
    Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());
    promise.resolve(FirebaseInstanceId.getInstance().getToken());
  }
}
