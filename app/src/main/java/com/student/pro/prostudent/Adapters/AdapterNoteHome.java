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
import android.widget.ImageView;
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

public class AdapterNoteHome extends RecyclerView.Adapter<AdapterNoteHome.ViewHolder> {
    private static final String TAG = "AdapterNoteLog";

    private ArrayList<Notes> notes = new ArrayList<>();
    private Context mContext;
    private String status;

    public AdapterNoteHome(ArrayList<Notes> notes, Context mContext, String status) {
        this.notes = notes;
        this.mContext = mContext;
        this.status = status;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout_home, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Intent intent = new Intent(mContext, NoteViewActivity.class);
        intent.putExtra("ID", notes.get(position).getNote_id().toString());

        holder.title.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryLight));
        holder.content.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryLightTransparent));
        holder.title.setText(notes.get(position).getTitle());
        holder.content.setText(notes.get(position).getContent());
        Log.d(TAG, notes.get(position).getTag_disc());
        holder.tag_home.setText(notes.get(position).getTag_disc());
        intent.putExtra("Read", "false");
        intent.putExtra("Status", status);
        if(position>0)
        {
            if(notes.get(position).getTag_disc().toString().equals(notes.get(position-1).getTag_disc().toString()))
            {
                holder.section_const.setVisibility(View.GONE);
            }
        }



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

        TextView title, content,tag_home;
        ImageView right,left;
        ConstraintLayout parentLayout,section_const;

        public ViewHolder(View itemView) {
            super(itemView);
            section_const = itemView.findViewById(R.id.section_const);

            tag_home = itemView.findViewById(R.id.text_tag_home);
            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);
            parentLayout = itemView.findViewById(R.id.note_parent);

        }
    }


}
