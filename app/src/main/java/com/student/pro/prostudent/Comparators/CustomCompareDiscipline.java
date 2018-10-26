package com.student.pro.prostudent.Comparators;

import com.student.pro.prostudent.Objects.Disciplines;

import java.util.Comparator;

/**
 * Created by jonnh on 3/15/2018.
 */

public class CustomCompareDiscipline implements Comparator<Disciplines> {

    @Override
    public int compare(Disciplines disciplines, Disciplines t1) {

        String x1 = disciplines.getYear();
        String x2 = t1.getYear();
        int sComp = x1.compareTo(x2);
        if (sComp != 0) {
            return sComp;
        } else {
            String y1 = disciplines.getName();
            String y2 = t1.getName();
            return y1.compareTo(y2);
        }
    }
}
