package com.w4po.securechat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    private List<Message> userMessagesList;
    private FirebaseAuth mAuth;
    private String currentUserID, otherUserImage;
    private LayoutInflater inflater;


    public MessageAdapter(Context context, List<Message> userMessagesList, String otherUserImage) {
        this.userMessagesList = userMessagesList;
        this.otherUserImage = otherUserImage;
        inflater = LayoutInflater.from(context);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.custom_messages_layout, viewGroup, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        Message message = userMessagesList.get(i);
        String fromUID = message.getFrom();

        holder.senderLayout.setVisibility(View.GONE);
        holder.receiverLayout.setVisibility(View.GONE);

        String timeStr = DateFormat.getDateTimeInstance().format(new Date(message.getTimestamp()));

        if (fromUID.equals(currentUserID)) {
            holder.senderLayout.setVisibility(View.VISIBLE);

            holder.txtSenderMessage.setText(message.getMessage());
            holder.txtSenderTime.setText(timeStr);
        } else {
            holder.receiverLayout.setVisibility(View.VISIBLE);

            Picasso.get().load(otherUserImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);

            holder.receiverProfileImage.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent profileIntent = new Intent(context, ProfileActivity.class);
                profileIntent.putExtra("visit_user_id", fromUID);
                context.startActivity(profileIntent);
            });

            holder.txtReceiverMessage.setText(message.getMessage());
            holder.txtReceiverTime.setText(timeStr);
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        public LinearLayout senderLayout, receiverLayout;
        public TextView txtSenderMessage, txtSenderTime, txtReceiverMessage, txtReceiverTime;
        public CircleImageView receiverProfileImage;

        public Holder(@NonNull View itemView) {
            super(itemView);

            senderLayout = itemView.findViewById(R.id.senderLinLCM);
            receiverLayout = itemView.findViewById(R.id.receiverLinLCM);
            txtSenderMessage = itemView.findViewById(R.id.txtSenderMsgCM);
            txtSenderTime = itemView.findViewById(R.id.txtSenderTimeCM);
            txtReceiverMessage = itemView.findViewById(R.id.txtReceiverMsgCM);
            txtReceiverTime = itemView.findViewById(R.id.txtReceiverTimeCM);
            receiverProfileImage = itemView.findViewById(R.id.imgReceiverPicCM);
        }
    }
}
