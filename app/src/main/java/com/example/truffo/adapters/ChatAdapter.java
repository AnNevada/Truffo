package com.example.truffo.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truffo.databinding.ItemContainerReceivedMessageBinding;
import com.example.truffo.databinding.ItemContainerSentMessageBinding;
import com.example.truffo.models.ChatMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap receiverProfileImage) {
        this.receiverProfileImage = receiverProfileImage;
    }

    //PUT DATA IN THE CONSTRUCTOR
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        //Check the current position
        if(getItemViewType(position) == VIEW_TYPE_SENT)
        {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }
        else
        {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderID.equals(senderId))
        {
            return VIEW_TYPE_SENT;
        }
        else
        {
            return VIEW_TYPE_RECEIVED;
        }
    }

    //SENT MESSAGE VIEWHOLDER
    static class SentMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding)
        {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }
        //SET SENT MESSAGE TEXT AND TIME TO DISPLAY
        void setData(ChatMessage chatMessage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    //RECEIVED MESSAGE VIEWHOLDER
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding)
        {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }
        //SET RECEIVED MESSAGE TEXT AND TIME TO DISPLAY
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDataTime.setText(chatMessage.dateTime);
            if(receiverProfileImage != null)
            {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
