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

public class CustomCompareNotes2 implements Comparator<Notes> {
    private static final String TAG = "notecomparelog";

    @Override
    public int compare(Notes notes, Notes t1) {
        String x1 = notes.getTag_disc();
        String x2 = t1.getTag_disc();
        int sComp = x1.compareTo(x2);
        return sComp;
    }
}
