package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.ecg.ble_login.Escaneo.Escaneoctivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Archivo extends AppCompatActivity {

    List<ListElement> elements;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    ArrayList<String> Fecha = new ArrayList<String>();
    ArrayList<String> ECG = new ArrayList<String>();
    String [] Fech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archivo);

        SwipeRefreshLayout refreshLayout = findViewById(R.id.swipToRefresh);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i=0; i<=100; i++){
                            for (int j=0; j<10000000; j++){
                            }
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
                thread.start();
            }
        });

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setSelectedItemId(R.id.archivo);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
                        return true;
                    case R.id.usuario:
                        startActivity(new Intent(getApplicationContext(),Usuario.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        LeerFecha();
    }

    public void init(){
        Fech = new String[Fecha.size()];
        int cantidad = Fecha.size();
        Fecha.toArray(Fech);
        elements = new ArrayList<>();
        for (int i = 0; i < cantidad; i++){
            String item = Fech[i];
            elements.add(new ListElement(item));
            //Log.e("Item: ", String.valueOf(item));
        }
        ListAdapter listAdapter = new ListAdapter(elements, this, new  ListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ListElement item) {

                moveToDescription(item);
            }
        });
        RecyclerView recyclerView = findViewById(R.id.listReciclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);
    }

    public void moveToDescription(ListElement item){

        Intent intent = new Intent(Archivo.this, GraficaArchivo.class);
        intent.putExtra("ListElement", item);
        startActivity(intent);
        finish();
    }

    public void LeerFecha(){
        String id = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(id).child("Grabaciones").child("Fecha").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    StringBuffer date = new StringBuffer("");
                    date.append(snapshot.getKey());
                    Fecha.add(date.toString());
                }
                //Log.e("Fechas: ", String.valueOf(Fecha));
                init();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override public void onBackPressed() { return; }
}