package com.example.ecg.ble_login.Grafica;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecg.ConexionBLE;
import com.example.ecg.DatosEmail;
import com.example.ecg.MailJob;
import com.example.ecg.R;
import com.example.ecg.Registrado;
import com.example.ecg.ble.Utilidades.HexString;
import com.example.ecg.ble_login.DispositivosActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class CaracteristicasOperacionActivity extends AppCompatActivity  implements View.OnClickListener {

    MaterialButton record_toggle_btn;
    FirebaseFirestore db;
    private String usrid;
    private String docid;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean saveData = true;
    private Date fecha;
    private int value;
    ArrayList<String> BPMdat = new ArrayList<String>();
    String [] bpmDat;
    String bpmEmail;

    private DatabaseReference dbEmail;
    private ValueEventListener eventListener;
    private String CorreoMedico;
    private String NombreUsuario;

    private int cantidad = 0;
    private int bpmdato = 0;

    MaterialButton home;

    @BindView(R.id.derivacion)
    TextView derivacion;

    @BindView(R.id.grabado)
    ProgressBar grabado;
    @BindView(R.id.temporizador)
    TextView temporizador;

    @BindView(R.id.bpm)
    ProgressBar bpm;

    @BindView(R.id.IdGraficaRT)
    LineChart mChart;
    private Thread thread;
    private boolean plotData = true;
    public int cambioEstado = 0;

    public static final String EXTRA_CHARACTERISTIC_UUID = "extra_uuid";
    @BindView(R.id.connect)
    Button connectButton;
    @BindView(R.id.dato)
    TextView dato;
    @BindView(R.id.notify)
    Button notifyButton;
    private UUID characteristicUuid;
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    private RxBleDevice bleDevice;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CaracteristicasOperacionActivity() {
    }

    public static Intent startActivityIntent(Context context, String peripheralMacAddress, UUID characteristicUuid) {
        Intent intent = new Intent(context, CaracteristicasOperacionActivity.class);
        intent.putExtra(DispositivosActivity.EXTRA_MAC_ADDRESS, peripheralMacAddress);
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristicUuid);
        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafica_con_registro);


        home = findViewById(R.id.home);
        record_toggle_btn = findViewById(R.id.record_toggle_btn);
        db=FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getIntent().hasExtra("com.example.ecg.LoginActivity")){
            usrid = getIntent().getExtras().getString("com.example.ecg.LoginActivity");
        }
        if (getIntent().hasExtra("com.example.ecg.LoginActivity")){
            docid = getIntent().getExtras().getString("com.example.ecg.LoginActivity");
        }

        record_toggle_btn.setOnClickListener(this);

        ButterKnife.bind(this);
        String macAddress = getIntent().getStringExtra(DispositivosActivity.EXTRA_MAC_ADDRESS);
        characteristicUuid = (UUID) getIntent().getSerializableExtra(EXTRA_CHARACTERISTIC_UUID);
        bleDevice = ConexionBLE.getRxBleClient(this).getBleDevice(macAddress);
        connectionObservable = prepareConnectionObservable();
        //noinspection ConstantConditions
        getSupportActionBar().setSubtitle(getString(R.string.mac_address, macAddress));

        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("ECG");
        mChart.getDescription().setTextSize(15);
        mChart.getDescription().setPosition(70, 40);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);


        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextColor(Color.BLACK);

        mChart.setDrawBorders(false);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(true);
        mChart.getLegend().setEnabled(true);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setDrawLabels(false);
        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getAxisRight().setDrawLabels(false);
        mChart.getAxisRight().setDrawAxisLine(false);

        XAxis xl = mChart.getXAxis();
        xl.setDrawGridLines(false);
        xl.setDrawGridLinesBehindData(false);
        //xl.setGridColor(Color.rgb(255, 87, 34));
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis lA = mChart.getAxisLeft();
        lA.setDrawGridLines(true);
        lA.setDrawGridLinesBehindData(true);
        lA.setGridColor(Color.rgb(255, 87, 34));
        lA.setAxisMaximum(3500f);
        lA.setAxisMinimum(-400f);


        YAxis rA = mChart.getAxisRight();
        rA.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);
        startPlot();
        startsave();
        DatosCorreo();

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaracteristicasOperacionActivity.this, Registrado.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void startPlot(){
        if(thread !=null){
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    plotData = true;
                    try {
                        Thread.sleep(1);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void startsave(){
        if(thread !=null){
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    saveData = true;
                    try {
                        Thread.sleep(1);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void addEntry(String dato){
        LineData data = mChart.getData();
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
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(100);
            mChart.moveViewToX(data.getEntryCount());
        }
    }



    //Caracteristicas linea de Grafica
    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "Vital Health                                                      10 mm/mV");
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

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(ReplayingShare.instance());
    }

    @OnClick(R.id.connect)
    public void onConnectToggleClick() {
        notifyButton.setVisibility(View.VISIBLE);
        if (isConnected()) {
            triggerDisconnect();
        } else {
            final Disposable connectionDisposable = connectionObservable
                    .flatMapSingle(RxBleConnection::discoverServices)
                    .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(characteristicUuid))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> connectButton.setText(R.string.connecting))
                    .subscribe(
                            characteristic -> {
                                updateUI(characteristic);
                                Log.i(getClass().getSimpleName(), "Hey, connection has been established!");
                            },
                            this::onConnectionFailure,
                            this::onConnectionFinished
                    );

            compositeDisposable.add(connectionDisposable);

        }
        apagar();
    }

    @OnClick(R.id.notify)
    public void onNotifyClick() {

        if (isConnected()) {
            final Disposable disposable = connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUuid))
                    .doOnNext(notificationObservable -> runOnUiThread(this::notificationHasBeenSetUp))
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);

            compositeDisposable.add(disposable);
        }
        encender();
    }

    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(R.id.main), "Connection error: " + throwable, Snackbar.LENGTH_SHORT);
        updateUI(null);
    }

    private void onConnectionFinished() {
        updateUI(null);
    }

    private void onReadFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(R.id.main), "Read error: " + throwable, Snackbar.LENGTH_SHORT);
    }

    private void onWriteSuccess() {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(R.id.main), "Write success", Snackbar.LENGTH_SHORT);
    }

    private void onWriteFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(R.id.main), "Write error: " + throwable, Snackbar.LENGTH_SHORT);
    }

    //noinspection ConstantConditions
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void onNotificationReceived(byte[] bytes) {
        cargarDatos(bytes);
        Snackbar.make(findViewById(R.id.main), "Change: " + HexString.bytesToHex(bytes), Snackbar.LENGTH_SHORT);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void cargarDatos(byte[] bytes){
        String s = new String(bytes, StandardCharsets.UTF_8);
        int pdat = s.indexOf("G");
        int pdat2 = s.indexOf("M");
        int endOfLineIndex = s.indexOf(" ");
        int pdat3 = s.indexOf(";");
        if (endOfLineIndex > 0) {
            String dataInPrint = s.substring((pdat + 1), (pdat2));
            String dataInPrint2 = s.substring((pdat2 + 1), endOfLineIndex);
            String derivaciondato = s.substring((endOfLineIndex + 1), pdat3);
            dato.setText("BPM: " + dataInPrint2);
            derivacion.setText(derivaciondato);
            color(dataInPrint2);
            if (cambioEstado ==1 ){
                nuevoDato(dataInPrint, dataInPrint2);
            }
            if (plotData) {
                addEntry(dataInPrint);
                plotData = false;
            }
        }
    }

    private void color(String dataInPrint2) {
        int graf = Integer.parseInt(dataInPrint2);
        if(graf <= 60){
            bpm.getIndeterminateDrawable()
                    .setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
        }
        if(graf >= 100){
            bpm.getIndeterminateDrawable()
                    .setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        } if (graf >60 && graf <100){
            bpm.getIndeterminateDrawable()
                    .setColorFilter(Color.rgb(24,255,255 ), PorterDuff.Mode.SRC_IN);
        }
    }

    public void nuevoDato(String ecg, String bpm){
        Map<String, Object> mant = new HashMap<>();
        mant.put("ECG", ecg);
        mant.put("BPM", bpm);
        String id = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(id).child("Grabaciones").child("Fecha").child(String.valueOf(fecha)).push().setValue(mant).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

    }

    public void DatosCorreo(){
        String id = mAuth.getCurrentUser().getUid();
        dbEmail =
                FirebaseDatabase.getInstance().getReference()
                        .child("Users").child(id);

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DatosEmail pred = dataSnapshot.getValue(DatosEmail.class);
                CorreoMedico =pred.getEmailDoctor();
                NombreUsuario = pred.getName();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error!", ""+databaseError.toException());
            }
        };

        dbEmail.addValueEventListener(eventListener);
    }

    private void email(Date fecha){
        String id = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(id).child("Grabaciones").child("Fecha").child(String.valueOf(fecha)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    StringBuffer ecg = new StringBuffer("");
                    ecg.append(snapshot.getValue());
                    BPMdat.add(ecg.toString());
                }
                    bpmDat = new String[BPMdat.size()];
                    cantidad = BPMdat.size();
                    BPMdat.toArray(bpmDat);
                    for (int i = 0; i < cantidad; i++){
                        int dato3 = bpmDat[i].indexOf("M");
                        int dato4 = bpmDat[i].indexOf("}");
                        bpmEmail = bpmDat[i].substring(dato3+2,dato4);
                        int gg=Integer.parseInt(bpmEmail);
                        value = value+gg;
                    }
                    bpmdato = value/cantidad;
                    Toast.makeText(CaracteristicasOperacionActivity.this, "Promedio BPM en la grabación: " + bpmdato +" bpm", Toast.LENGTH_SHORT).show();

                    if (bpmdato > 60 && bpmdato <100){
                        Toast.makeText(CaracteristicasOperacionActivity.this, "Tu frecuencia cardiaca es normal", Toast.LENGTH_SHORT).show();
                    }

                    if (bpmdato < 60) {
                        Toast.makeText(CaracteristicasOperacionActivity.this, "Posible bradicardia, tu medico fue notificado", Toast.LENGTH_SHORT).show();
                        new MailJob("vitalhealt2020@gmail.com", "iomismo2604").execute(
                                new MailJob.Mail("vitalhealt2020@gmail.com", ""+CorreoMedico, "Notificacion Vital Health", "El paciente "+NombreUsuario+" tiene una frecuencia cardiaca baja, posible bradicardia")
                        );
                    }

                    if (bpmdato > 100) {
                        Toast.makeText(CaracteristicasOperacionActivity.this, "Posible taquicardia, tu medico fue notificado", Toast.LENGTH_SHORT).show();
                        new MailJob("vitalhealt2020@gmail.com", "iomismo2604").execute(
                                new MailJob.Mail("vitalhealt2020@gmail.com", ""+CorreoMedico, "Notificacion Vital Health", "El paciente "+NombreUsuario+" tiene una frecuencia cardiaca alta, posible taquicardia")
                        );
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        value=0;
        bpmdato=0;
    }

    public void encender(){
        bpm.setVisibility(View.VISIBLE);
        dato.setVisibility(View.VISIBLE);
        record_toggle_btn.setVisibility(View.VISIBLE);
        notifyButton.setVisibility(View.INVISIBLE);
    }

    public void apagar(){
        bpm.setVisibility(View.INVISIBLE);
        dato.setVisibility(View.INVISIBLE);
        record_toggle_btn.setVisibility(View.INVISIBLE);
    }


    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(R.id.main), "Notifications error: " + throwable, Snackbar.LENGTH_SHORT);
    }

    private void notificationHasBeenSetUp() {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(R.id.main), "ECG Activo", Snackbar.LENGTH_SHORT);
    }

    private void triggerDisconnect() {
        disconnectTriggerSubject.onNext(true);
    }

    /**
     * This method updates the UI to a proper state.
     *
     * @param characteristic a nullable {@link BluetoothGattCharacteristic}. If it is null then UI is assuming a disconnected state.
     */
    private void updateUI(BluetoothGattCharacteristic characteristic) {
        connectButton.setText(characteristic != null ? R.string.disconnect : R.string.connect);
        notifyButton.setEnabled(hasProperty(characteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY));
    }

    private boolean hasProperty(BluetoothGattCharacteristic characteristic, int property) {
        return characteristic != null && (characteristic.getProperties() & property) > 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        compositeDisposable.clear();
    }

    @Override public void onBackPressed() {
        return; }

    @Override
    public void onClick(View v) {

        fecha = new Date();
        cambioEstado = 1;
        new CountDownTimer(5500, 1000) {
            public void onTick(long millisUntilFinished) {
                long segundos = millisUntilFinished/1000;
                record_toggle_btn.setVisibility(View.INVISIBLE);
                temporizador.setVisibility(View.VISIBLE);
                grabado.setVisibility(View.VISIBLE);
                temporizador.setText(segundos+"s");
            }
            public void onFinish() {
                cambioEstado = 0;
                temporizador.setVisibility(View.INVISIBLE);
                grabado.setVisibility(View.INVISIBLE);
                Toast.makeText(CaracteristicasOperacionActivity.this, "Datos guardados con exito", Toast.LENGTH_SHORT).show();
                record_toggle_btn.setVisibility(View.VISIBLE);
                email(fecha);
            }
        }.start();
    }
    
}
