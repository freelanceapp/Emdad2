package com.apps.emdad.activities_fragments.activity_filter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.apps.emdad.R;
import com.apps.emdad.adapters.KeywordsAdapter;
import com.apps.emdad.databinding.ActivityFilterBinding;
import com.apps.emdad.interfaces.Listeners;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.FilterModel;
import com.apps.emdad.models.KeywordModel;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class FilterActivity extends AppCompatActivity implements Listeners.BackListener {
    private ActivityFilterBinding binding;
    private String lang;
    private boolean isChange = false;
    private FilterModel filterModel;
    private KeywordsAdapter adapter;
    private List<KeywordModel> keywordList;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_filter);
        initView();
    }

    private void initView() {
        keywordList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setListener(this);
        String dist = String.format(Locale.ENGLISH,"%s %s",binding.seekBarDistance.getProgressFloat(),getString(R.string.km));
        binding.tvDistance.setText(dist);
        binding.tvRate.setText(String.valueOf(binding.seekBarRate.getProgressFloat()));
        filterModel = new FilterModel((int) binding.seekBarDistance.getProgressFloat(),binding.seekBarRate.getProgressFloat(),"");
        binding.recView.setLayoutManager(new GridLayoutManager(this,2));
        addKeywords();
        adapter = new KeywordsAdapter(keywordList,this);
        binding.recView.setAdapter(adapter);
        binding.seekBarDistance.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                String dist = String.format(Locale.ENGLISH,"%s %s",seekParams.progressFloat,getString(R.string.km));
                binding.tvDistance.setText(dist);
                binding.btnApply.setBackgroundResource(R.color.colorPrimary);
                isChange = true;
                filterModel.setDistance((int) seekParams.progressFloat);

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        binding.seekBarRate.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                binding.tvRate.setText(String.valueOf(seekParams.progressFloat));
                binding.btnApply.setBackgroundResource(R.color.colorPrimary);
                isChange = true;
                filterModel.setRate((int) seekParams.progressFloat);

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        binding.btnApply.setOnClickListener(v -> {
            if (isChange){

                Intent intent = getIntent();
                intent.putExtra("data",filterModel);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }


    private void addKeywords(){
        keywordList.add(new KeywordModel("restaurant",getString(R.string.restaurants)));
        keywordList.add(new KeywordModel("store",getString(R.string.stores)));
        keywordList.add(new KeywordModel("supermarket",getString(R.string.supermarket)));
        keywordList.add(new KeywordModel("bakery",getString(R.string.bakery)));
        keywordList.add(new KeywordModel("cafe",getString(R.string.cafe)));
        keywordList.add(new KeywordModel("florist",getString(R.string.florist)));
        keywordList.add(new KeywordModel("library",getString(R.string.library)));


    }

    public void setItemData(KeywordModel keywordModel) {
        isChange = true;
        binding.btnApply.setBackgroundResource(R.color.colorPrimary);
        filterModel.setKeyword(keywordModel.getKeyword());


    }

    @Override
    public void back() {
        super.onBackPressed();
    }


}