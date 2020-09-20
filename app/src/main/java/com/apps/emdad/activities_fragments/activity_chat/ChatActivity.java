package com.apps.emdad.activities_fragments.activity_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.databinding.ActivityChatBinding;
import com.apps.emdad.models.OrderModel;
import com.apps.emdad.models.OrdersDataModel;
import com.apps.emdad.models.SingleOrderDataModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.apps.emdad.tags.Tags;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private final int IMG_REQ = 1;
    private final int CAMERA_REQ = 2;
    private final int MIC_REQ = 3;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String WRITE_PERM = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String CAMERA_PERM = Manifest.permission.CAMERA;
    private final String MIC_PERM = Manifest.permission.RECORD_AUDIO;
    private long audio_total_seconds = 0;
    private MediaRecorder recorder;
    private String audio_path = "";
    private Handler handler;
    private Runnable runnable;
    private Preferences preferences;
    private UserModel userModel;
    private int order_id;
    private OrderModel orderModel;
    private boolean isDataChanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        order_id = intent.getIntExtra("order_id",0);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        binding.imageChooser.setOnClickListener(v -> {
            if (binding.expandedLayout.isExpanded()) {
                binding.expandedLayout.collapse(true);

            } else {
                binding.expandedLayout.expand(true);

            }
        });

        binding.btnHide.setOnClickListener(v -> {
            binding.expandedLayout.collapse(true);
        });


        binding.imgGallery.setOnClickListener(v -> {
            checkGalleryPermission();

        });

        binding.imageCamera.setOnClickListener(v -> {
            checkCameraPermission();
        });

        binding.imageRecord.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isMicReady()) {
                    createMediaRecorder();

                } else {
                    checkMicPermission();
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                if (isMicReady()) {
                    try {
                        recorder.stop();
                        stopTimer();
                        sendAttachment(audio_path, "sound");
                    } catch (Exception e) {
                        Log.e("error1", e.getMessage() + "___");
                    }

                }

            }

            return true;
        });

        binding.llBack.setOnClickListener(v -> onBackPressed());
        getOrderById();

    }

    private void getOrderById() {
        Api.getService(Tags.base_url).getSingleOrder(userModel.getUser().getToken(),order_id)
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        binding.progBarData.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            orderModel = response.body().getOrder();
                            binding.setModel(orderModel);
                            updateUi(orderModel);

                        } else {
                            binding.progBarData.setVisibility(View.GONE);

                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                        try {
                            binding.progBarData.setVisibility(View.GONE);

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void updateUi(OrderModel orderModel) {
        Log.e("order_status",orderModel.getOrder_status()+"__");
        if (orderModel.getOrder_status().equals("new_order")||orderModel.getOrder_status().equals("driver_end_rate")||orderModel.getOrder_status().equals("client_end_and_rate")||orderModel.getOrder_status().equals("order_driver_back")){
            binding.imageRecord.setVisibility(View.GONE);
            binding.imageChooser.setVisibility(View.GONE);
            binding.imageSend.setVisibility(View.GONE);
            binding.msgContent.setVisibility(View.GONE);
        }else {
            binding.imageRecord.setVisibility(View.VISIBLE);
            binding.imageChooser.setVisibility(View.VISIBLE);
            binding.imageSend.setVisibility(View.VISIBLE);
            binding.msgContent.setVisibility(View.VISIBLE);
        }


        if (orderModel.getOrder_status().equals("new_order")){
            binding.orderStatus.setBackgroundResource(R.drawable.pending_bg);
            binding.orderStatus.setText(getString(R.string.pending));
        }else if (orderModel.getOrder_status().equals("client_cancel")){
            binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
            binding.orderStatus.setText(getString(R.string.cancel));
            binding.btnResend.setVisibility(View.VISIBLE);
        }else if (orderModel.getOrder_status().equals("driver_end_rate")||orderModel.getOrder_status().equals("client_end_and_rate")){
            binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
            binding.orderStatus.setText(getString(R.string.done));
            binding.btnResend.setVisibility(View.VISIBLE);

        }
    }
    private void resendOrder(){
        isDataChanged = true;
        binding.btnResend.setVisibility(View.GONE);
    }
    private void sendAttachment(String file_uri, String attachment_type) {
        /*Intent intent = new Intent(this, ServiceUploadAttachment.class);
        intent.putExtra("file_uri", file_uri);
        intent.putExtra("user_token", userModel.getData().getToken());
        intent.putExtra("user_id", userModel.getData().getId());
        intent.putExtra("room_id", chatUserModel.getRoom_id());
        intent.putExtra("attachment_type", attachment_type);
        startService(intent);*/


    }

    private void createMediaRecorder() {

        String audio_name = "AUD" + System.currentTimeMillis() + ".mp3";

        File file = new File(Tags.audio_path);
        boolean isFolderCreate;

        if (!file.exists()) {
            isFolderCreate = file.mkdir();
        } else {
            isFolderCreate = true;
        }


        if (isFolderCreate) {
            startTimer();
            binding.recordTime.setVisibility(View.VISIBLE);
            createVibration();
            audio_path = file.getAbsolutePath() + "/" + audio_name;
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioChannels(1);
            recorder.setOutputFile(audio_path);
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
        } else {
            Toast.makeText(this, "Unable to create sound file on your device", Toast.LENGTH_SHORT).show();
        }


    }

    private void createVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (vibrator != null) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));

            }
        } else {
            if (vibrator != null) {
                vibrator.vibrate(new long[]{200, 200}, 0);
            }
        }
    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, CAMERA_PERM) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, WRITE_PERM) == PackageManager.PERMISSION_GRANTED) {
            selectImage(CAMERA_REQ);

        } else {

            ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERM, WRITE_PERM}, CAMERA_REQ);

        }

    }


    private void checkGalleryPermission() {
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) == PackageManager.PERMISSION_GRANTED) {
            selectImage(IMG_REQ);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, IMG_REQ);

        }

    }


    private void checkMicPermission() {
        if (ActivityCompat.checkSelfPermission(this, MIC_PERM) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, WRITE_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{MIC_PERM, WRITE_PERM}, MIC_REQ);

        }

    }

    private boolean isMicReady() {

        if (ActivityCompat.checkSelfPermission(this, MIC_PERM) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, WRITE_PERM) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }

    private void selectImage(int req) {

        Intent intent = new Intent();
        if (req == IMG_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            }
            intent.setType("image/*");


        } else if (req == CAMERA_REQ) {

            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        }

        startActivityForResult(intent, req);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMG_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage(requestCode);
            } else {
                Toast.makeText(this, "Access image denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                selectImage(requestCode);
            } else {
                Toast.makeText(this, "Access camera denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MIC_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Access camera denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMG_REQ && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            sendAttachment(uri.toString(), "img");

        } else if (requestCode == CAMERA_REQ && resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            Uri uri = getUriFromBitmap(bitmap);
            sendAttachment(uri.toString(), "img");

        }

    }


    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
        return Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", ""));

    }


    private void startTimer() {
        handler = new Handler();
        runnable = () -> {
            audio_total_seconds += 1;
            binding.recordTime.setText(getRecordTimeFormat(audio_total_seconds));
            startTimer();
        };

        handler.postDelayed(runnable, 1000);
    }

    private void stopTimer() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        audio_total_seconds = 0;
        if (runnable != null) {
            handler.removeCallbacks(runnable);

        }
        handler = null;
        binding.recordTime.setText("00:00:00");
        binding.recordTime.setVisibility(View.GONE);
    }

    private String getRecordTimeFormat(long seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int second = (int) (seconds % 60);

        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, second);

    }

    @Override
    public void onBackPressed() {
        if (isDataChanged){
            setResult(RESULT_OK);
        }

        finish();
    }
}