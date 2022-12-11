package com.example.truffo.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truffo.R;
import com.example.truffo.databinding.ItemContainerReceivedMessageBinding;
import com.example.truffo.databinding.ItemContainerSentMessageBinding;
import com.example.truffo.models.ChatBot;
import com.example.truffo.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatBotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatBot> chatMessages;
    public ChatBotAdapter(List<ChatBot> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType)
        {
            case 0:
                return new SentMessageViewHolder(
                        ItemContainerSentMessageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),
                                parent,
                                false
                        )
                );
            case 1:
                return new ReceivedMessageViewHolder(
                        ItemContainerReceivedMessageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),
                                parent,
                                false
                        )
                );
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatBot chatMessage = chatMessages.get(position);
        switch (chatMessage.getSender()){
            case "user":
                ((SentMessageViewHolder) holder).binding.textMessage.setText(chatMessage.getMessage());
                break;
            case "bot":
                ((ReceivedMessageViewHolder) holder).binding.textMessage.setText(chatMessage.getMessage());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (chatMessages.get(position).getSender()){
            case "user":
                return  0;
            case "bot":
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding)
        {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
            binding.imageProfile.setImageResource(R.drawable.robot);
        }

    }
}
