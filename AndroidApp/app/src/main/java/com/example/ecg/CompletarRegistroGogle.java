package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pl.droidsonroids.gif.GifImageView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CompletarRegistroGogle extends AppCompatActivity {

    private TextView  bienvenidoLabel, continuarLabel;
    private GifImageView registroGifImageView;
    private TextInputLayout  nameTextField, ageTextField, emailDoctorTextField;
    private MaterialButton inicioSesion;
    private TextInputEditText emailEditText, nameEditText, ageEditText, emailDoctorEditText;

    private String name = " ";
    private String age = " ";
    private String email = " ";
    private String emailDoctor = " ";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private FirebaseFirestore db;
    private String usrid;
    private String docid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completar_registro_gogle);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        db=FirebaseFirestore.getInstance();
        if (getIntent().hasExtra("com.example.ecg.LoginActivity")){
            usrid = getIntent().getExtras().getString("com.example.ecg.LoginActivity");
        }
        if (getIntent().hasExtra("com.example.ecg.LoginActivity")){
            docid = getIntent().getExtras().getString("com.example.ecg.LoginActivity");
        }

        registroGifImageView = findViewById(R.id.registroGifImageView);
        bienvenidoLabel = findViewById(R.id.bienvenidoLabel);
        continuarLabel = findViewById(R.id.continuarLabel);
        inicioSesion = findViewById(R.id.inicioSesion);
        emailEditText = findViewById(R.id.emailEditText);

        nameTextField = findViewById(R.id.nameTextField);
        ageTextField = findViewById(R.id.ageTextField);
        emailDoctorTextField = findViewById(R.id.emailDoctorTextField);
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        emailDoctorEditText = findViewById(R.id.emailDoctorEditText);

        inicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();

            }
        });
    }



    public void validate(){
        email = emailEditText.getText().toString().trim();
        name = nameEditText.getText().toString().trim();
        age = ageEditText.getText().toString().trim();
        emailDoctor = emailDoctorEditText.getText().toString().trim();

        if(!name.isEmpty()){
            nameEditText.setError(null);
        }else {
            nameEditText.setError("Nombre completo es necesario");
        }

        if(!age.isEmpty()){
            ageEditText.setError(null);
        }else {
            ageEditText.setError("Edad es necesaria");
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("Correo invalido");
        } else {
            emailEditText.setError(null);
        }

        if (emailDoctor.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailDoctor).matches()){
            emailDoctorEditText.setError("Correo invalido");
        } else {
            emailDoctorEditText.setError(null);
        }

        if (!name.isEmpty() && !age.isEmpty() && !emailDoctor.isEmpty() && !email.isEmpty()){
            registrar();
        }
    }

    private void registrar() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("age", age);
        map.put("emailDoctor", emailDoctor);

        String id = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(CompletarRegistroGogle.this, Usuario.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(CompletarRegistroGogle.this, "Fallo ingreso de datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override public void onBackPressed() { return; }
}