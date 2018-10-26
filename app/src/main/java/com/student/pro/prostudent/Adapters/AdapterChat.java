package com.student.pro.prostudent.Adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.student.pro.prostudent.Objects.Chat;
import com.student.pro.prostudent.R;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.ViewHolder> {
    private static final String TAG = "AdapterChatLog";
    private ArrayList<Chat> messages = new ArrayList<>();
    private Context mContext;
    private String status;

    public AdapterChat(ArrayList<Chat> messages, Context mContext, String status) {
        this.messages = messages;
        this.mContext = mContext;
        this.status = status;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder:");
        if (status.equals("professor")) {
            if (messages.get(position).getSender().toString().equals("student")) {
                holder.leftLayout.setVisibility(LinearLayout.VISIBLE);
                holder.leftMessage.setText(messages.get(position).getContent().toString());
                holder.rightLayout.setVisibility(LinearLayout.GONE);
                String[] arraydate = messages.get(position).getDate().toString().split(" ");
                holder.date_received.setText(arraydate[0] + " - " + arraydate[1]);

            } else {
                holder.rightLayout.setVisibility(LinearLayout.VISIBLE);
                holder.rightMessage.setText(messages.get(position).getContent().toString());
                holder.leftLayout.setVisibility(LinearLayout.GONE);
                String[] arraydate = messages.get(position).getDate().toString().split(" ");
                holder.date_send.setText(arraydate[0] + " - " + arraydate[1]);

            }
        } else {
            if (messages.get(position).getSender().toString().equals("professor")) {
                holder.leftLayout.setVisibility(LinearLayout.VISIBLE);
                holder.leftMessage.setText(messages.get(position).getContent().toString());
                holder.rightLayout.setVisibility(LinearLayout.GONE);
                String[] arraydate = messages.get(position).getDate().toString().split(" ");
                holder.date_received.setText(arraydate[0] + " - " + arraydate[1]);
            } else {
                holder.rightLayout.setVisibility(LinearLayout.VISIBLE);
                holder.rightMessage.setText(messages.get(position).getContent().toString());
                holder.leftLayout.setVisibility(LinearLayout.GONE);
                String[] arraydate = messages.get(position).getDate().toString().split(" ");
                holder.date_send.setText(arraydate[0] + " - " + arraydate[1]);
            }
        }

        holder.leftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.date_received.getVisibility() == View.GONE) {
                    holder.date_received.setVisibility(View.VISIBLE);
                } else {
                    holder.date_received.setVisibility(View.GONE);
                }

            }
        });

        holder.rightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.date_send.getVisibility() == View.GONE) {
                    holder.date_send.setVisibility(View.VISIBLE);
                } else {
                    holder.date_send.setVisibility(View.GONE);

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout leftLayout;

        ConstraintLayout rightLayout;

        TextView leftMessage, date_send, date_received;

        TextView rightMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            leftLayout = itemView.findViewById(R.id.chat_left_msg_layout);
            rightLayout = itemView.findViewById(R.id.chat_right_msg_layout);
            leftMessage = itemView.findViewById(R.id.chat_left_msg_text_view);
            rightMessage = itemView.findViewById(R.id.chat_right_msg_text_view);
            date_send = itemView.findViewById(R.id.date_send);
            date_received = itemView.findViewById(R.id.date_received);

        }
    }
}
