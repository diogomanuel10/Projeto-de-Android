package com.student.pro.prostudent.Comparators;

import android.util.Log;

import com.student.pro.prostudent.Objects.Notes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by jonnh on 3/27/2018.
 */

public class CustomCompareNotes implements Comparator<Notes> {
    private static final String TAG = "notecomparelog";

    @Override
    public int compare(Notes notes, Notes t1) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        Date x1 = null;
        Date x2 = null;

        try {
            x1 = sdf.parse(notes.getDate().toString());
            x2 = sdf.parse(t1.getDate().toString());

        } catch (ParseException e) {


            e.printStackTrace();
            }
        float dif = x1.getTime()-x2.getTime();
        if(dif>0)
        {
            Log.d(TAG, "compare: -1");
            return -1;
        }
        if(dif<0){
            Log.d(TAG, "compare: 1");

            return 1;
        }
        else
        {
            Log.d(TAG, "compare: 0");

            return 0 ;
        }

    }
}
