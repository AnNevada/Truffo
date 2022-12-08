package com.example.truffo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.truffo.R;
import com.example.truffo.adapters.ChatBotAdapter;
import com.example.truffo.databinding.ActivityChatBotBinding;
import com.example.truffo.listeners.RetrofitAPI;
import com.example.truffo.models.BotMessage;
import com.example.truffo.models.ChatBot;
import com.example.truffo.models.ChatMessage;
import com.example.truffo.utils.Constants;
import com.example.truffo.utils.PrefsManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends AppCompatActivity {
    private ActivityChatBotBinding binding;
    private final String BOT_KEY = "bot";
    private final String USER_KEY = "user";
    private List<ChatBot> chatMessageList;
    private ChatBotAdapter chatBotAdapter;
    private PrefsManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        init();
    }
    private void init()
    {
        preferenceManager = new PrefsManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatMessageList.add(new ChatBot("Dsdsd","BOT"));
        chatBotAdapter = new ChatBotAdapter(chatMessageList,preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatBotAdapter);
    }

    private void setListeners()
    {
        binding.layoutSend.setOnClickListener(v -> {
            Log.d("DEBUG", "getRespond: sdsadas");
            if(binding.inputMessage.getText().toString().isEmpty()){
                Toast.makeText(ChatBotActivity.this,"Please enter your message",Toast.LENGTH_SHORT).show();
            }
            else {
                getRespond(binding.inputMessage.getText().toString());
            }
        });
    }

    private void getRespond(String message) {
        chatMessageList.add(new ChatBot(message,Constants.KEY_USER_ID));
        chatBotAdapter.notifyDataSetChanged();
        String url = "http://api.brainshop.ai/get?bid=170989&key=AoljClPCFIMHE1hm&uid=[uid]&msg="+message;
        String BASE_URL = "http://api.brainshop.ai";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<BotMessage> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<BotMessage>() {
            @Override
            public void onResponse(Call<BotMessage> call, Response<BotMessage> response) {
                if(response.isSuccessful()){
                   BotMessage botMessage = response.body();
                    chatMessageList.add(new ChatBot(botMessage.getCnt(),BOT_KEY));
                    chatBotAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<BotMessage> call, Throwable t) {
                chatMessageList.add(new ChatBot("Please revert your question",BOT_KEY));
                chatBotAdapter.notifyDataSetChanged();
           }
        });
    }



}