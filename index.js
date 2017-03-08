'use strict';
/**
 * This exposes the native ToastAndroid module as a JS module. This has a
 * function 'show' which takes the following parameters:
 *
 * 1. String message: A string with the text to toast
 * 2. int duration: The duration of the toast. May be ToastAndroid.SHORT or
 *    ToastAndroid.LONG
 */
import { NativeModules, DeviceEventEmitter, Platform } from 'react-native';


export const FCMEvent = {
  RefreshToken: 'FCMTokenRefreshed',
  Notification: 'FCMNotificationReceived'
}

const RNFirebaseModule = NativeModules.RNFirebaseModule;

const FCM = {};

FCM.getInitialNotification = () => {
    return RNFirebaseModule.getInitialNotification();
}

FCM.getFCMToken = () => {
    return RNFirebaseModule.getFCMToken();
};

FCM.on = (event, callback) => {
    if (!Object.values(FCMEvent).includes(event)) {
        throw new Error(`Invalid FCM event subscription, use import {FCMEvent} from 'react-native-fcm' to avoid typo`);
    };

    return DeviceEventEmitter.addListener(event, callback);
};


FCM.send = (senderId, payload) => {
    RNFirebaseModule.send(senderId, payload);
};

export default FCM;
//module.exports = NativeModules.RNFirebaseModule;
