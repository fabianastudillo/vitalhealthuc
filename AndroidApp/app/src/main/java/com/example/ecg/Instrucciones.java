package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.ecg.ble_login.Escaneo.Escaneoctivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shuhart.stepview.StepView;

public class Instrucciones extends AppCompatActivity {

    StepView stepView;
    TextView stepTextView, descriptionTextView;

    int stepIndex = 0;
    String[] stepsTexts = {"Paso 1", "Paso 2", "Paso 3", "Paso 4", "Paso 5"};
    String[] descriptionTexts = {"Dirígete a la sección de señal, es la que tiene un corazón!... No olvides colocar los electrodos de la manera correcta, muñecas y pierna o la opción de pecho y abdomen",
            "Activa tu bluetooth, y selecciona el dispositivo Vital Health, recuerda que nuestro sistema funciona con BLE, si tu dispositivo no dispone de esta tecnología lo sentimos...",
            "Selecciona el ítem de notificaciones, es el último ya que este sistema notifica cada medición del dispositivo al smartphone, luego de esto... Tranquilo, el menú es muy intuitivo",
            "Y listo!... Ya podrás visualizar tu ECG, grabar en tu registro la señal, mirar tu frecuencia cardiaca, ver tu promedio BPM durante la grabación y manipular la imagen en vivo",
            "No olvides que en tu registro de archivos, podrás ver todas tus grabaciones, el promedio del BMP durante la misma y desplazarse por la imagen"

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrucciones);

        stepTextView = findViewById(R.id.stepTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        stepView = findViewById(R.id.step_View);
        stepView.getState()
                .animationType(StepView.ANIMATION_ALL)
                .stepsNumber(5)
                .animationDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .commit();

        nextStep();

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setSelectedItemId(R.id.instrucciones);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.instrucciones:
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
    }

    private void nextStep() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stepIndex++;

                if (stepIndex < stepsTexts.length){
                    stepTextView.setText(stepsTexts[stepIndex]);
                    descriptionTextView.setText(descriptionTexts[stepIndex]);
                    stepView.go(stepIndex, true);
                    nextStep();
                }
            }
        }, 10000);
    }

    @Override public void onBackPressed() { return; }
}