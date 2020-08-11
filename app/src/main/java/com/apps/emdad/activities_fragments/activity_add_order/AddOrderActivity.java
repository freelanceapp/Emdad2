package com.apps.emdad.activities_fragments.activity_add_order;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_filter.FilterActivity;
import com.apps.emdad.activities_fragments.activity_shops.ShopsActivity;
import com.apps.emdad.adapters.ChatBotAdapter;
import com.apps.emdad.databinding.ActivityAddOrderBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.ChatBotModel;
import com.apps.emdad.models.NearbyModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class AddOrderActivity extends AppCompatActivity {
    private ActivityAddOrderBinding binding;
    private ChatBotAdapter adapter;
    private List<ChatBotModel> chatBotModelList;
    private double user_lat;
    private double user_lng;
    private int shopListPos = 0;
    private int write_order_details_pos = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("Lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new TransitionSet());
            getWindow().setExitTransition(new TransitionSet());

        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_order);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        user_lat = intent.getDoubleExtra("lat", 0.0);
        user_lng = intent.getDoubleExtra("lng", 0.0);
    }

    private void initView() {
        chatBotModelList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
        String time = dateFormat.format(new Date(calendar.getTimeInMillis()));
        binding.tvTime.setText(time);
        String am_pm = time.substring(time.length() - 2);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatBotAdapter(this, chatBotModelList, "Emad", "", am_pm.toLowerCase());
        binding.recView.setAdapter(adapter);
        startChat();


        binding.cardRestart.setOnClickListener(v -> startChat());
        binding.close.setOnClickListener(v -> super.onBackPressed());
        binding.imageCloseSheet.setOnClickListener(v -> onBackPressed());

    }

    private void startChat() {
        chatBotModelList.clear();
        adapter.notifyDataSetChanged();

        ChatBotModel chatBotModel = createInstance(ChatBotAdapter.empty);
        chatBotModelList.add(chatBotModel);
        adapter.notifyItemInserted(chatBotModelList.size() - 1);

        chatBotModelList.add(null);
        adapter.notifyItemInserted(chatBotModelList.size() - 1);


        new Handler()
                .postDelayed(() -> {
                    chatBotModelList.remove(chatBotModelList.size() - 1);
                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);
                    ChatBotModel chatBotModel1 = createInstance(ChatBotAdapter.greeting);
                    chatBotModelList.add(chatBotModel1);
                    adapter.notifyItemInserted(chatBotModelList.size() - 1);

                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                    new Handler().postDelayed(() -> {

                        new Handler().postDelayed(() -> {

                            chatBotModelList.add(null);
                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                            new Handler().postDelayed(() -> {

                                chatBotModelList.remove(chatBotModelList.size() - 1);
                                adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                                ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.welcome);
                                chatBotModelList.add(chatBotModel2);
                                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                new Handler().postDelayed(() -> {

                                    chatBotModelList.add(null);
                                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                    new Handler().postDelayed(() -> {
                                        chatBotModelList.remove(chatBotModelList.size() - 1);
                                        adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                                        ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.help);
                                        chatBotModelList.add(chatBotModel3);
                                        adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                    }, 1000);


                                }, 1000);


                            }, 1000);


                        }, 1000);


                    }, 1000);


                }, 3000);
    }

    private ChatBotModel createInstance(int type) {
        ChatBotModel chatBotModel = new ChatBotModel();
        chatBotModel.setType(type);
        return chatBotModel;
    }

    public void addOrder_Package(String action, int adapterPosition) {
        binding.cardRestart.setVisibility(View.VISIBLE);
        ChatBotModel chatBotModel1 = chatBotModelList.get(adapterPosition);
        chatBotModel1.setEnabled(false);
        chatBotModelList.set(adapterPosition, chatBotModel1);
        adapter.notifyItemChanged(adapterPosition);


        ChatBotModel chatBotModel = createInstance(ChatBotAdapter.new_order);
        chatBotModel.setText(action);
        chatBotModelList.add(chatBotModel);
        adapter.notifyItemInserted(chatBotModelList.size() - 1);
        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


        new Handler().postDelayed(() -> {
            chatBotModelList.add(null);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

            new Handler().postDelayed(() -> {
                chatBotModelList.remove(chatBotModelList.size() - 1);
                adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                ChatBotModel chatBotModel2;
                if (action.equals(getString(R.string.new_order))) {

                    chatBotModel2 = createInstance(ChatBotAdapter.store);
                    ;
                } else {
                    chatBotModel2 = createInstance(ChatBotAdapter.drop_off_location);
                    ;

                }

                chatBotModelList.add(chatBotModel2);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


            }, 1000);
        }, 1000);
    }

    public void openShops_Maps(int adapterPosition, String action) {
        shopListPos = adapterPosition;

        if (action.equals(getString(R.string.shop_list))) {
            Intent intent = new Intent(this, ShopsActivity.class);
            intent.putExtra("lat", user_lat);
            intent.putExtra("lng", user_lng);
            startActivityForResult(intent, 100);
        } else {

        }


    }

    private void updateSelectedShopListUi() {
        ChatBotModel chatBotModel1 = chatBotModelList.get(shopListPos);
        chatBotModel1.setEnabled(false);
        chatBotModelList.set(shopListPos, chatBotModel1);
        adapter.notifyItemChanged(shopListPos);


        ChatBotModel chatBotModel = createInstance(ChatBotAdapter.store_details);
        chatBotModel.setText(getString(R.string.shop_list));
        chatBotModelList.add(chatBotModel);
        adapter.notifyItemInserted(chatBotModelList.size() - 1);
        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            NearbyModel.Result result = (NearbyModel.Result) data.getSerializableExtra("data");
            updateSelectedShopUI(result);

        }
    }

    private void updateSelectedShopUI(NearbyModel.Result result) {

        updateSelectedShopListUi();
        ChatBotModel chatBotModel = createInstance(ChatBotAdapter.place_details);
        chatBotModel.setText(result.getName());
        chatBotModel.setFrom_lat(result.getGeometry().getLocation().getLat());
        chatBotModel.setFrom_lng(result.getGeometry().getLocation().getLng());
        chatBotModel.setDistance(result.getDistance());
        chatBotModel.setFrom_address(result.getVicinity());
        chatBotModel.setRate(result.getRating());

        if (result.getPhotos() != null && result.getPhotos().size() > 0) {
            chatBotModel.setImage_url(result.getPhotos().get(0).getPhoto_reference());
        } else {
            chatBotModel.setImage_url(result.getIcon());

        }
        chatBotModelList.add(chatBotModel);
        adapter.notifyItemInserted(chatBotModelList.size() - 1);
        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

        chatBotModelList.add(null);
        adapter.notifyItemInserted(chatBotModelList.size() - 1);
        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

        new Handler()
                .postDelayed(() -> {
                    chatBotModelList.remove(chatBotModelList.size() - 1);
                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);


                    ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.needs);
                    ;
                    chatBotModelList.add(chatBotModel2);
                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                }, 1000);


    }


    public void writeOrderDetails(int adapterPosition) {
        write_order_details_pos = adapterPosition;
        openSheet();
    }

    private void openSheet() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.root.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.root.setVisibility(View.VISIBLE);
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
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.slide_down);
        binding.root.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.root.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.root.getVisibility()==View.VISIBLE){
            closeSheet();
        }else {
            super.onBackPressed();

        }

    }


}
