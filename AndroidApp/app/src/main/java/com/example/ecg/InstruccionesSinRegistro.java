package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ecg.ble.Escaneo.EscaneoActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.shuhart.stepview.StepView;

public class InstruccionesSinRegistro extends AppCompatActivity {

    MaterialButton registrate;

    StepView stepView;
    TextView stepTextView, descriptionTextView;

    int stepIndex = 0;
    String[] stepsTexts = {"Paso 1", "Paso 2", "Paso 3", "Paso 4", "Paso 5"};
    String[] descriptionTexts = {"Dirígete a la sección de señal, es la que tiene un corazón!... No olvides colocar los electrodos de la manera correcta, muñecas y pierna o la opción de pecho y abdomen",
            "Activa tu bluetooth, y selecciona el dispositivo Vital Health, recuerda que nuestro sistema funciona con BLE, si tu dispositivo no dispone de esta tecnología lo sentimos...",
            "Selecciona el ítem de notificaciones, es el último ya que este sistema notifica cada medición del dispositivo al smartphone, luego de esto... Tranquilo, el menú es muy intuitivo",
            "Y listo!... Ya podrás visualizar tu ECG, mirar tu frecuencia cardiaca,manipular la imagen en vivo, si quieres de mas beneficios, debes registrarte a nuestro servicio, no te arrepentiras",
            "Nuestro sistema te permitirá realizar grabaciones de tu ECG, mirar el promedio de tu BPM durante la grabación, acceder a tu registro de grabaciones cuando quieras.."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrucciones_sin_registro);

        stepTextView = findViewById(R.id.stepTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        stepView = findViewById(R.id.step_View);
        stepView.getState()
                .animationType(StepView.ANIMATION_ALL)
                .stepsNumber(5)
                .animationDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .commit();

        nextStep();

        registrate = findViewById(R.id.registrate);

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setSelectedItemId(R.id.instrucciones);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.instrucciones:
                        return true;
                    case R.id.senal:
                        startActivity(new Intent(getApplicationContext(), EscaneoActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        registrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InstruccionesSinRegistro.this, LoginActivity.class);
                startActivity(intent);
                finish();
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