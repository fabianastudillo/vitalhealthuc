package com.example.ecg.ble_login.Escaneo;

import android.content.Intent;
import android.os.Bundle;

import com.example.ecg.ConexionBLE;
import com.example.ecg.R;
import com.example.ecg.ble_login.Servicios.ServiciosDescubiertosActivity;
import com.example.ecg.ble_login.Utilidades.LocationPermission;
import com.example.ecg.ble_login.Utilidades.ScanExceptionHandler;
import com.google.android.material.button.MaterialButton;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class Escaneoctivity extends AppCompatActivity {

    public static final String EXTRA_MAC_ADDRESS = "extra_mac_address";
    @BindView(R.id.scan_toggle_btn)
    MaterialButton scanToggleButton;
    @BindView(R.id.scan_results)
    RecyclerView recyclerView;
    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private ResultadoEscaneoAdapter resultsAdapter;
    private boolean hasClickedScan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escaneo);
        ButterKnife.bind(this);
        rxBleClient = ConexionBLE.getRxBleClient(this);
        configureResultList();
    }

    @OnClick(R.id.scan_toggle_btn)
    public void onScanToggleClick() {

        if (isScanning()) {
            scanDisposable.dispose();
        } else {
            if (rxBleClient.isScanRuntimePermissionGranted()) {
                scanBleDevices();
            } else {
                hasClickedScan = true;
                LocationPermission.requestLocationPermission(this, rxBleClient);
            }
        }

        updateButtonUIState();
    }

    private void scanBleDevices() {
        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
//                            .setDeviceAddress("B4:99:4C:34:DC:8B")
                        // add custom filters if needed
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(resultsAdapter::addScanResult, this::onScanFailure);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults, rxBleClient)
                && hasClickedScan) {
            hasClickedScan = false;
            scanBleDevices();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isScanning()) {
            /*
             * Stop scanning in onPause callback.
             */
            scanDisposable.dispose();
        }
    }

    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        resultsAdapter = new ResultadoEscaneoAdapter();
        recyclerView.setAdapter(resultsAdapter);
        resultsAdapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final ScanResult itemAtPosition = resultsAdapter.getItemAtPosition(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void onAdapterItemClick(ScanResult scanResults) {
        final String macAddress = scanResults.getBleDevice().getMacAddress();
        final Intent intent = new Intent(this, ServiciosDescubiertosActivity.class);
        intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
        startActivity(intent);
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            ScanExceptionHandler.handleException(this, (BleScanException) throwable);
        }
    }

    private void dispose() {
        scanDisposable = null;
        resultsAdapter.clearScanResults();
        updateButtonUIState();
    }

    private void updateButtonUIState() {
        scanToggleButton.setText(isScanning() ? R.string.stop_scan : R.string.start_scan);
    }
}
