package com.apps.emdad.activities_fragments.activity_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_delegate_add_offer.DelegateAddOfferActivity;
import com.apps.emdad.adapters.ChatActionAdapter;
import com.apps.emdad.adapters.OffersAdapter;
import com.apps.emdad.databinding.ActivityChatBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.ChatActionModel;
import com.apps.emdad.models.FromToLocationModel;
import com.apps.emdad.models.OffersDataModel;
import com.apps.emdad.models.OffersModel;
import com.apps.emdad.models.OrderModel;
import com.apps.emdad.models.OrdersDataModel;
import com.apps.emdad.models.RangeOfferModel;
import com.apps.emdad.models.SingleOrderDataModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.apps.emdad.tags.Tags;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
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
    private int offer_current_page = 1;
    private boolean offer_isLoading = false;
    private List<OffersModel> offersModelList;
    private OffersAdapter offersAdapter;
    private String currency = "";
    private String lang;
    private List<ChatActionModel> actionReasonList;
    private ChatActionAdapter chatActionAdapter;
    private ChatActionModel chatActionModel = null;
    private OffersModel offersModel = null;
    private int reasonType = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        order_id = intent.getIntExtra("order_id", 0);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        actionReasonList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        offersModelList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        binding.setLang(lang);
        currency = getString(R.string.sar);
        if (userModel != null) {
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }
        binding.recViewOffers.setLayoutManager(new LinearLayoutManager(this));
        offersAdapter = new OffersAdapter(offersModelList, this, currency);
        binding.recViewOffers.setAdapter(offersAdapter);
        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    LinearLayoutManager manager = (LinearLayoutManager) binding.recView.getLayoutManager();
                    int last_item_pos = manager.findLastCompletelyVisibleItemPosition();
                    int total_items_count = binding.recView.getAdapter().getItemCount();
                    if (last_item_pos == (total_items_count - 2) && !offer_isLoading) {
                        int page = offer_current_page + 1;
                        loadMoreOffer(page);
                    }
                }
            }
        });
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
        binding.tvReadyDeliverOrder.setOnClickListener(v -> {
            navigateToDriverAddOffer();
        });
        chatActionAdapter = new ChatActionAdapter(actionReasonList, this);
        binding.recViewAction.setLayoutManager(new LinearLayoutManager(this));
        binding.recViewAction.setAdapter(chatActionAdapter);

        binding.btnActionOk.setOnClickListener(v ->
        {
            if (chatActionModel != null) {

                switch (reasonType) {
                    case 1:
                        leaveOrder(chatActionModel);
                        break;
                    case 2:
                        changeDriver(chatActionModel);
                        break;
                    case 3:
                        if (chatActionModel.getAction().equals("سعر التوصيل مرتفع")) {
                            clientRefuseOffer(offersModel, "yes");

                        } else if (chatActionModel.getAction().equals("لم اعد احتاج الطلب") || chatActionModel.getAction().equals("سبب آخر")) {
                            deleteOrder(chatActionModel);

                        } else {
                            clientRefuseOffer(offersModel, "no");

                        }
                        break;
                }


                closeSheet();
            }
        });
        binding.btnActionCancel.setOnClickListener(v ->
        {
            closeSheet();
        });
        binding.btnDriverCancel.setOnClickListener(v -> {
            driverCancelOffer();
        });
        binding.btnDriverBack.setOnClickListener(v -> {
            driverCancelOffer();
        });
        binding.tvLeaveOrder.setOnClickListener(v -> driverLeaveOrderActions());
        binding.btnCancel.setOnClickListener(v ->
                clientCancelOrder()
        );
        binding.imageMore.setOnClickListener(v -> {
            if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                changeDriverActions();

            } else {
                openDriverActionSheet();
            }

        });
        binding.btnDriverActionCancel.setOnClickListener(v -> closeDriverActionSheet());
        binding.btnDriverAnotherOffer.setOnClickListener(v -> {
            navigateToDriverAddOffer();
        });
        binding.tvShare.setOnClickListener(v -> {
            closeDriverActionSheet();
            share();
        });
        binding.tvEndOrder.setOnClickListener(v -> {
            closeDriverActionSheet();
            endOrder();
        });


        getOrderById(null);

    }


    private void getOrderById(ProgressDialog dialog) {

        Api.getService(Tags.base_url).getSingleOrder(userModel.getUser().getToken(), order_id, userModel.getUser().getId())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        binding.progBarData.setVisibility(View.GONE);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (response.isSuccessful()) {
                            orderModel = response.body().getOrder();
                            binding.setModel(orderModel);
                            updateUi(orderModel);

                        } else {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
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
                            if (dialog != null) {
                                dialog.dismiss();
                            }
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

        if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
            binding.imageMore.setVisibility(View.VISIBLE);

        } else {
            binding.imageMore.setVisibility(View.VISIBLE);
            if (orderModel.getDriver_last_offer() == null) {
                binding.tvReadyDeliverOrder.setVisibility(View.VISIBLE);
                binding.tvMsgLeft.setText(orderModel.getDetails());
                binding.tvMsgLeft.setVisibility(View.VISIBLE);
            } else {
                binding.tvReadyDeliverOrder.setVisibility(View.GONE);
                if (orderModel.getOrder_status().equals("new_order") || orderModel.getOrder_status().equals("pennding") || orderModel.getOrder_status().equals("have_offer")) {
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);

                } else {
                    binding.flDriverOffers.setVisibility(View.GONE);
                    binding.tvMsgLeft.setVisibility(View.GONE);


                }
                binding.tvDriverOfferPrice.setText(String.format(Locale.ENGLISH, "%s %s %s %s", getString(R.string.your_offer_of), orderModel.getDriver_last_offer().getOffer_value(), currency, getString(R.string.sent_to_the_client_please_wait_until_he_accepts_your_offer_thank_you)));
            }

        }


        String status = orderModel.getOrder_status();
        Log.e("status", status);
        switch (status) {
            case "new_order":
                binding.orderStatus.setBackgroundResource(R.drawable.pending_bg);
                binding.orderStatus.setText(getString(R.string.pending));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);


                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);

                    binding.flOffers.setVisibility(View.VISIBLE);
                    binding.llOfferData.setVisibility(View.VISIBLE);
                    binding.llComingOffer.setVisibility(View.GONE);
                } else {
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                }


                break;
            case "pennding":
                binding.orderStatus.setBackgroundResource(R.drawable.pending_bg);
                binding.orderStatus.setText(getString(R.string.pending));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);
                } else {
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                }


                break;

            case "have_offer":
                binding.orderStatus.setBackgroundResource(R.drawable.pending_bg);
                binding.orderStatus.setText(getString(R.string.pending));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);
                    binding.flOffers.setVisibility(View.VISIBLE);
                    binding.llOfferData.setVisibility(View.GONE);
                    binding.llComingOffer.setVisibility(View.VISIBLE);
                    getOffers();
                } else {

                    if (orderModel.getDriver_last_offer() != null && orderModel.getDriver_last_offer().getStatus().equals("refuse")) {
                        binding.flClientRefuseOffer.setVisibility(View.VISIBLE);
                        binding.flOffers.setVisibility(View.GONE);
                        binding.flDriverOffers.setVisibility(View.GONE);

                    } else {
                        binding.flDriverOffers.setVisibility(View.VISIBLE);
                        binding.flClientRefuseOffer.setVisibility(View.GONE);

                    }
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                }


                break;
            case "accept_driver":
                binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
                binding.orderStatus.setText(getString(R.string.order_accepted));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.VISIBLE);
                binding.imageChooser.setVisibility(View.VISIBLE);
                binding.imageSend.setVisibility(View.VISIBLE);
                binding.msgContent.setVisibility(View.VISIBLE);
                binding.consUserData.setVisibility(View.VISIBLE);
                binding.flOffers.setVisibility(View.GONE);
                binding.llOfferData.setVisibility(View.GONE);
                binding.llComingOffer.setVisibility(View.GONE);
                updateUserUi();


                break;
            case "driver_end_rate":
            case "client_end_and_rate":
                binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
                binding.orderStatus.setText(getString(R.string.done));
                binding.btnResend.setVisibility(View.VISIBLE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);

                updateUserUi();

                break;

            case "order_driver_back":
                binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
                binding.orderStatus.setText(getString(R.string.cancel));
                binding.btnResend.setVisibility(View.VISIBLE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.tvCanceled.setVisibility(View.VISIBLE);
                updateUserUi();

                break;
            case "client_cancel":
                binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
                binding.orderStatus.setText(getString(R.string.canceled));
                binding.btnResend.setVisibility(View.VISIBLE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.tvCanceled.setVisibility(View.VISIBLE);

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);

                } else {
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                }
                break;
            case "cancel_for_late":
                binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
                binding.orderStatus.setText(getString(R.string.cancel));
                binding.btnResend.setVisibility(View.VISIBLE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.tvCanceled.setVisibility(View.VISIBLE);



                break;


        }


    }

    private void updateUserUi() {
        binding.tvMsgLeft.setVisibility(View.GONE);
        binding.tvMsgRight.setVisibility(View.GONE);


        if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL + orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).fit().into(binding.userImage);
            binding.tvName.setText(orderModel.getDriver().getName());
            binding.rateBar.setRating(Float.parseFloat(orderModel.getDriver().getRate()));
            binding.flCall.setVisibility(View.VISIBLE);
            binding.llBill.setVisibility(View.GONE);
            double offer_value = 0.0;
            if (orderModel.getOrder_offer() != null) {
                offer_value = Double.parseDouble(orderModel.getOrder_offer().getOffer_value()) + Double.parseDouble(orderModel.getOrder_offer().getTax_value());

            }
            binding.tvOfferValue.setText(String.format(Locale.ENGLISH, "%.2f %s", offer_value, currency));
        } else {

            Picasso.get().load(Uri.parse(Tags.IMAGE_URL + orderModel.getClient().getLogo())).placeholder(R.drawable.user_avatar).fit().into(binding.userImage);
            binding.tvName.setText(orderModel.getClient().getName());
            binding.rateBar.setRating(Float.parseFloat(orderModel.getClient().getRate()));
            binding.flCall.setVisibility(View.GONE);
            binding.llBill.setVisibility(View.VISIBLE);

            double offer_value = 0.0;
            if (orderModel.getDriver_last_offer() != null) {
                offer_value = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value());
            }
            binding.tvOfferValue.setText(String.format(Locale.ENGLISH, "%.2f %s", offer_value, currency));
        }


    }

    private void navigateToDriverAddOffer() {

        Intent intent = new Intent(this, DelegateAddOfferActivity.class);
        double pick_up_distance = getDistance(new LatLng(Double.parseDouble(userModel.getUser().getLatitude()), Double.parseDouble(userModel.getUser().getLongitude())), new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude())));
        double drop_off_distance = getDistance(new LatLng(Double.parseDouble(userModel.getUser().getLatitude()), Double.parseDouble(userModel.getUser().getLongitude())), new LatLng(Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude())));

        double from_loc_to_loc_distance = getDistance(new LatLng(Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude())), new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude())));
        FromToLocationModel fromToLocationModel = new FromToLocationModel(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude()), orderModel.getMarket_address(), pick_up_distance, Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude()), orderModel.getClient_address(), drop_off_distance, from_loc_to_loc_distance, Double.parseDouble(userModel.getUser().getLatitude()), Double.parseDouble(userModel.getUser().getLongitude()));
        intent.putExtra("data", fromToLocationModel);
        intent.putExtra("user_token", userModel.getUser().getToken());
        intent.putExtra("client_id", orderModel.getClient().getId());
        intent.putExtra("order_id", order_id);
        intent.putExtra("driver_id", userModel.getUser().getId());
        startActivityForResult(intent, 100);
    }


    private void driverLeaveOrderActions() {
        reasonType = 1;
        binding.tvActionType.setText(R.string.withdraw_order);
        actionReasonList.clear();
        ChatActionModel chatActionModel1 = new ChatActionModel("موقع المتجر بعيد");
        actionReasonList.add(chatActionModel1);
        ChatActionModel chatActionModel2 = new ChatActionModel("لا أرغب في توصيل الطلب");
        actionReasonList.add(chatActionModel2);
        chatActionAdapter.notifyDataSetChanged();
        openSheet();

    }

    private void changeDriverActions() {
        reasonType = 2;
        binding.tvActionType.setText(R.string.change_driver);
        actionReasonList.clear();
        ChatActionModel chatActionModel1 = new ChatActionModel("المرسول غير مناسب");
        actionReasonList.add(chatActionModel1);
        ChatActionModel chatActionModel2 = new ChatActionModel("المرسول طلب التواصل خارج التطبيق");
        actionReasonList.add(chatActionModel2);
        ChatActionModel chatActionModel3 = new ChatActionModel("المرسول لم يقبل الدفع الالكتروني");
        actionReasonList.add(chatActionModel3);
        ChatActionModel chatActionModel4 = new ChatActionModel("سبب آخر");
        actionReasonList.add(chatActionModel4);
        chatActionAdapter.notifyDataSetChanged();
        openSheet();
    }

    public void deleteOrderActions(OffersModel offersModel) {

        this.offersModel = offersModel;
        reasonType = 3;
        binding.tvActionType.setText(R.string.delete_order);
        actionReasonList.clear();
        ChatActionModel chatActionModel1 = new ChatActionModel("الطلب متأخر والمرسول لا يجيب");
        actionReasonList.add(chatActionModel1);
        ChatActionModel chatActionModel2 = new ChatActionModel("المرسول طلب التواصل خارج التطبيق");
        actionReasonList.add(chatActionModel2);
        ChatActionModel chatActionModel3 = new ChatActionModel("المرسول غير جاد");
        actionReasonList.add(chatActionModel3);
        ChatActionModel chatActionModel4 = new ChatActionModel("المرسول طلب الإلغاء");
        actionReasonList.add(chatActionModel4);
        ChatActionModel chatActionModel5 = new ChatActionModel("المرسول لم يقبل الدفع الالكتروني");
        actionReasonList.add(chatActionModel5);
        ChatActionModel chatActionModel6 = new ChatActionModel("سعر التوصيل مرتفع");
        actionReasonList.add(chatActionModel6);
        ChatActionModel chatActionModel7 = new ChatActionModel("لم اعد احتاج الطلب");
        actionReasonList.add(chatActionModel7);
        ChatActionModel chatActionModel8 = new ChatActionModel("سبب آخر");
        actionReasonList.add(chatActionModel8);
        chatActionAdapter.notifyDataSetChanged();

        openSheet();
    }

    private void resendOrder() {
        isDataChanged = true;
        binding.btnResend.setVisibility(View.GONE);
    }

    public void setReason(ChatActionModel chatActionModel) {
        this.chatActionModel = chatActionModel;

    }

    private void leaveOrder(ChatActionModel chatActionModel) {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).driverLeaveOrder(userModel.getUser().getToken(), orderModel.getClient().getId(), userModel.getUser().getId(), order_id, chatActionModel.getAction())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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

    private void changeDriver(ChatActionModel chatActionModel) {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).changeDriver(userModel.getUser().getToken(), orderModel.getClient().getId(), order_id, chatActionModel.getAction())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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

    private void deleteOrder(ChatActionModel chatActionModel) {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientDeleteOrder(userModel.getUser().getToken(), orderModel.getClient().getId(), order_id, chatActionModel.getAction())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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

    private void clientCancelOrder() {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientCancelOrder(userModel.getUser().getToken(), orderModel.getClient().getId(), order_id)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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

    public void clientAcceptOffer(OffersModel offersModel) {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientAcceptOffer(userModel.getUser().getToken(), orderModel.getClient().getId(), Integer.parseInt(offersModel.getDriver_id()), order_id, offersModel.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                isDataChanged = true;
                                getOrderById(dialog);
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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

    public void clientRefuseOffer(OffersModel offersModel, String type) {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientRefuseOffer(userModel.getUser().getToken(), orderModel.getClient().getId(), Integer.parseInt(offersModel.getDriver_id()), order_id, offersModel.getId(), type)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                isDataChanged = true;
                                getOrderById(dialog);
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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

    public void driverCancelOffer() {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).driverCancelOffer(userModel.getUser().getToken(), userModel.getUser().getId(), order_id)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                isDataChanged = true;
                                setResult(RESULT_OK);
                                finish();
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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


    private void reSendOffer(String offer_value) {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).sendDriverOffer(userModel.getUser().getToken(), orderModel.getClient().getId(), Integer.parseInt(offersModel.getDriver_id()), order_id, offer_value, "make_offer")
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                isDataChanged = true;
                                getOrderById(dialog);
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
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

    private void share() {

    }

    private void endOrder() {


    }

    private void openSheet() {
        binding.flAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.flAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.flAction.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void closeSheet() {

        binding.flAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.flAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flAction.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void openDriverActionSheet() {
        binding.flDriverAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.flDriverAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.flDriverAction.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void closeDriverActionSheet() {

        binding.flDriverAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.flDriverAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flDriverAction.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
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

    private void getOffers() {

        Api.getService(Tags.base_url).getClientOffers(userModel.getUser().getToken(), userModel.getUser().getId(), order_id, 1, "on", 10)
                .enqueue(new Callback<OffersDataModel>() {
                    @Override
                    public void onResponse(Call<OffersDataModel> call, Response<OffersDataModel> response) {
                        if (response.isSuccessful()) {
                            offersModelList.clear();
                            if (response.body() != null) {
                                offer_current_page = response.body().getCurrent_page();
                                binding.llOfferData.setVisibility(View.GONE);
                                updateDataDistance(response.body().getData(), false);


                            }
                        } else {
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<OffersDataModel> call, Throwable t) {
                        try {
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

    private void loadMoreOffer(int page) {
        offersModelList.add(null);
        offersAdapter.notifyItemInserted(offersModelList.size() - 1);
        offer_isLoading = true;

        Api.getService(Tags.base_url).getClientOffers(userModel.getUser().getToken(), userModel.getUser().getId(), order_id, page, "on", 10)
                .enqueue(new Callback<OffersDataModel>() {
                    @Override
                    public void onResponse(Call<OffersDataModel> call, Response<OffersDataModel> response) {
                        offer_isLoading = false;
                        if (offersModelList.get(offersModelList.size() - 1) == null) {
                            offersModelList.remove(offersModelList.size() - 1);
                            offersAdapter.notifyItemRemoved(offersModelList.size() - 1);
                        }
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                if (response.body().getData().size() > 0) {
                                    offer_current_page = response.body().getCurrent_page();
                                    updateDataDistance(response.body().getData(), true);
                                }


                            }
                        } else {
                            offer_isLoading = false;
                            if (offersModelList.get(offersModelList.size() - 1) == null) {
                                offersModelList.remove(offersModelList.size() - 1);
                                offersAdapter.notifyItemRemoved(offersModelList.size() - 1);
                            }
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<OffersDataModel> call, Throwable t) {
                        offer_isLoading = false;
                        if (offersModelList.get(offersModelList.size() - 1) == null) {
                            offersModelList.remove(offersModelList.size() - 1);
                            offersAdapter.notifyItemRemoved(offersModelList.size() - 1);
                        }
                        try {
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

    private void updateDataDistance(List<OffersModel> data, boolean isLoadMore) {
        LatLng place_location = new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude()));
        for (int index = 0; index < data.size(); index++) {
            OffersModel offersModel = data.get(index);
            offersModel.setDistance(calculateDistance(place_location, new LatLng(Double.parseDouble(offersModel.getDriver().getLatitude()), Double.parseDouble(offersModel.getDriver().getLongitude()))));
            offersModel.setOrder_time(orderModel.getOrder_time_arrival());
            data.set(index, offersModel);
        }

        if (!isLoadMore) {
            offersModelList.clear();
            offersModelList.addAll(data);
            offersAdapter.notifyDataSetChanged();
        } else {
            int old_pos = offersModelList.size() - 1;
            offersModelList.addAll(data);
            int new_pos = offersModelList.size();
            offersAdapter.notifyItemRangeInserted(old_pos, new_pos);
        }
    }

    private String calculateDistance(LatLng latLng1, LatLng latLng2) {
        return String.format(Locale.ENGLISH, "%s %s", String.format(Locale.ENGLISH, "%.2f", (SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000)), getString(R.string.km));

    }

    private double getDistance(LatLng latLng1, LatLng latLng2) {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000;
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

        } else if (requestCode == 100 && resultCode == RESULT_OK) {
            orderModel.setOrder_status("have_offer");
            getOrderById(null);
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
        if (isDataChanged) {
            setResult(RESULT_OK);
        }

        finish();
    }


}