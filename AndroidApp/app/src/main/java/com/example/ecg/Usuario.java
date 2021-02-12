package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ecg.ble_login.Escaneo.Escaneoctivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Usuario extends AppCompatActivity {

    TextView emailTextView;
    MaterialButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setSelectedItemId(R.id.usuario);

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
                        return true;
                }
                return false;
            }
        });

        emailTextView = findViewById(R.id.emailTextView);
        logoutButton = findViewById(R.id.logoutbutton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailTextView.setText(user.getEmail());
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Usuario.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override public void onBackPressed() { return; }
}