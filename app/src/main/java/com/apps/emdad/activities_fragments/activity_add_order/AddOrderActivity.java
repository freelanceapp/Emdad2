package com.apps.emdad.activities_fragments.activity_add_order;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionSet;

import com.apps.emdad.R;
import com.apps.emdad.adapters.ChatBotAdapter;
import com.apps.emdad.databinding.ActivityAddOrderBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.ChatBotModel;

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
        initView();
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
    }

    private void startChat() {
        chatBotModelList.clear();
        adapter.notifyDataSetChanged();

        ChatBotModel chatBotModel = createInstance(ChatBotAdapter.empty);
        chatBotModelList.add(chatBotModel);
        adapter.notifyItemInserted(chatBotModelList.size()-1);

        chatBotModelList.add(null);
        adapter.notifyItemInserted(chatBotModelList.size()-1);



        new Handler()
                .postDelayed(() -> {
                    chatBotModelList.remove(chatBotModelList.size() - 1);
                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);
                    ChatBotModel chatBotModel1 = createInstance(ChatBotAdapter.greeting);
                    chatBotModelList.add(chatBotModel1);
                    adapter.notifyItemInserted(chatBotModelList.size()-1);

                    binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);

                    new Handler().postDelayed(()->{

                        new Handler().postDelayed(()->{

                            chatBotModelList.add(null);
                            adapter.notifyItemInserted(chatBotModelList.size()-1);
                            binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);


                            new Handler().postDelayed(()->{

                                chatBotModelList.remove(chatBotModelList.size() - 1);
                                adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                                ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.welcome);
                                chatBotModelList.add(chatBotModel2);
                                adapter.notifyItemInserted(chatBotModelList.size()-1);
                                binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);


                                new Handler().postDelayed(()->{

                                    chatBotModelList.add(null);
                                    adapter.notifyItemInserted(chatBotModelList.size()-1);
                                    binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);


                                    new Handler().postDelayed(()->{
                                        chatBotModelList.remove(chatBotModelList.size() - 1);
                                        adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                                        ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.help);
                                        chatBotModelList.add(chatBotModel3);
                                        adapter.notifyItemInserted(chatBotModelList.size()-1);
                                        binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);


                                },1000);




                            },1000);


                        },1000);












                        },1000);


                    },1000);




                }, 3000);
    }

    private ChatBotModel createInstance(int type){
        ChatBotModel chatBotModel = new ChatBotModel();
        chatBotModel.setType(type);
        return chatBotModel;
    }

    public void addOrder_Package(String action, int adapterPosition) {

        ChatBotModel chatBotModel1 = chatBotModelList.get(adapterPosition);
        chatBotModel1.setEnabled(false);
        chatBotModelList.set(adapterPosition,chatBotModel1);
        adapter.notifyItemChanged(adapterPosition);


        ChatBotModel chatBotModel = createInstance(ChatBotAdapter.new_order);
        chatBotModel.setText(action);
        chatBotModelList.add(chatBotModel);
        adapter.notifyItemInserted(chatBotModelList.size()-1);
        binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);


        new Handler().postDelayed(()->{
            chatBotModelList.add(null);
            adapter.notifyItemInserted(chatBotModelList.size()-1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);

            new Handler().postDelayed(()->{
                chatBotModelList.remove(chatBotModelList.size() - 1);
                adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                ChatBotModel chatBotModel2;
                if (action.equals(getString(R.string.new_order))){

                    chatBotModel2 = createInstance(ChatBotAdapter.store);;
                }else {
                    chatBotModel2 = createInstance(ChatBotAdapter.drop_off_location);;

                }


                chatBotModelList.add(chatBotModel2);
                adapter.notifyItemInserted(chatBotModelList.size()-1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size()-1);


            },1000);
        },1000);
    }
}
