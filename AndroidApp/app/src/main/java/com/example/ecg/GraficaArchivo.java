package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GraficaArchivo extends AppCompatActivity implements View.OnClickListener {

    TextView datoFecha, bpmTextView;
    private String Fecha;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase1;
    ArrayList<String> ECG = new ArrayList<String>();
    String [] ecg;
    String ecgGrafica;
    String bpmGrafica;
    MaterialButton delate;
    MaterialButton regresar;

    private LineChart mChart1;
    private Thread thread1;
    private boolean plotData1 = true;

    private int cantidad1 = 0;
    private int value1 = 0;
    private int bpmdato1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafica_archivo);

        ListElement element = (ListElement) getIntent().getSerializableExtra("ListElement");
        datoFecha = findViewById(R.id.datoFecha);
        bpmTextView = findViewById(R.id.bpmTextView);
        datoFecha.setText(element.getFecha());
        Fecha = datoFecha.getText().toString();

        delate = findViewById(R.id.delate);
        delate.setOnClickListener(this);

        regresar = findViewById(R.id.regresar);
        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraficaArchivo.this, Archivo.class);
                startActivity(intent);
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase1 = FirebaseDatabase.getInstance().getReference();

        mChart1 = (LineChart) findViewById(R.id.GraficaFile);

        mChart1.getDescription().setEnabled(true);
        mChart1.getDescription().setText("ECG");
        mChart1.getDescription().setTextSize(25);
        mChart1.getDescription().setPosition(100, 50);
        mChart1.setTouchEnabled(true);
        mChart1.setDragEnabled(true);
        mChart1.setScaleEnabled(false);
        mChart1.setDrawGridBackground(false);
        mChart1.setPinchZoom(true);
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart1.setData(data);

        Legend l = mChart1.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextColor(Color.BLACK);

        mChart1.setDrawBorders(false);
        mChart1.setDrawGridBackground(false);
        mChart1.getDescription().setEnabled(true);
        mChart1.getLegend().setEnabled(true);
        mChart1.getAxisLeft().setDrawGridLines(false);
        mChart1.getAxisLeft().setDrawLabels(false);
        mChart1.getAxisLeft().setDrawAxisLine(false);
        mChart1.getXAxis().setDrawGridLines(false);
        mChart1.getXAxis().setDrawLabels(false);
        mChart1.getXAxis().setDrawAxisLine(false);
        mChart1.getAxisRight().setDrawGridLines(false);
        mChart1.getAxisRight().setDrawLabels(false);
        mChart1.getAxisRight().setDrawAxisLine(false);

        XAxis xl = mChart1.getXAxis();
        xl.setDrawGridLines(false);
        xl.setDrawGridLinesBehindData(false);
        //xl.setGridColor(Color.rgb(255, 87, 34));
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis lA = mChart1.getAxisLeft();
        lA.setDrawGridLines(true);
        lA.setDrawGridLinesBehindData(true);
        lA.setGridColor(Color.rgb(255, 87, 34));
        lA.setAxisMaximum(4500f);
        lA.setAxisMinimum(-2500f);


        YAxis rA = mChart1.getAxisRight();
        rA.setEnabled(false);

        mChart1.getAxisLeft().setDrawGridLines(false);
        mChart1.getXAxis().setDrawGridLines(false);
        mChart1.setDrawBorders(false);

        startPlot1();
        LeerDatos();
    }

    public void LeerDatos(){
        String id = mAuth.getCurrentUser().getUid();
        mDatabase1.child("Users").child(id).child("Grabaciones").child("Fecha").child(Fecha).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    StringBuffer ecg = new StringBuffer("");
                    ecg.append(snapshot.getValue());
                    ECG.add(ecg.toString());
                }
                //Log.e("ECG: ", String.valueOf(ECG));
                ecg = new String[ECG.size()];
                cantidad1 = ECG.size();
                ECG.toArray(ecg);
                for (int i = 0; i < cantidad1; i++){
                    int dato1 = ecg[i].indexOf("=");
                    int dato2 = ecg[i].indexOf(",");
                    int dato3 = ecg[i].indexOf("M");
                    int dato4 = ecg[i].indexOf("}");
                    ecgGrafica = ecg[i].substring(dato1+1,dato2);
                    bpmGrafica = ecg[i].substring(dato3+2,dato4);
                    int gg=Integer.parseInt(bpmGrafica);
                    value1 = value1+gg;
                    addEntry1(ecgGrafica);
                }
                bpmdato1 = value1/cantidad1;
                bpmTextView.setText("BPM: "+String.valueOf(bpmdato1));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addEntry1(String dato){
        LineData data = mChart1.getData();
        if(data != null){
            ILineDataSet set = data.getDataSetByIndex(0);
            if(set==null){
                set=createSet();
                data.addDataSet(set);
            }
            int datint = 0;
            try{
                datint = Integer.parseInt(dato);
            }catch(NumberFormatException ex){ // handle your exception
            }
            data.addEntry( new Entry(set.getEntryCount(), datint),0);
            data.notifyDataChanged();
            mChart1.notifyDataSetChanged();
            mChart1.setVisibleXRangeMaximum(200);
            mChart1.moveViewToX(data.getEntryCount());
        }
    }

    //Caracteristicas linea de Grafica
    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "Vital Healt");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1f);
        set.setColor(Color.BLACK);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private void startPlot1(){
        if(thread1 !=null){
            thread1.interrupt();
        }
        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    plotData1 = true;
                    try {
                        Thread.sleep(0, 10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread1.start();
    }

    @Override
    public void onClick(View v) {

        AlertDialog.Builder alerta = new AlertDialog.Builder(GraficaArchivo.this);
        alerta.setMessage("¿Deseas borrar el archivo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = mAuth.getCurrentUser().getUid();
                        mDatabase1.child("Users").child(id).child("Grabaciones").child("Fecha").child(Fecha).removeValue();
                        Intent intent = new Intent(GraficaArchivo.this, Archivo.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog titulo =alerta.create();
        titulo.setTitle("Borrar registro");
        titulo.show();


    }

    @Override public void onBackPressed() { return; }

}