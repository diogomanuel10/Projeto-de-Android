package com.student.pro.prostudent.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.student.pro.prostudent.Activities.TicketViewActivity;
import com.student.pro.prostudent.Objects.Tickets;
import com.student.pro.prostudent.Objects.Users;
import com.student.pro.prostudent.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jonnh on 3/29/2018.
 */

public class AdapterTicket extends RecyclerView.Adapter<AdapterTicket.ViewHolder> {
    private static final String TAG = "AdapterTicketLog";
    private String status;
    private ArrayList<Tickets> tickets = new ArrayList<>();
    private Context mContext;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference mDB_tickets = FirebaseDatabase.getInstance().getReference("tickets");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private String UserID = user.getUid();


    public AdapterTicket(ArrayList<Tickets> tickets, Context mContext, String status) {
        this.tickets = tickets;
        this.mContext = mContext;
        this.status = status;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);


        return holder;
    }

    private void deleteItem(int position) {
        this.tickets.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        mDatabase.child(tickets.get(position).getUser_id().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.ticket_user.setText(dataSnapshot.child("name").getValue().toString() + " " +
                        dataSnapshot.child("surname").getValue().toString());
                Picasso.get()
                        .load(dataSnapshot.child("url").getValue().toString())
                        .placeholder(R.drawable.default_icon)
                        .error(R.drawable.default_icon)
                        .into(holder.ticket_Uimage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        holder.ticket_title.setText(tickets.get(position).getTitle());
        if (tickets.get(position).getSolved().toString().equals("true")) {
            holder.ticket_solved.setBackground(mContext.getResources().getDrawable(R.drawable.ic_solved_full));
        } else if (tickets.get(position).getSolved().toString().equals("false") && status.equals("professor")) {
            holder.ticket_solved.setBackground(mContext.getResources().getDrawable(R.drawable.ic_solved));
            holder.ticket_solved.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDB_tickets.child(tickets.get(position).getUser_id().toString()).child(tickets.get(position).getTicket_id().toString()).child("solved").setValue("true");
                    holder.ticket_solved.setClickable(false);
                }
            });
        }
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, TicketViewActivity.class);
                Log.d(TAG, tickets.get(position).getTicket_id());
                intent.putExtra("TicketID", tickets.get(position).getTicket_id());
                intent.putExtra("UserID", tickets.get(position).getUser_id());
                intent.putExtra("Message", tickets.get(position).getContent());
                intent.putExtra("Date", tickets.get(position).getDate());
                intent.putExtra("Title", tickets.get(position).getTitle());
                intent.putExtra("Status", status);
                intent.putExtra("Solved", tickets.get(position).getSolved());
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView ticket_user, ticket_title;
        Button ticket_solved;
        CircleImageView ticket_Uimage;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ticket_solved = itemView.findViewById(R.id.ticket_solved);
            ticket_title = itemView.findViewById(R.id.ticket_title);
            ticket_user = itemView.findViewById(R.id.ticket_user);
            ticket_Uimage = itemView.findViewById(R.id.ticket_Uimage);
            parentLayout = itemView.findViewById(R.id.ticket_parent);

        }
    }

}
