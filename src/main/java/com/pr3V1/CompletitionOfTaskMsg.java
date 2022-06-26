package com.pr3V1;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class CompletitionOfTaskMsg implements Serializable {
    private int week;
    private int month;
    private int year;
    private int hour;
    private int day;
    private String id;
    public CompletitionOfTaskMsg(LocalDateTime date,String id){
        day=date.getDayOfYear();
        month=date.getMonthValue();
        year=date.getYear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        week = date.get(weekFields.weekOfWeekBasedYear());
        hour = date.getHour();
        this.id=id;
    }

    public CompletitionOfTaskMsg(){}
    public int getWeek() {
        return week;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public String getId() {
        return id;
    }
}
