package com.pr3V1;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class CompletitionOfTaskMsg implements Serializable {
    int week;
    int month;
    int year;
    int hour;
    int day;
    int id;
    public CompletitionOfTaskMsg(LocalDateTime date,int id){
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
}
