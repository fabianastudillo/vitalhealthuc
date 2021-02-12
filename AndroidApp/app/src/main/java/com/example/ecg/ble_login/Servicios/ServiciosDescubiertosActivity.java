package com.example.ecg.ble_login.Servicios;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.ecg.ConexionBLE;
import com.example.ecg.R;
import com.example.ecg.ble_login.DispositivosActivity;
import com.example.ecg.ble_login.Grafica.CaracteristicasOperacionActivity;
import com.google.android.material.snackbar.Snackbar;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class ServiciosDescubiertosActivity extends AppCompatActivity {

    @BindView(R.id.connect)
    Button connectButton;
    @BindView(R.id.scan_results)
    RecyclerView recyclerView;
    private ResultadosDescubiertosAdapter adapter;
    private RxBleDevice bleDevice;
    private String macAddress;
    private final CompositeDisposable servicesDisposable = new CompositeDisposable();

    @OnClick(R.id.connect)
    public void onConnectToggleClick() {
        final Disposable disposable = bleDevice.establishConnection(false)
                .flatMapSingle(RxBleConnection::discoverServices)
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::updateUI)
                .subscribe(adapter::swapScanResult, this::onConnectionFailure);
        servicesDisposable.add(disposable);

        updateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);
        ButterKnife.bind(this);
        macAddress = getIntent().getStringExtra(DispositivosActivity.EXTRA_MAC_ADDRESS);
        //noinspection ConstantConditions
        getSupportActionBar().setSubtitle(getString(R.string.mac_address, macAddress));
        bleDevice = ConexionBLE.getRxBleClient(this).getBleDevice(macAddress);
        configureResultList();
    }

    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        adapter = new ResultadosDescubiertosAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final ResultadosDescubiertosAdapter.AdapterItem itemAtPosition = adapter.getItem(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
    }

    private void onAdapterItemClick(ResultadosDescubiertosAdapter.AdapterItem item) {

        if (item.type == ResultadosDescubiertosAdapter.AdapterItem.CHARACTERISTIC) {
            final Intent intent = CaracteristicasOperacionActivity.startActivityIntent(this, macAddress, item.uuid);
            // If you want to check the alternative com.example.sampleapplication.Grafica.com.example.sampleapplication.example4_characteristic1.advanced.com.example.sampleapplication.example4_characteristic1.advanced implementation comment out the line above and uncomment one below
//            final Intent intent = CaracteristicasAvanzadasOperacionActivity.startActivityIntent(this, macAddress, item.uuid);
            startActivity(intent);
        } else {
            //noinspection ConstantConditions
            Snackbar.make(findViewById(android.R.id.content), R.string.not_clickable, Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(android.R.id.content), "Error de conexión, tal vez no seleccionaste nuestro dispositivo ", Snackbar.LENGTH_SHORT).show();
    }

    private void updateUI() {
        connectButton.setEnabled(!isConnected());
    }

    @Override
    protected void onPause() {
        super.onPause();
        servicesDisposable.clear();
    }
}
