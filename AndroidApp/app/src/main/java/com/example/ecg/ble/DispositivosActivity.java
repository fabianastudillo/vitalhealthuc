package com.example.ecg.ble;

import android.content.Intent;
import android.os.Bundle;

import com.example.ecg.ble.Conexion.ConexionActivity;
import com.example.ecg.ble.Servicios.ServiciosDescubiertosActivity;
import com.example.ecg.R;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DispositivosActivity extends AppCompatActivity {

    public static final String EXTRA_MAC_ADDRESS = "extra_mac_address";
    private String macAddress;

    @OnClick(R.id.connect)
    public void onConnectClick() {
        final Intent intent = new Intent(this, ConexionActivity.class);
        intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
        startActivity(intent);
    }

    @OnClick(R.id.discovery)
    public void onDiscoveryClick() {
        final Intent intent = new Intent(this, ServiciosDescubiertosActivity.class);
        intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);
        macAddress = getIntent().getStringExtra(EXTRA_MAC_ADDRESS);
        //noinspection ConstantConditions
        getSupportActionBar().setSubtitle(getString(R.string.mac_address, macAddress));
    }
}
