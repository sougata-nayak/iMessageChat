package com.example.imessagechat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.imessagechat.Adapters.MessageAdapter;
import com.example.imessagechat.Models.Messages;
import com.example.imessagechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageSenderId;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private DatabaseReference usersRef;

    private Toolbar ChatToolBar;

    private EditText messageInputText;
    private ImageButton sendMessageButton, sendFilesButton;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;


    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    String saveCurrentTime, saveCurrentDate, checker = "";
    private Uri fileUri;

    private ProgressDialog loadingBar;

    List<String> keyList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        Initialize();

        userName.setText(messageReceiverName);

        DisplayLastSeen();

        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());


        GetMessageReceiverInfoForToolbar();


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendMessage();
            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{

                        "Images",
                        "PDF files",
                        "Documents"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatsActivity.this);
                builder.setTitle("Select File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){

                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 0);

                        }
                        if(which == 1){

                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF file"), 0);
                        }
                        if(which == 2){

                            checker = "doc";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select docs file"), 0);
                        }
                    }
                });
                builder.show();

            }
        });


        UpdateMessageList();
    }


    private void Initialize() {

        messageReceiverId = getIntent().getStringExtra("visit_user_id");
        messageReceiverName = getIntent().getStringExtra("visit_user_name");

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        ChatToolBar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);


        messageInputText = findViewById(R.id.input_private_message);
        sendMessageButton = findViewById(R.id.send_message_btn);
        sendFilesButton = findViewById(R.id.send_files_btn);

        loadingBar = new ProgressDialog(this);


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        rootRef = FirebaseDatabase.getInstance().getReference();


        messageAdapter = new MessageAdapter(messagesList);


        userMessagesList = (RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }


    private void UpdateMessageList(){

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        keyList.add(dataSnapshot.getKey());

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


                        Messages messages = dataSnapshot.getValue(Messages.class);

                        int index = keyList.indexOf(dataSnapshot.getKey());

                        messagesList.remove(index);

                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void GetMessageReceiverInfoForToolbar(){

        usersRef.child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("image")){
                        String profileImage = dataSnapshot.child("image").getValue().toString();

                        Glide.with(getApplicationContext()).load(profileImage).placeholder(R.drawable.default_profile_picture)
                                .into(userImage);
                    }
                    else{

                        userImage.setImageResource(R.drawable.default_profile_picture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == 0 && data!=null && data.getData()!=null) {

            fileUri = data.getData();

            if(!checker.equals("image")){

                loadingBar.setTitle("Sending File");
                loadingBar.setMessage("Please wait till we send your image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                        .push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+"." + checker);


                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String myUrl = uri.toString();

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message", myUrl);
                                messageImageBody.put("name", fileUri.getLastPathSegment());
                                messageImageBody.put("type", checker);
                                messageImageBody.put("from", messageSenderId);
                                messageImageBody.put("to", messageReceiverId);
                                messageImageBody.put("messageId", messagePushId);
                                messageImageBody.put("time", saveCurrentTime);
                                messageImageBody.put("date", saveCurrentDate);


                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageImageBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageImageBody);


                                rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.00*taskSnapshot.getBytesTransferred())/(taskSnapshot.getTotalByteCount());
                        loadingBar.setMessage((int)p + "% uploaded");
                    }
                });

            }

            else if(checker.equals("image")){

                loadingBar.setTitle("Sending Image");
                loadingBar.setMessage("Please wait till we send your image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                        .push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+".jpg");

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String myUrl = uri.toString();

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message", myUrl);
                                messageImageBody.put("name", fileUri.getLastPathSegment());
                                messageImageBody.put("type", checker);
                                messageImageBody.put("from", messageSenderId);
                                messageImageBody.put("to", messageReceiverId);
                                messageImageBody.put("messageId", messagePushId);
                                messageImageBody.put("time", saveCurrentTime);
                                messageImageBody.put("date", saveCurrentDate);


                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageImageBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageImageBody);


                                rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.00*taskSnapshot.getBytesTransferred())/(taskSnapshot.getTotalByteCount());
                        loadingBar.setMessage((int)p + "% uploaded");
                    }
                });

            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
    }



    private void DisplayLastSeen(){

        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("userState").hasChild("state")){

                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("online")){

                        userLastSeen.setText("Online");

                    }
                    else if(state.equals("offline")){

                        userLastSeen.setText("Last Seen: " + date + " " + time);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void SendMessage() {

        String message = messageInputText.getText().toString().trim();

        if(message.isEmpty()){
            messageInputText.setError("Enter a message first");
        }
        else{

            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                    .push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageReceiverId);
            messageTextBody.put("messageId", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);


            rootRef.updateChildren(messageBodyDetails);

            messageInputText.setText("");
        }
    }
}