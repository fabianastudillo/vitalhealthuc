package com.example.ecg;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleException;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.exceptions.UndeliverableException;

public class ConexionBLE extends Application {

    private RxBleClient rxBleClient;

    public static RxBleClient getRxBleClient(Context context){
        ConexionBLE application = (ConexionBLE) context.getApplicationContext();
        return application.rxBleClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rxBleClient = RxBleClient.create(this);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException && throwable.getCause() instanceof BleException){
                Log.v("ConexionBLE", "Suppressed UndeliverableException" + throwable.toString());
                return;
            }
            throw new RuntimeException("Unexplicable Throwable in RxJavaPlugins error handler", throwable);
        });
    }
}
