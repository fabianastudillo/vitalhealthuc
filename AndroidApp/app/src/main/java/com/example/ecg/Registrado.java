package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.ecg.ble_login.Escaneo.Escaneoctivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Registrado extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrado);

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.instrucciones:
                        startActivity(new Intent(getApplicationContext(),Instrucciones.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.senal:
                        startActivity(new Intent(getApplicationContext(), Escaneoctivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.archivo:
                        startActivity(new Intent(getApplicationContext(),Archivo.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.usuario:
                        startActivity(new Intent(getApplicationContext(),Usuario.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

    };

    @Override public void onBackPressed() { return; }
}