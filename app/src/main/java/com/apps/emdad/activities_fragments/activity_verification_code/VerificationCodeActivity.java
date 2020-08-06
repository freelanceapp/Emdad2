package com.apps.emdad.activities_fragments.activity_verification_code;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_sign_up.SignUpActivity;
import com.apps.emdad.databinding.ActivityVerificationCodeBinding;

import java.util.Locale;
import java.util.Timer;

public class VerificationCodeActivity extends AppCompatActivity {
    private ActivityVerificationCodeBinding binding;
    private String phone_code;
    private String phone;
    private boolean canSend = false;
    private  CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_verification_code);
        getDataFromIntent();
        initView();

    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent!=null){
            phone_code = intent.getStringExtra("phone_code");
            phone = intent.getStringExtra("phone");

        }
    }

    private void initView() {
        String mPhone= phone_code+phone;
        binding.setPhone(mPhone);
        startCounter();
        binding.btnResendCode.setOnClickListener(v -> {
            if (canSend){
                resendCode();
            }
        });
    }



    private void startCounter() {
        countDownTimer = new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) ((millisUntilFinished/1000) / 60);
                int seconds = (int) ((millisUntilFinished/1000) % 60);

                String time = String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
                binding.btnResendCode.setText(String.format(Locale.ENGLISH, "%s %s", getString(R.string.resend_in), time));
                binding.btnResendCode.setTextColor(ContextCompat.getColor(VerificationCodeActivity.this, R.color.color4));
                binding.btnResendCode.setBackgroundResource(R.color.transparent);

            }

            @Override
            public void onFinish() {
                canSend = true;
                binding.btnResendCode.setText(R.string.resend);
                binding.btnResendCode.setTextColor(ContextCompat.getColor(VerificationCodeActivity.this, R.color.colorPrimary));
                binding.btnResendCode.setBackgroundResource(R.color.white);

                Intent intent = new Intent(VerificationCodeActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
                stopTimer();

            }
        };

        countDownTimer.start();
    }

    private void resendCode() {
        if (countDownTimer!=null){
            countDownTimer.start();
        }
    }

    private void stopTimer() {
        if (countDownTimer!=null){
            countDownTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopTimer();
    }


}