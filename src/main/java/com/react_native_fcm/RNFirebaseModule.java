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
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RNFirebaseModule extends ReactContextBaseJavaModule implements ActivityEventListener {
  private final static String TAG = RNFirebaseModule.class.getCanonicalName();

  public RNFirebaseModule(ReactApplicationContext reactContext) {
    super(reactContext);
    getReactApplicationContext().addActivityEventListener(this);
  }

  @Override
  public String getName() {
    return "RNFirebaseModule";
  }

  @ReactMethod
  public void getInitialNotification(Promise promise){
    Activity activity = getCurrentActivity();
    if(activity == null){
      promise.resolve(null);
      return;
    }
    promise.resolve(parseIntent(getCurrentActivity().getIntent()));
  }

  //Parameters: Context = main application context
  @ReactMethod
  public void getFCMToken(Promise promise) {
    Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());
    promise.resolve(FirebaseInstanceId.getInstance().getToken());
  }

  private void sendEvent(String eventName, Object params) {
    getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
  }

  private void registerTokenRefreshHandler() {
    IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.FCMRefreshToken");
    getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (getReactApplicationContext().hasActiveCatalystInstance()) {
          String token = intent.getStringExtra("token");
          sendEvent("FCMTokenRefreshed", token);
        }
      }
    }, intentFilter);
  }

  @ReactMethod
  public void send(String senderId, ReadableMap payload) throws Exception {
    FirebaseMessaging fm = FirebaseMessaging.getInstance();
    RemoteMessage.Builder message = new RemoteMessage.Builder(senderId + "@gcm.googleapis.com")
            .setMessageId(UUID.randomUUID().toString());

    ReadableMapKeySetIterator iterator = payload.keySetIterator();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      String value = getStringFromReadableMap(payload, key);
      message.addData(key, value);
    }
    fm.send(message.build());
  }

  private String getStringFromReadableMap(ReadableMap map, String key) throws Exception {
    switch (map.getType(key)) {
      case String:
        return map.getString(key);
      case Number:
        try {
          return String.valueOf(map.getInt(key));
        } catch (Exception e) {
          return String.valueOf(map.getDouble(key));
        }
      case Boolean:
        return String.valueOf(map.getBoolean(key));
      default:
        throw new Exception("Unknown data type: " + map.getType(key).name() + " for message key " + key );
    }
  }

  private void registerMessageHandler() {
    IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.ReceiveNotification");

    getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (getReactApplicationContext().hasActiveCatalystInstance()) {
          RemoteMessage message = intent.getParcelableExtra("data");
          WritableMap params = Arguments.createMap();
          WritableMap fcmData = Arguments.createMap();

          if (message.getNotification() != null) {
            Notification notification = message.getNotification();
            fcmData.putString("title", notification.getTitle());
            fcmData.putString("body", notification.getBody());
            fcmData.putString("color", notification.getColor());
            fcmData.putString("icon", notification.getIcon());
            fcmData.putString("tag", notification.getTag());
            fcmData.putString("action", notification.getClickAction());
          }
          params.putMap("fcm", fcmData);

          if(message.getData() != null){
            Map<String, String> data = message.getData();
            Set<String> keysIterator = data.keySet();
            for(String key: keysIterator){
              params.putString(key, data.get(key));
            }
          }
          sendEvent("FCMNotificationReceived", params);

        }
      }
    }, intentFilter);
  }

  private WritableMap parseIntent(Intent intent){
    WritableMap params;
    Bundle extras = intent.getExtras();
    if (extras != null) {
      try {
        params = Arguments.fromBundle(extras);
      } catch (Exception e){
        Log.e(TAG, e.getMessage());
        params = Arguments.createMap();
      }
    } else {
      params = Arguments.createMap();
    }
    WritableMap fcm = Arguments.createMap();
    fcm.putString("action", intent.getAction());
    params.putMap("fcm", fcm);

    params.putInt("opened_from_tray", 1);
    return params;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
  }

  @Override
  public void onNewIntent(Intent intent){
    sendEvent("FCMNotificationReceived", parseIntent(intent));
  }
}
