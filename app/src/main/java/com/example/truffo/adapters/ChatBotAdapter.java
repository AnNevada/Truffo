package com.example.truffo.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truffo.databinding.ItemContainerReceivedMessageBinding;
import com.example.truffo.databinding.ItemContainerSentMessageBinding;
import com.example.truffo.models.ChatBot;
import com.example.truffo.models.ChatMessage;

import java.util.List;

public class ChatBotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatBot> chatMessages;
    private final String senderId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public ChatBotAdapter(List<ChatBot> chatMessages,String senderID) {
        this.chatMessages = chatMessages;
        this.senderId = senderID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        switch (viewType)
//        {
//            case 0:
//                return new SentMessageViewHolder(
//                        ItemContainerSentMessageBinding.inflate(
//                                LayoutInflater.from(parent.getContext()),
//                                parent,
//                                false
//                        )
//                );
//            case 1:
//                return new ReceivedMessageViewHolder(
//                        ItemContainerReceivedMessageBinding.inflate(
//                                LayoutInflater.from(parent.getContext()),
//                                parent,
//                                false
//                        )
//                );
//        }
//        return null;
        if(viewType == VIEW_TYPE_SENT)
        {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
        else
        {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatBot chatMessage = chatMessages.get(position);
        if(getItemViewType(position) == VIEW_TYPE_SENT)
        {
            ((SentMessageViewHolder) holder).binding.textMessage.setText(chatMessage.getMessage());
        }
        else {
            ((ReceivedMessageViewHolder) holder).binding.textMessage.setText(chatMessage.getMessage());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).getSender().equals(senderId))
        {
            return VIEW_TYPE_SENT;
        }
        else
        {
            return VIEW_TYPE_RECEIVED;
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

        }

    }
}
