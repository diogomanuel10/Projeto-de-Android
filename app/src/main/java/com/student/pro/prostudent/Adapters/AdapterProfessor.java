package com.student.pro.prostudent.Adapters;

import android.content.Context;
import android.content.Intent;
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
import com.student.pro.prostudent.Activities.ClassActivity;
import com.student.pro.prostudent.Activities.NoteActivity;
import com.student.pro.prostudent.Objects.Disciplines;
import com.student.pro.prostudent.R;

import java.util.ArrayList;

/**
 * Created by jonnh on 3/14/2018.
 */

public class AdapterProfessor extends RecyclerView.Adapter<AdapterProfessor.ViewHolder> {
    private static final String TAG = "AdapterProfessorLog";

    private ArrayList<Disciplines> ucs = new ArrayList<>();

    private Context mContext;

    public AdapterProfessor(ArrayList<Disciplines> ucs, Context mContext) {
        this.ucs = ucs;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.professor_discipline_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final String user_id = user.getUid();
        final DatabaseReference mDB_tickets = FirebaseDatabase.getInstance().getReference("tickets");
        final int[] total_tickets = {0};
        final int[] solved_tickets = {0};
        mDB_tickets.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    for (DataSnapshot postpostSnap : postSnapshot.getChildren())
                    {
                        if(postpostSnap.child("id_disc").getValue().toString().equals(ucs.get(position).getId()) &&
                                postpostSnap.child("tag_disc").getValue().toString().equals(ucs.get(position).getTag())   )
                        {
                            total_tickets[0]++;
                        }
                        if(postpostSnap.child("id_disc").getValue().toString().equals(ucs.get(position).getId())&&
                                postpostSnap.child("tag_disc").getValue().toString().equals(ucs.get(position).getTag()) && postpostSnap.child("solved").getValue().toString().equals("true"))
                        {
                            Log.d(TAG, "ENTREI");
                            solved_tickets[0]++;
                        }
                    }
                }
                holder.ticket_count.setText(String.valueOf(solved_tickets[0]) + "/" + String.valueOf(total_tickets[0]));
                solved_tickets[0]=0;
                total_tickets[0]=0;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.name.setText(ucs.get(position).getName().toString());
        holder.tag.setText(ucs.get(position).getTag().toString());

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, ClassActivity.class);
                intent.putExtra("Discipline", ucs.get(position).getTag().toString());
                intent.putExtra("ID", ucs.get(position).getId().toString());
                intent.putExtra("Status","professor");
                mContext.startActivity(intent);
                Intent intent_finish = new Intent("finish_home");
                mContext.sendBroadcast(intent_finish);
            }
        });

        holder.add_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NoteActivity.class);
                intent.putExtra("Discipline", ucs.get(position).getTag().toString());
                intent.putExtra("ID", ucs.get(position).getId().toString());
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return ucs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, tag,ticket_count;
        Button add_note;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.class_prof_name);
            tag = itemView.findViewById(R.id.class_prof_tag);
            parentLayout = itemView.findViewById(R.id.class_prof_view);
            add_note = itemView.findViewById(R.id.class_add);
            ticket_count = itemView.findViewById(R.id.ticket_count);
        }
    }
}

