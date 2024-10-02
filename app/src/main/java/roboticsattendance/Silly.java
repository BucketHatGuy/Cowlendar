package roboticsattendance;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class Silly {

    public static void main(String[] args) throws Exception{
        DateTimeFormatter formatter;
        String studentBeginTimeString;
        String studentEndTimeString;
        String meetBeginTimeString;
        String meetEndTimeString;
        LocalTime studentBeginTime;
        LocalTime studentEndTime;
        LocalTime meetBeginTime;
        LocalTime meetEndTime;
        double studentDuration;
        double meetDuration;
        double percent;

        formatter = DateTimeFormatter.ofPattern("hh:mma");  
        studentBeginTimeString = "08:22 pm";
        studentEndTimeString = "09:00PM";
        meetBeginTimeString = "08:00 Pm";
        meetEndTimeString = "09:00pM";
        studentBeginTime = LocalTime.parse(studentBeginTimeString.toUpperCase().replaceAll("\\s", ""), formatter);
        studentEndTime = LocalTime.parse(studentEndTimeString.toUpperCase().replaceAll("\\s", ""), formatter);
        meetEndTime = LocalTime.parse(meetEndTimeString.toUpperCase().replaceAll("\\s", ""), formatter);
        meetBeginTime = LocalTime.parse(meetBeginTimeString.toUpperCase().replaceAll("\\s", ""), formatter);

        studentBeginTime = studentBeginTime.isBefore(meetBeginTime) ? meetBeginTime : studentBeginTime;
        studentEndTime = studentEndTime.isBefore(meetEndTime) ? meetEndTime : studentEndTime;

        studentDuration = (Duration.between(studentBeginTime, studentEndTime).toMinutes());
        System.out.println("StudentDuration: " + studentDuration);
        meetDuration = Duration.between(meetBeginTime, meetEndTime).toMinutes();
        System.out.println("MeetDuration: " + meetDuration);

        System.out.println((studentDuration/meetDuration));

        percent = Math.round((studentDuration/meetDuration) * 4) / 4.0;

        System.out.println("Your percentage for today was: " + percent);
    }
}
