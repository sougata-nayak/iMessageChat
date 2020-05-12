package com.example.imessagechat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imessagechat.Activities.ImageViewerActivity;
import com.example.imessagechat.Models.Messages;
import com.example.imessagechat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private FirebaseAuth mAuth;

    private DatabaseReference usersRef;

    private List<Messages> userMessagesList;

    private Context context;


    public MessageAdapter (List<Messages> userMessagesList){

        this.userMessagesList = userMessagesList;
    }

    public MessageAdapter(List<Messages> userMessagesList, Context context) {
        this.userMessagesList = userMessagesList;
        this.context = context;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);

        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        context = parent.getContext();

        return new MessageViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("image")){

                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    /*Glide.with(holder.itemView.getContext())
                            .load(receiverImage)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(holder.receiverProfileImage);*/
                    //The above method has wrong way of getting the context

                    Glide.with(context.getApplicationContext())
                            .load(receiverImage)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);


        if(fromMessageType.equals("text")){


            if(fromUserId.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setText(messages.getMessage() + "\n" + messages.getTime() + "  " + messages.getDate());


                holder.senderMessageText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0){

                                    DeleteSentMessages(position, holder);
                                }
                                else if(which == 1){

                                    DeleteMessageForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();

                        return true;
                    }
                });

            }
            else{

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);


                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setText(messages.getMessage() + "\n" + messages.getTime() + "  " + messages.getDate());


                holder.receiverMessageText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0){

                                    DeleteReceivedMessages(position, holder);
                                }

                            }
                        });
                        builder.show();

                        return true;
                    }
                });


            }
        }

        else if(fromMessageType.equals("image")){


            if(fromUserId.equals(messageSenderId)){

                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Glide.with(holder.messageSenderPicture.getContext()).load(messages.getMessage()).into(holder.messageSenderPicture);


                holder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });


                holder.messageSenderPicture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0){

                                    DeleteSentMessages(position, holder);
                                }
                                else if(which == 1){

                                    DeleteMessageForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();

                        return true;
                    }
                });

            }
            else{

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);


                Glide.with(holder.messageReceiverPicture.getContext()).load(messages.getMessage()).into(holder.messageReceiverPicture);


                holder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });


                holder.messageReceiverPicture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0){

                                    DeleteReceivedMessages(position, holder);
                                }

                            }
                        });
                        builder.show();

                        return true;
                    }
                });

            }
        }

        else if(fromMessageType.equals("pdf") || fromMessageType.equals("doc")){

            if(fromUserId.equals(messageSenderId)){

                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                holder.messageSenderPicture.setBackgroundResource(R.drawable.doc_icon);

                holder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.messageSenderPicture.getContext().startActivity(intent);
                    }
                });


                holder.messageSenderPicture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0){

                                    DeleteSentMessages(position, holder);
                                }
                                else if(which == 1){

                                    DeleteMessageForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();

                        return true;
                    }
                });



            }
            else{

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);


                holder.messageReceiverPicture.setBackgroundResource(R.drawable.doc_icon);


                holder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.messageReceiverPicture.getContext().startActivity(intent);
                    }
                });



                holder.messageReceiverPicture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == 0){

                                    DeleteReceivedMessages(position, holder);
                                }

                            }
                        });
                        builder.show();

                        return true;
                    }
                });

            }
        }



    }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }



    private void DeleteSentMessages(final int position, final MessageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void DeleteReceivedMessages(final int position, final MessageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void DeleteMessageForEveryone(final int position, final MessageViewHolder holder){

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                rootRef.child("Messages")
                        .child(userMessagesList.get(position).getFrom())
                        .child(userMessagesList.get(position).getTo())
                        .child(userMessagesList.get(position).getMessageId())
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
