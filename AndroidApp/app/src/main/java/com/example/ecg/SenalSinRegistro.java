package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class SenalSinRegistro extends AppCompatActivity {

    MaterialButton registrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senal_sin_registro);

        registrate = findViewById(R.id.registrate);

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setSelectedItemId(R.id.senal);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.instrucciones:
                        startActivity(new Intent(getApplicationContext(),InstruccionesSinRegistro.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.senal:
                        return true;
                }
                return false;
            }
        });

        registrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SenalSinRegistro.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override public void onBackPressed() { return; }
}