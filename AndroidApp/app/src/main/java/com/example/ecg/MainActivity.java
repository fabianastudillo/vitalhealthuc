package com.example.ecg;

import androidx.appcompat.app.AppCompatActivity;
import pl.droidsonroids.gif.GifImageView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Aniimaciones
        Animation animacion1 = AnimationUtils.loadAnimation(this,R.anim.desplazamiento_arriba);
        Animation animacion2 = AnimationUtils.loadAnimation(this,R.anim.desplazamiento_abajo);

        TextView versionTextView = findViewById(R.id.versionTextView);
        TextView uc = findViewById(R.id.uc);
        TextView vitalHealdTextView = findViewById(R.id.vitalHealdTextView);
        GifImageView logoGifImageView = findViewById(R.id.logoGifImageView);

        versionTextView.setAnimation(animacion2);
        uc.setAnimation(animacion2);
        vitalHealdTextView.setAnimation(animacion2);
        logoGifImageView.setAnimation(animacion1);

        new Handler().postDelayed(new Runnable() {

            @Override
                    public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                if(user != null && account != null){
                    Intent intent = new Intent(MainActivity.this, Registrado.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(MainActivity.this, Indicaciones.class);
                    startActivity(intent);
                    finish();
                }
            }
        },6900);

    }

}