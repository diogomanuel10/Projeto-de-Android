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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.student.pro.prostudent.Activities.NoteViewActivity;
import com.student.pro.prostudent.Objects.Notes;
import com.student.pro.prostudent.R;

import java.util.ArrayList;

/**
 * Created by jonnh on 3/27/2018.
 */

public class AdapterNote extends RecyclerView.Adapter<AdapterNote.ViewHolder> {
    private static final String TAG = "AdapterNoteLog";

    private ArrayList<Notes> notes = new ArrayList<>();
    private Context mContext;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("notes_read");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private String UserID = user.getUid(), status;

    public AdapterNote(ArrayList<Notes> notes, Context mContext, String status) {
        this.notes = notes;
        this.mContext = mContext;
        this.status = status;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
    private void deleteItem(int position) {
        this.notes.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Boolean mPref = sharedPref.getBoolean("notes_read", false);
        final Intent intent = new Intent(mContext, NoteViewActivity.class);
        intent.putExtra("ID", notes.get(position).getNote_id().toString());
        mDatabase.child(notes.get(position).getNote_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Pré snapshot = " + String.valueOf(position));

                Boolean flag = false;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(position<notes.size())
                    {
                        if (dataSnapshot.getKey().equals(notes.get(position).getNote_id())) {
                            if (postSnapshot.getKey().equals(UserID) && postSnapshot.getValue().toString().equals("true")) {
                                //Ver todas as notas
                                if (!mPref) {
                                    holder.title.setTextColor(mContext.getResources().getColor(R.color.colorSecondaryDark));
                                    holder.content.setTextColor(mContext.getResources().getColor(R.color.colorSecondaryDarkTransparent));
                                    holder.title.setText(notes.get(position).getTitle());
                                    holder.content.setText(notes.get(position).getContent());
                                    intent.putExtra("Read", "true");
                                    intent.putExtra("Status",status);

                                } else {
                                    deleteItem(position);

                                }
                                flag = true;
                            }
                        }
                    }

                }

                //Nota por ler
                if (!flag) {
                    if(position<notes.size())
                    {
                        holder.title.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryLight));
                        holder.content.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryLightTransparent));
                        holder.title.setText(notes.get(position).getTitle());
                        holder.content.setText(notes.get(position).getContent());
                        intent.putExtra("Read", "false");
                        intent.putExtra("Status",status);
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, content;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);
            parentLayout = itemView.findViewById(R.id.note_parent);

        }
    }


}
