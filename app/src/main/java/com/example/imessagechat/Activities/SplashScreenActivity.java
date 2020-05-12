package com.example.imessagechat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.imessagechat.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);



        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                mFirebaseAuth = FirebaseAuth.getInstance();

                if(mFirebaseAuth.getCurrentUser() != null){
                    //Logged in
                    finish();
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                }
                else{
                    //Send to login screen
                    finish();
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                }

            }
        }, 1000);
    }
}