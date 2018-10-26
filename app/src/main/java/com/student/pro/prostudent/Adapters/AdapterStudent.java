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
import com.student.pro.prostudent.Activities.TicketActivity;
import com.student.pro.prostudent.Objects.Disciplines;
import com.student.pro.prostudent.Objects.Favorite;
import com.student.pro.prostudent.R;


import java.util.ArrayList;

/**
 * Created by jonnh on 3/14/2018.
 */

public class AdapterStudent extends RecyclerView.Adapter<AdapterStudent.ViewHolder> {
    private static final String TAG = "AdapterStudentLog";

    private ArrayList<Disciplines> ucs = new ArrayList<>();

    private Context mContext;

    public AdapterStudent(ArrayList<Disciplines> ucs, Context mContext) {
        this.ucs = ucs;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_discipline_layout, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final String user_id = user.getUid();

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users_fav").child(user_id);
        holder.disc_back.setBackgroundResource(R.drawable.round_button_two_colors);
        holder.name.setText(ucs.get(position).getName().toString());
        holder.tag.setText(ucs.get(position).getTag().toString());
        holder.home_fav.setBackgroundResource(R.drawable.ic_favorite_border);
      mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (int i = 0; i < ucs.size(); i++) {

                        if (postSnapshot.child("id").getValue().toString().equals(ucs.get(position).getId().toString())
                                && postSnapshot.child("tag").getValue().toString().equals(ucs.get(position).getTag().toString())
                                ) {
                            Log.d(TAG, "Posição holder = " + String.valueOf(position));
                            Log.d(TAG, "ID Disciplina = " + ucs.get(position).getId());
                            Log.d(TAG, "Snapshot = "+postSnapshot.child("id").getValue());
                            Log.d(TAG, "TAG Disciplina = " + ucs.get(position).getTag());
                            Log.d(TAG, "Snapshot = "+postSnapshot.child("tag").getValue());
                            Log.d(TAG, "onDataChange: ENTROU");
                            holder.home_fav.setBackgroundResource(R.drawable.ic_favorite_full);
                        }
                    }
                }
                mDatabase.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, ClassActivity.class);
                intent.putExtra("Discipline", ucs.get(position).getTag().toString());
                intent.putExtra("ID", ucs.get(position).getId().toString());
                intent.putExtra("Status", "student");
                mContext.startActivity(intent);
                Intent intent_finish = new Intent("finish_home");
                mContext.sendBroadcast(intent_finish);
            }
        });

        holder.home_ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TicketActivity.class);
                intent.putExtra("Discipline", ucs.get(position).getTag().toString());
                intent.putExtra("ID", ucs.get(position).getId().toString());
                mContext.startActivity(intent);
            }
        });

      holder.home_fav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (holder.home_fav.getBackground().getConstantState().equals
                        (mContext.getResources().getDrawable(R.drawable.ic_favorite_border).getConstantState())) {
                    holder.home_fav.setBackgroundResource(R.drawable.ic_favorite_full);
                    Favorite mFav = new Favorite(ucs.get(position).getTag().toString(),
                            ucs.get(position).getId().toString());
                    mDatabase.push().setValue(mFav);
                } else {
                    holder.home_fav.setBackgroundResource(R.drawable.ic_favorite_border);
                    final Favorite mFav = new Favorite(ucs.get(position).getTag().toString(),
                            ucs.get(position).getId().toString());

                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                if (postSnapshot.child("tag").getValue().toString().equals(mFav.getTag().toString())
                                        && postSnapshot.child("id").getValue().toString().equals(mFav.getId().toString()
                                )) {
                                    postSnapshot.getRef().removeValue();
                                }
                            }
                            mDatabase.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ucs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, tag;
        Button home_ticket, home_fav;
        ConstraintLayout parentLayout, disc_back;

        public ViewHolder(View itemView) {
            super(itemView);
            disc_back = itemView.findViewById(R.id.disc_background);
            name = itemView.findViewById(R.id.class_name);
            tag = itemView.findViewById(R.id.class_prof_tag);
            parentLayout = itemView.findViewById(R.id.class_view);
            home_ticket = itemView.findViewById(R.id.class_add);
            home_fav = itemView.findViewById(R.id.home_favorite);
        }
    }
}

