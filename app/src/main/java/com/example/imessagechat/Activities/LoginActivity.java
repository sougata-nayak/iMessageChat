package com.example.imessagechat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.imessagechat.R;

public class LoginActivity extends AppCompatActivity {


    private EditText userPhone;
    private Button sendCodeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userPhone = findViewById(R.id.editTextMobile);
        sendCodeButton = findViewById(R.id.buttonContinue);

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number = userPhone.getText().toString().trim();

                if(number.isEmpty() || number.length() < 10){

                    userPhone.setError("Number is required");
                    userPhone.requestFocus();
                    return;
                }

                Intent intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("phoneNumber", number);
                startActivity(intent);

            }
        });

    }

}
