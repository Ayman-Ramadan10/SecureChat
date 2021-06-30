package com.w4po.securechat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.Holder> {
    private ArrayList<Contact> chatsList;
    private Context context;
    private LayoutInflater inflater;
    private char type;

    public ContactAdapter(Context context, ArrayList<Contact> chatsList, char type) {
        this.context = context;
        this.chatsList = chatsList;
        this.type = type;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.users_display_layout, viewGroup, false);

        return new ContactAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactAdapter.Holder holder, int i) {
        Contact contact = chatsList.get(i);

        holder.txtName.setText(contact.getName());

        if (contact.getImage() != null)
            Picasso.get().load(contact.getImage()).into(holder.profileImage);

        HashMap<String, Object> userState = (HashMap) contact.getUserState();

        String status = null;
        if (type == '1') { //ContactsFragment
            status = contact.getStatus() == null ? "" : contact.getStatus();
            holder.txtStatus.setText(status);
        }

        if (userState == null || ((String) userState.get("state")).equals("offline")) {
            holder.imgOnline.setVisibility(View.INVISIBLE);
            if (type == '0') {
                Long timestamp = userState != null && userState.containsKey("timestamp") ? (Long) userState.get("timestamp") : null;
                if (timestamp == null) {
                    holder.txtStatus.setText("offline");
                } else {
                    String timeStr = DateFormat.getDateTimeInstance().format(new Date(timestamp));
                    holder.txtStatus.setText("Last Seen: " + timeStr);
                }
            }
        } else {
            holder.imgOnline.setVisibility(View.VISIBLE);

            if (type == '0')
                holder.txtStatus.setText("online");
        }

        holder.itemView.setOnClickListener(view -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("visit_user_id", contact.getUid());
            chatIntent.putExtra("visit_user_name", contact.getName());
            chatIntent.putExtra("visit_image", contact.getImage());
            chatIntent.putExtra("visit_user_PK", contact.getPublicKey());
            context.startActivity(chatIntent);
        });
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView txtName, txtStatus;
        public CircleImageView profileImage;
        public ImageView imgOnline;


        public Holder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtNamePT);
            txtStatus = itemView.findViewById(R.id.txtUserStatusPT);
            profileImage = itemView.findViewById(R.id.imgUserPicPT);
            imgOnline = itemView.findViewById(R.id.imgOnlinePT);
        }
    }
}
