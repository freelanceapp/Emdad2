package com.apps.emdad.activities_fragments.activity_shop_products;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_add_order_text.AddOrderTextActivity;
import com.apps.emdad.activities_fragments.activity_login.LoginActivity;
import com.apps.emdad.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.emdad.adapters.CustomHoursAdapter;
import com.apps.emdad.adapters.HoursAdapter;
import com.apps.emdad.adapters.shop_products_adapters.ProductAdapter;
import com.apps.emdad.adapters.shop_products_adapters.ShopExpandGroup;
import com.apps.emdad.databinding.ActivityShopProductsBinding;
import com.apps.emdad.databinding.DialogHoursBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.CustomPlaceDataModel;
import com.apps.emdad.models.CustomPlaceModel;
import com.apps.emdad.models.CustomShopDataModel;
import com.apps.emdad.models.HourModel;
import com.apps.emdad.models.PlaceDetailsModel;
import com.apps.emdad.models.ProductModel;
import com.apps.emdad.models.ShopDepartmentDataModel;
import com.apps.emdad.models.ShopDepartments;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.tags.Tags;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopProductActivity extends AppCompatActivity {
    private ActivityShopProductsBinding binding;
    private CustomShopDataModel placeModel;
    private String lang;
    private List<CustomPlaceModel.Days> daysModelList;
    private List<HourModel> hourModelList;
    private ProductAdapter productAdapter;
    private boolean canSend = false;
    private UserModel userModel;
    private Preferences preferences;
    private String currency;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_products);
        getDataFromIntent();
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        userModel = preferences.getUserData(this);

    }

    private void getDataFromIntent() {

        Intent intent = getIntent();
        placeModel = (CustomShopDataModel) intent.getSerializableExtra("data");

    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        daysModelList = new ArrayList<>();
        hourModelList = new ArrayList<>();

         currency = getString(R.string.sar);
        if (userModel != null) {
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }
        binding.setCurrency(currency);

        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.setLang(lang);

        binding.appBar.addOnOffsetChangedListener((AppBarLayout.BaseOnOffsetChangedListener) (appBarLayout, verticalOffset) -> {
            int total = appBarLayout.getTotalScrollRange() + verticalOffset;
            if (total == 0) {
                if (placeModel.getShopDepartmentsList()!=null&&placeModel.getShopDepartmentsList().size()>0){
                    binding.tab.setVisibility(View.VISIBLE);
                    binding.tvMenu.setVisibility(View.INVISIBLE);

                }else {
                    binding.tab.setVisibility(View.INVISIBLE);
                    binding.tvMenu.setVisibility(View.VISIBLE);


                }
            } else {
                binding.tvMenu.setVisibility(View.VISIBLE);
                binding.tab.setVisibility(View.INVISIBLE);


            }
        });

        binding.flBack.setOnClickListener(v -> finish());


        binding.tvShow.setOnClickListener(v -> {
            if (placeModel.getPlace_type().equals("custom")) {
                if (daysModelList.size() > 0) {
                    createDialogAlertDays();
                } else {
                    Toast.makeText(this, R.string.work_hour_not_aval, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (hourModelList.size() > 0) {
                    createDialogAlertHours();
                } else {
                    Toast.makeText(this, R.string.work_hour_not_aval, Toast.LENGTH_SHORT).show();
                }
            }

        });

        binding.imageShare.setOnClickListener(v -> {
            String url = Tags.base_url + placeModel.getShop_id();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, url);
            startActivity(Intent.createChooser(intent, "Share"));
        });

        binding.flChooseFromMenu.setOnClickListener(v -> {
            if (canSend) {
                Intent intent = new Intent(this, AddOrderTextActivity.class);
                intent.putExtra("data", placeModel);
                startActivityForResult(intent, 100);

            }
        });

        binding.consReview.setOnClickListener(v -> {
            if (userModel == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("from", false);
                startActivity(intent);
            } else {

            }
        });

        binding.flBack.setOnClickListener(v -> {
            super.onBackPressed();
        });

        if (placeModel.getPlace_type().equals("custom")) {
            updateUI();
        }
        else {
            getPlaceDetails();
        }

        getDepartments();
    }

    private void getDepartments(){
        Log.e("id",placeModel.getShop_id());
        Api.getService(Tags.base_url).getShopDepartmentProduct(placeModel.getShop_id()).enqueue(new Callback<ShopDepartmentDataModel>() {
            @Override
            public void onResponse(Call<ShopDepartmentDataModel> call, Response<ShopDepartmentDataModel> response) {
                binding.progBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (response.body()!=null&&response.body().getData()!=null&&response.body().getData().size()>0){
                        updateDepartmentsUi(response.body().getData());

                    }
                } else {

                    try {
                        Log.e("error_code", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<ShopDepartmentDataModel> call, Throwable t) {



                try {
                    binding.progBar.setVisibility(View.GONE);


                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage() + "__");

                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(ShopProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                        }else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){}
                        else {
                            Toast.makeText(ShopProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }


                } catch (Exception e) {

                }
            }
        });

    }

    private void updateDepartmentsUi(List<ShopDepartments> data) {
        placeModel.setShopDepartmentsList(data);
        binding.setModel(placeModel);
        ShopExpandGroup shopExpandGroup;
        List<ShopExpandGroup> shopExpandGroupList = new ArrayList<>();

        for (ShopDepartments departments: data){

            TabLayout.Tab tab = binding.tab.newTab();
            tab.setTag(departments);
            String title ="";
            if (lang.equals("ar")){
                title = departments.getTitle_ar();


            }else {
                title = departments.getTitle_en();


            }
            tab.setText(title);
            binding.tab.addTab(tab);

            shopExpandGroup = new ShopExpandGroup(departments,departments.getProducts_list(),title);
            shopExpandGroupList.add(shopExpandGroup);
            productAdapter = new ProductAdapter(shopExpandGroupList,this,currency);
            binding.recView.setLayoutManager(new LinearLayoutManager(this));
            binding.recView.setAdapter(productAdapter);
            if (shopExpandGroupList.size()>0){
                productAdapter.toggleGroup(0);

            }

        }




    }


    private void getPlaceDetails()
    {

        String fields = "opening_hours,photos,reviews";

        Api.getService("https://maps.googleapis.com/maps/api/")
                .getPlaceDetails(placeModel.getShop_id(), fields, lang, getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDetailsModel>() {
                    @Override
                    public void onResponse(Call<PlaceDetailsModel> call, Response<PlaceDetailsModel> response) {
                        if (response.isSuccessful() && response.body() != null&&response.body().getResult()!=null&&response.body().getResult().getOpening_hours()!=null&&response.body().getResult().getOpening_hours().getWeekday_text()!=null) {
                            placeModel.setHourModelList(getHours(response.body().getResult().getOpening_hours()));
                            updateUI();
                        } else {

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceDetailsModel> call, Throwable t) {
                        try {

                            Log.e("Error", t.getMessage());
                            Toast.makeText(ShopProductActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }
    private List<HourModel> getHours(PlaceDetailsModel.Opening_Hours opening_hours)
    {
        List<HourModel> list = new ArrayList<>();

        for (String time: opening_hours.getWeekday_text()){

            String day = time.split(":", 2)[0].trim();
            String t = time.split(":",2)[1].trim();
            HourModel hourModel = new HourModel(day,t);
            list.add(hourModel);




        }

        return list;
    }
    private void updateUI()
    {
        if (placeModel.getDays() != null && placeModel.getDays().size() > 0) {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.gray11));
            binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.gray11));
            daysModelList.clear();
            daysModelList.addAll(placeModel.getDays());
            binding.tvHours.setText(String.format("%s%s%s", daysModelList.get(0).getFrom_time(), "-", daysModelList.get(0).getTo_time()));



        } else {
            if (placeModel.getHourModelList()!=null&&placeModel.getHourModelList().size()>0){
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.gray11));
                binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.gray11));
                hourModelList.clear();
                hourModelList.addAll(placeModel.getHourModelList());
                binding.tvHours.setText(hourModelList.get(0).getTime());

            }else {
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_rose));
                binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.color_rose));

            }









        }


        binding.setModel(placeModel);
        binding.imageShare.setVisibility(View.VISIBLE);
        binding.llContainer.setVisibility(View.VISIBLE);


    }

    private void createDialogAlertHours()
    {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();
        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        HoursAdapter adapter = new HoursAdapter(hourModelList, this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private void createDialogAlertDays()
    {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();
        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        CustomHoursAdapter adapter = new CustomHoursAdapter(daysModelList, this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }




}