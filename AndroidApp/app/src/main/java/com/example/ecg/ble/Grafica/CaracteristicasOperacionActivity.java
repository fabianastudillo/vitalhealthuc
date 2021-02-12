package com.example.ecg.ble.Grafica;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ecg.ConexionBLE;
import com.example.ecg.SinRegistro;
import com.example.ecg.ble.DispositivosActivity;
import com.example.ecg.R;
import com.example.ecg.ble.Utilidades.HexString;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

public class CaracteristicasOperacionActivity extends AppCompatActivity {

    @BindView(R.id.bpm)
    ProgressBar bpm;

    @BindView(R.id.IdGraficaRT)
    LineChart mChart;
    private Thread thread;
    private boolean plotData = true;

    MaterialButton home;

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
        setContentView(R.layout.activity_grafica_sin_registro);
        ButterKnife.bind(this);
        String macAddress = getIntent().getStringExtra(DispositivosActivity.EXTRA_MAC_ADDRESS);
        characteristicUuid = (UUID) getIntent().getSerializableExtra(EXTRA_CHARACTERISTIC_UUID);
        bleDevice = ConexionBLE.getRxBleClient(this).getBleDevice(macAddress);
        connectionObservable = prepareConnectionObservable();
        //noinspection ConstantConditions
        getSupportActionBar().setSubtitle(getString(R.string.mac_address, macAddress));
        home = findViewById(R.id.home);

        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("ECG");
        mChart.getDescription().setTextSize(15);
        mChart.getDescription().setPosition(75, 40);
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

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaracteristicasOperacionActivity.this, SinRegistro.class);
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
        notifyButton.setVisibility(View.INVISIBLE);
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
        int pdat = s.indexOf(";");
        int pdat2 = s.indexOf("M");
        int pdat3 = s.indexOf("$");
        int endOfLineIndex = s.indexOf(" ");
        if (endOfLineIndex > 0) {
            String dataInPrint = s.substring((pdat + 1), (pdat3 ));
            String dataInPrint2 = s.substring((pdat2 + 1), endOfLineIndex);
            dato.setText("BPM: " + dataInPrint2);
            color(dataInPrint2);
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

    public void encender(){
        bpm.setVisibility(View.VISIBLE);
        dato.setVisibility(View.VISIBLE);
    }

    public void apagar(){
        bpm.setVisibility(View.INVISIBLE);
        dato.setVisibility(View.INVISIBLE);
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

    @Override public void onBackPressed() { return; }

}
