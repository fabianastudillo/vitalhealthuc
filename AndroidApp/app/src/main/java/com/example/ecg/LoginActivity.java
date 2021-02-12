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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    TextView bienvenidoLabel, continuarLabel, nuevoUsuario, olvidasteContrasena;
    GifImageView logoGifImageView;
    TextInputLayout usuarioTextField, contrasenaTextField;
    MaterialButton inicioSesion, sinRegistro;
    TextInputEditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    public static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logoGifImageView = findViewById(R.id.logoGifImageView);
        bienvenidoLabel = findViewById(R.id.bienvenidoLabel);
        continuarLabel = findViewById(R.id.continuarLabel);
        usuarioTextField = findViewById(R.id.usuarioTextField);
        contrasenaTextField = findViewById(R.id.contrasenaTextField);
        inicioSesion = findViewById(R.id.inicioSesion);
        nuevoUsuario = findViewById(R.id.nuevoUsuario);
        olvidasteContrasena = findViewById(R.id.olvidasteContra);
        sinRegistro = findViewById(R.id.sinRegistro);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        nuevoUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Registro.class);

                Pair[] pairs= new Pair[7];
                pairs[0] = new Pair<View,String> (logoGifImageView, "logoImageTrans");
                pairs[1] = new Pair<View,String> (bienvenidoLabel, "textTrans");
                pairs[2] = new Pair<View,String> (continuarLabel, "iniciaSesionTextTrans");
                pairs[3] = new Pair<View,String> (usuarioTextField, "emailInputTextTrans");
                pairs[4] = new Pair<View,String> (contrasenaTextField, "passwordInputTextTrans");
                pairs[5] = new Pair<View,String> (inicioSesion, "buttonRegistroTrans");
                pairs[6] = new Pair<View,String> (nuevoUsuario, "newUserTrans");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation( LoginActivity.this, pairs);
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                    finish();
                }

            }
        });

        sinRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SinRegistro.class);
                startActivity(intent);
                finish();
            }
        });

        olvidasteContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });

        inicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        // Google Sign-In
        signInButton = findViewById(R.id.loginGoogle);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle (){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e){
                Toast.makeText(LoginActivity.this, "Fallo Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String id = mAuth.getCurrentUser().getUid();
                            DatabaseReference userDataBase = FirebaseDatabase.getInstance().getReference("Users/"+id+"/name");
                            userDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent intent = new Intent(LoginActivity.this, Usuario.class);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        Intent intent = new Intent(LoginActivity.this, CompletarRegistroGogle.class);
                                        intent.putExtra("com.example.ecg.CompletarRegistroGogle",(mAuth.getUid()));
                                        intent.putExtra("com.example.ecg.Archivo",(mAuth.getUid()));
                                        intent.putExtra("com.example.ecg.ble_login.Grafica.CaracteristicasOperacionActivity",(mAuth.getUid()));
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Fallo en iniciar sesion", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void validate(){
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("Correo invalido");
        } else {
            emailEditText.setError(null);
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
            iniciarSesion(email, password);
    }

    public void iniciarSesion(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(LoginActivity.this, Usuario.class);
                            intent.putExtra("com.example.ecg.ble_login.Grafica",(mAuth.getUid()));
                            intent.putExtra("com.example.ecg.Archivo",(mAuth.getUid()));
                            intent.putExtra("com.example.ecg.ble_login.Grafica.CaracteristicasOperacionActivity",(mAuth.getUid()));
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Credenciales equivocadas, trata de nuevo", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override public void onBackPressed() { return; }
}