package com.example.ecg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pl.droidsonroids.gif.GifImageView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Registro extends AppCompatActivity {

    private TextView nuevoUsuario, bienvenidoLabel, continuarLabel;
    private GifImageView registroGifImageView;
    private TextInputLayout usuarioRegistroTextField, contrasenaTextField, nameTextField, ageTextField, emailDoctorTextField;
    private MaterialButton inicioSesion;
    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText, ageEditText, emailDoctorEditText;

    private String name = " ";
    private String email = " ";
    private String password = " ";
    private String confirmPassword = " ";
    private String age = " ";
    private String emailDoctor = " ";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        registroGifImageView = findViewById(R.id.registroGifImageView);
        bienvenidoLabel = findViewById(R.id.bienvenidoLabel);
        continuarLabel = findViewById(R.id.continuarLabel);
        usuarioRegistroTextField = findViewById(R.id.usuarioRegistroTextField);
        contrasenaTextField = findViewById(R.id.contrasenaTextField);
        inicioSesion = findViewById(R.id.inicioSesion);
        nuevoUsuario = findViewById(R.id.nuevoUsuario);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        nameTextField = findViewById(R.id.nameTextField);
        ageTextField = findViewById(R.id.ageTextField);
        emailDoctorTextField = findViewById(R.id.emailDoctorTextField);
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        emailDoctorEditText = findViewById(R.id.emailDoctorEditText);

        nuevoUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionBack();
            }
        });

        inicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();

            }
        });

    }

    public void validate(){
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        confirmPassword = confirmPasswordEditText.getText().toString().trim();

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

        if (password.isEmpty() || password.length() <8){
            passwordEditText.setError("Se necesitan mas de 8 caracteres");
            return;
        } else if (!Pattern.compile("[0-9]").matcher(password).find()){
            passwordEditText.setError("Al menos un número");
            return;
        } else {
            passwordEditText.setError(null);
        }

        if (!confirmPassword.equals(password)){
            confirmPasswordEditText.setError("Deben ser iguales");
            return;
        }

        if (!name.isEmpty() && !age.isEmpty() && !emailDoctor.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()){
            registrar();
        }
    }

    public void registrar(){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", name);
                            map.put("email", email);
                            map.put("age", age);
                            map.put("emailDoctor", emailDoctor);

                            String id = mAuth.getCurrentUser().getUid();
                            mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task2) {
                                    Intent intent = new Intent(Registro.this, Usuario.class);
                                    startActivity(intent);
                                    finish();

                                }
                            });
                        } else {
                            Toast.makeText(Registro.this, "Fallo en registrarse", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }


    @Override
    public void onBackPressed(){
        transitionBack();
    }

    public void transitionBack(){
        Intent intent = new Intent(Registro.this, LoginActivity.class);

        Pair[] pairs= new Pair[7];
        pairs[0] = new Pair<View,String> (registroGifImageView, "logoImageTrans");
        pairs[1] = new Pair<View,String> (bienvenidoLabel, "textTrans");
        pairs[2] = new Pair<View,String> (continuarLabel, "iniciaSesionTextTrans");
        pairs[3] = new Pair<View,String> (usuarioRegistroTextField, "emailInputTextTrans");
        pairs[4] = new Pair<View,String> (contrasenaTextField, "passwordInputTextTrans");
        pairs[5] = new Pair<View,String> (inicioSesion, "buttonRegistroTrans");
        pairs[6] = new Pair<View,String> (nuevoUsuario, "newUserTrans");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation( Registro.this, pairs);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
            finish();
        }

    }
}