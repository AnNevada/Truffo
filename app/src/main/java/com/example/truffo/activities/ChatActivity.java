package com.example.truffo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.truffo.R;
import com.example.truffo.databinding.ActivityChatBinding;
import com.example.truffo.models.User;
import com.example.truffo.utils.Constants;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setLinsteners();
        loadReceiverDetails();
    }

    //THIS FUNCTION USES TO RECEIVE USER DATA WHEN WE CLICK THE USER ICON AT THE USERSACTIVITY
    private void loadReceiverDetails()
    {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

    }

    private void setLinsteners()
    {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
}