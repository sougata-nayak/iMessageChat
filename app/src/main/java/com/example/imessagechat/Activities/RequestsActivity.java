package com.example.imessagechat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.imessagechat.Models.Contacts;
import com.example.imessagechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsActivity extends AppCompatActivity {


    private RecyclerView myRequestLists;

    private DatabaseReference ChatsRequestsRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;

    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        myRequestLists = (RecyclerView) findViewById(R.id.chat_request_lists);
        myRequestLists.setLayoutManager(new LinearLayoutManager(this));

        ChatsRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRequestsRef.child(currentUserId), Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_decline_button).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                DatabaseReference get_ref_type = getRef(position).child("request_type").getRef();

                get_ref_type.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            final String request_type = dataSnapshot.getValue().toString();

                            if(request_type.equals("received")){

                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild("image")){

                                            final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                            Toast.makeText(RequestsActivity.this, "requests", Toast.LENGTH_SHORT).show();
                                            Glide.with(holder.userImage.getContext()).load(requestUserImage).placeholder(R.drawable.default_profile_picture).into(holder.userImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText(requestUserStatus);


                                        holder.acceptRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                contactsRef.child(currentUserId).child(list_user_id).child("Contacts")
                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){
                                                            contactsRef.child(list_user_id).child(currentUserId).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){
                                                                        ChatsRequestsRef.child(currentUserId).child(list_user_id).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if(task.isSuccessful()){

                                                                                            ChatsRequestsRef.child(list_user_id).child(currentUserId).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                            if(task.isSuccessful()){
                                                                                                                Toast.makeText(RequestsActivity.this, "New contact saved", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        holder.declineRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                ChatsRequestsRef.child(currentUserId).child(list_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()){

                                                                    ChatsRequestsRef.child(list_user_id).child(currentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(RequestsActivity.this, "Contact deleted", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if(request_type.equals("sent")){

                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_button);
                                request_sent_btn.setText("Request Sent");

                                holder.itemView.findViewById(R.id.request_decline_button).setVisibility(View.INVISIBLE);



                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild("image")){

                                            final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                            Toast.makeText(RequestsActivity.this, "requests", Toast.LENGTH_SHORT).show();
                                            Glide.with(holder.userImage.getContext()).load(requestUserImage).placeholder(R.drawable.default_profile_picture).into(holder.userImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText(requestUserStatus);


                                        holder.acceptRequest.setText("Cancel Chat Request");

                                        holder.acceptRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                ChatsRequestsRef.child(currentUserId).child(list_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()){

                                                                    ChatsRequestsRef.child(list_user_id).child(currentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(RequestsActivity.this, "Chat request Cancelled", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });



                            }
                        }
                        else{

                            Toast.makeText(RequestsActivity.this, "No pending requests", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }



            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);

                RequestViewHolder holder = new RequestViewHolder(view);
                return holder;
            }
        };

        myRequestLists.setAdapter(adapter);
        adapter.startListening();
    }





    public static class RequestViewHolder extends RecyclerView.ViewHolder{


        TextView userName, userStatus;
        CircleImageView userImage;
        Button acceptRequest, declineRequest;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            userImage = itemView.findViewById(R.id.users_profile_image);

            acceptRequest = itemView.findViewById(R.id.request_accept_button);
            declineRequest = itemView.findViewById(R.id.request_decline_button);
        }
    }

}
