package com.example.truffo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.truffo.adapters.ChatAdapter;
import com.example.truffo.databinding.ActivityChatBinding;
import com.example.truffo.models.ChatMessage;
import com.example.truffo.models.User;
import com.example.truffo.network.ApiClient;
import com.example.truffo.network.ApiService;
import com.example.truffo.utils.Constants;
import com.example.truffo.utils.PrefsManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PrefsManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;

    //Vars for google map
    private static final String TAG = "MapTest";
    public static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PrefsManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);

        binding.textTyping.setText(receiverUser.name + " is typing...");

        binding.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    updateTypingStatus(1);
                } else {
                    updateTypingStatus(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // UPDATE TYPING STATUS WHEN RECEIVE BROADCAST
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                int typingStatus = intent.getIntExtra(Constants.KEY_IS_TYPING, -1);
                                if(typingStatus == 0) {
                                    binding.textTyping.setVisibility(View.GONE);
                                } else {
                                    binding.textTyping.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        , new IntentFilter(Constants.ACTION_TYPING)
                );

        database = FirebaseFirestore.getInstance();
    }

    // UPDATE TYPING STATUS OF SENDER
    private void updateTypingStatus(int status) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverUser.token);

            JSONObject typingStatus = new JSONObject();
            typingStatus.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TYPING_STATUS);
            typingStatus.put(Constants.KEY_IS_TYPING, status);
            typingStatus.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            typingStatus.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            typingStatus.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));

            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_DATA, typingStatus);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            String messageBody = body.toString();

            ApiClient.getClient().create(ApiService.class).sendMessage(
                    Constants.getRemoteMsgHeaders(),
                    messageBody
            ).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseObj = new JSONObject(Objects.requireNonNull(response.body()));
                            if (responseObj.getInt("failure") != 0) {
                                Log.d("TypingStatus", "onResponse: " + responseObj.toString());
                            }
                        } catch (Exception exception) {
                            Log.d(TAG, "onResponse: failed");
                        }
                    } else {
                        Log.d(TAG, "onResponse: success");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.d(TAG, "onFailure: ");
                }
            });
        }
        catch(Exception exception) {
            Log.d(TAG, "updateTypingStatus: ");
        }
    }

    // SEND MESSAGE TO FIRESTORE DATABASE
    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)); //PUT SENDER ID
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id); //PUT RECEIVER ID
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString()); //PUT MESSAGE
        message.put(Constants.KEY_TIMESTAMP, new Date()); //PUT DATE
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message); //COLLECT MESSAGE OBJECT

        // UPDATE CONVERSATION IF ALREADY EXISTS, ELSE CREATE NEW CONVERSATION
        if(conversationId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        }
        else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }

        // SENDER SENDS NOTIFICATION IF RECEIVER IS NOT ONLINE
        if(!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_SEND_NOTIFICATION);
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            }
            catch(Exception exception) {
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    try {
                        if(response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                }
                else {
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    // LISTEN TO AVAILABILITY OF THE RECEIVER
    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if(error != null) return;

                    if(value != null) {
                        if(value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                        if(receiverUser.image == null) {
                            receiverUser.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                            chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                        }
                    }

                    if(isReceiverAvailable) {
                        binding.textAvailability.setVisibility(View.VISIBLE);
                    }
                    else {
                        binding.textAvailability.setVisibility(View.GONE);
                    }
                });
    }

    // REGISTER LISTENER FOR MESSAGES IN FIRESTORE DATABASE
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    // EVENT LISTENER AFTER LOAD MESSAGES IN FIRESTORE DATABASE
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) return;

        if(value != null) {
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

            if(count == 0) {
                chatAdapter.notifyDataSetChanged();
            }
            else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

        // LOAD CONVERSATIONS AFTER MESSAGES ARE LOADED
        if(conversationId == null) {
            checkForConversation();
        }
    };

    //THIS FUNCTION USES TO RECEIVE USER DATA WHEN WE CLICK THE USER ICON AT THE USERSACTIVITY
    private void loadReceiverDetails()
    {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

    }

    // CONVERT BASE64 STRING TO IMAGE
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if(encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.layoutLocation.setOnClickListener(v -> displayLocation());
    }



    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> {
                    conversationId = documentReference.getId();
                });
    }

    private void updateConversation(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    // LOAD CONVERSATIONS IN WHICH USER PARTICIPATE FROM FIRESTORE DATABASE
    private void checkForConversation() {
        if(chatMessages.size() != 0) {
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id);
            checkForConversationRemotely(receiverUser.id, preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }

    // LOAD CONVERSATIONS FROM DATABASE WHICH MATCHES THE USER ID AND RECEIVER ID
    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    // EVENT LISTENER AFTER LOAD CONVERSATIONS IN WHICH USER PARTICIPATE FROM FIRESTORE DATABASE
    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        updateTypingStatus(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    //ALL METHODS FOR GOOGLE MAP
    //CHECK SERIVCE STATUS (chatactivity)
    public boolean isServiceOK(){
        Log.d(TAG, "isServiceOK: checking ggservice version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ChatActivity.this);
        if(available == ConnectionResult.SUCCESS){
            //everything is fine
            Log.d(TAG, "isServiceOK: is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occur
            Log.d(TAG, "isServiceOK: error");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(ChatActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void displayLocation() {
        Toast.makeText(this, "Location sent", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ChatActivity.this, MapActivity.class);
        startActivity(intent);
    }
}