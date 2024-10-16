package roboticsattendance;

import java.awt.Color;
import java.io.FileWriter;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;


public class Cowlendar extends ListenerAdapter {
    static Connection connection = null;
    static Statement statement;
    static ResultSet resultSet;

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event){
        // event variables
        String command = event.getName();
        String username = event.getUser().getName();
        ArrayList<String> memberDivisionsArray;
        HashSet<String> updatedDatesSet = getMemberDates(getMemberDivisions(username));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM_dd_yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma");
        LocalDate currentDate = LocalDate.now();
        String message = "";
        OptionMapping option1;
        OptionMapping option2;
        OptionMapping option3;
        EmbedBuilder embed = new EmbedBuilder();

        // common variable names used in the switch case
        String date = "";
        String time = "";
        int divisionsAccountedFor = 0;
        boolean formatError = false;
        String member = "";
        ArrayList<String> variableDivisionsArray;
        String division;

        memberDivisionsArray = getMemberDivisions(username);

        event.deferReply().queue();

        if(!(getMembers()).contains(username)){
            message = "Error: You weren't found in the database. Please contact a mentor to get added.";
            embed.setColor(Color.RED);
        } else {
            switch (command) {
                case "login":
                    option1 = event.getOption("date");
                    date = option1 != null ? option1.getAsString() : "";

                    option2 = event.getOption("time");
                    time = option2 != null ? option2.getAsString() : "";

                    date = date != "" ? date : currentDate.format(dateFormatter).toString();
                    divisionsAccountedFor = 0;

                    try {
                        date = getFormattedDate(date);
                    } catch (Exception e) {
                        e.printStackTrace();

                        message = "Error: Invalid date format. Check the date you entered and try again.";
                        embed.setColor(Color.RED);
                        formatError = true;
                    }

                    try {
                        time = getFormattedTime(time);
                    } catch (Exception e) {
                        e.printStackTrace();

                        message = "Error: Invalid time format. Check the time you entered and try again.";
                        embed.setColor(Color.RED);
                        formatError = true;
                    }

                    if(!formatError){
                        if((currentDate.format(dateFormatter).toString()).equals(date)){
                            if(updatedDatesSet.contains(date)){
                                for (String divisionInArray : memberDivisionsArray) {
                                    try {
                                        statement.executeUpdate("UPDATE " + divisionInArray + " SET " + date + " = '" + time +"' WHERE memberName='" + username + "';");
                                        divisionsAccountedFor++;
                                    } catch (Exception e) {
                                        continue;
                                    }
                                }
            
                                if(divisionsAccountedFor == 0){
                                    message = "Error: Unknown. If this persists, contact Jose probably lol.";
                                    embed.setColor(Color.RED);
                                } else {
                                    message = "Success! You have logged in for the meeting on " + date;
                                    embed.setColor(Color.GREEN);
                                }

                            } else {
                                message = "Error: Invalid date. Check the date you entered and try again.";
                                embed.setColor(Color.RED);
                            }
                        } else {
                            event.getChannel().sendMessage("<@330057747469172737> " + username + " has requested an attendance correction.").queue();
                            message = "Date = " + date + " \nTime = " + time;
                            embed.setColor(Color.YELLOW);
                        }
                    }
                break;

                case "impersonate":
                    option1 = event.getOption("date");
                    date = option1 != null ? option1.getAsString() : "";

                    option2 = event.getOption("time");
                    time = option2 != null ? option2.getAsString() : "";

                    option3 = event.getOption("member");
                    member = option3 != null ? option3.getAsString() : "";

                    date = date != "" ? date : currentDate.format(dateFormatter).toString();
                    divisionsAccountedFor = 0;

                    try {
                        date = getFormattedDate(date);
                    } catch (Exception e) {
                        e.printStackTrace();

                        message = "Error: Invalid date format. Check the date you entered and try again.";
                        embed.setColor(Color.RED);
                        formatError = true;
                    }

                    try {
                        time = getFormattedTime(time);
                    } catch (Exception e) {
                        e.printStackTrace();

                        message = "Error: Invalid time format. Check the time you entered and try again.";
                        embed.setColor(Color.RED);
                        formatError = true;
                    }

                    if(!getMembers().contains(member)){
                        message = "Error: Member is not in the database. Check the member you entered and try again.";
                        embed.setColor(Color.RED);
                        formatError = true;
                    } else {
                        memberDivisionsArray = getMemberDivisions(member);
                    }

                    if(!formatError){
                        if(updatedDatesSet.contains(date)){
                            for (String divisionInArray : memberDivisionsArray) {
                                try {
                                    statement.executeUpdate("UPDATE " + divisionInArray + " SET " + date + " = '" + time +"' WHERE memberName='" + member + "';");
                                    divisionsAccountedFor++;
                                } catch (Exception e) {
                                    continue;
                                }
                            }
            
                            if(divisionsAccountedFor == 0){
                                message = "Error: Unknown. If this persists, contact Jose probably lol.";
                                embed.setColor(Color.RED);
                            } else {
                                message = "Success! You have logged in as \"" + member + "\" for the meet on " + date;
                                embed.setColor(Color.GREEN);
                            }

                        } else {
                            message = "Error: Invalid date. Check the date you entered and try again.";
                            embed.setColor(Color.RED);
                        }
                    }
                    
                break;

                case "percent":
                    ArrayList<Double> attendanceArray = new ArrayList<>();
                    ArrayList<String> divisionDatesArray = new ArrayList<>();
                    Double totalNumber = 0.0;

                    String[] studentTimeList;
                    String[] meetTimeList;
                    LocalTime studentBeginTime;
                    LocalTime studentEndTime;
                    LocalTime meetBeginTime;
                    LocalTime meetEndTime;
                    double studentDuration;
                    double meetDuration;

                    option1 = event.getOption("member");
                    member = option1 != null ? option1.getAsString() : "";

                    option2 = event.getOption("division");
                    division = option2 != null ? option2.getAsString() : "";

                    if(member != ""){
                        if((getAdmins()).contains(username)){
                            if((getMembers()).contains(member)){
                                if(getMemberDivisions(member).contains(division)){
                                    divisionDatesArray = getDivisionDates(division);

                                    for (String dateInArray : divisionDatesArray) {
                                        try{
                                            resultSet = statement.executeQuery("SELECT " + dateInArray + " FROM " + division + " WHERE memberName = '" + member + "';");
                                            resultSet.next();

                                            studentTimeList = (resultSet.getString(dateInArray)).split("-");

                                            System.out.println(studentTimeList[0]);
                                            System.out.println(studentTimeList[1]);

                                            studentBeginTime = LocalTime.parse(studentTimeList[0].toUpperCase().replaceAll("\\s", ""), timeFormatter);
                                            studentEndTime = LocalTime.parse(studentTimeList[1].toUpperCase().replaceAll("\\s", ""), timeFormatter);

                                            System.out.println(studentBeginTime);
                                            System.out.println(studentEndTime);

                                            resultSet = statement.executeQuery("SELECT " + dateInArray + " FROM " + division + " WHERE memberName = 'time';");
                                            resultSet.next();

                                            meetTimeList = (resultSet.getString(dateInArray)).split("-");

                                            meetBeginTime = LocalTime.parse(meetTimeList[0].toUpperCase().replaceAll("\\s", ""), timeFormatter);
                                            meetEndTime = LocalTime.parse(meetTimeList[1].toUpperCase().replaceAll("\\s", ""), timeFormatter);

                                            studentBeginTime = studentBeginTime.isBefore(meetBeginTime) ? meetBeginTime : studentBeginTime;
                                            studentEndTime = studentEndTime.isAfter(meetEndTime) ? meetEndTime : studentEndTime;

                                            if(!(((studentBeginTime.isAfter(meetEndTime)) && !studentBeginTime.equals(meetEndTime)) || (studentEndTime.isBefore(meetBeginTime) && !studentEndTime.equals(meetBeginTime)))){
                                                studentDuration = (Duration.between(studentBeginTime, studentEndTime).toMinutes());
                                                meetDuration = Duration.between(meetBeginTime, meetEndTime).toMinutes();

                                                attendanceArray.add(Math.round((studentDuration/meetDuration) * 4) / 4.0);
                                            } else {
                                                System.out.println("We're here!");

                                                attendanceArray.add(0.0);
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();

                                            attendanceArray.add(0.0);
                                        }
                                    }

                                    System.out.println(attendanceArray);

                                    for(Double value : attendanceArray){
                                        totalNumber += value;
                                    }

                                    message = member + "'s attendance rate is: " + Math.round((totalNumber / attendanceArray.size()) * 100) + "%";
                                    embed.setColor(Color.LIGHT_GRAY);
                                } else {
                                    message = "Error: Member isn't on that division. Please check the spelling and try again";
                                    embed.setColor(Color.RED);
                                }
                            } else {
                                message = "Error: Member not found. Please check the spelling and try again";
                                embed.setColor(Color.RED);
                            }
                        } else {
                            message = "Error: Permission denied. Only admins are allowed to check other member's attendance rates.";
                            embed.setColor(Color.RED);
                        }

                    } else {
                        if(getMemberDivisions(username).contains(division)){
                            divisionDatesArray = getDivisionDates(division);

                            for (String dateInArray : divisionDatesArray) {
                                try{
                                    resultSet = statement.executeQuery("SELECT " + dateInArray + " FROM " + division + " WHERE memberName = '" + username + "';");
                                    resultSet.next();

                                    studentTimeList = (resultSet.getString(dateInArray)).split("-");

                                    System.out.println(studentTimeList[0]);
                                    System.out.println(studentTimeList[1]);

                                    studentBeginTime = LocalTime.parse(studentTimeList[0].toUpperCase().replaceAll("\\s", ""), timeFormatter);
                                    studentEndTime = LocalTime.parse(studentTimeList[1].toUpperCase().replaceAll("\\s", ""), timeFormatter);

                                    System.out.println(studentBeginTime);
                                    System.out.println(studentEndTime);

                                    resultSet = statement.executeQuery("SELECT " + dateInArray + " FROM " + division + " WHERE memberName = 'time';");
                                    resultSet.next();

                                    meetTimeList = (resultSet.getString(dateInArray)).split("-");

                                    meetBeginTime = LocalTime.parse(meetTimeList[0].toUpperCase().replaceAll("\\s", ""), timeFormatter);
                                    meetEndTime = LocalTime.parse(meetTimeList[1].toUpperCase().replaceAll("\\s", ""), timeFormatter);

                                    studentBeginTime = studentBeginTime.isBefore(meetBeginTime) ? meetBeginTime : studentBeginTime;
                                    studentEndTime = studentEndTime.isAfter(meetEndTime) ? meetEndTime : studentEndTime;

                                    if(!(((studentBeginTime.isAfter(meetEndTime)) && !studentBeginTime.equals(meetEndTime)) || (studentEndTime.isBefore(meetBeginTime) && !studentEndTime.equals(meetBeginTime)))){
                                        studentDuration = (Duration.between(studentBeginTime, studentEndTime).toMinutes());
                                        meetDuration = Duration.between(meetBeginTime, meetEndTime).toMinutes();

                                        attendanceArray.add(Math.round((studentDuration/meetDuration) * 4) / 4.0);
                                    } else {
                                        attendanceArray.add(0.0);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();

                                    attendanceArray.add(0.0);
                                }
                            }

                            System.out.println(attendanceArray);

                            for(Double value : attendanceArray){
                                totalNumber += value;
                            }

                            message = "Your attendance rate is: " + Math.round((totalNumber / attendanceArray.size()) * 100) + "%";
                            embed.setColor(Color.LIGHT_GRAY);
                        } else {
                            message = "Error: Member isn't on that division. Please check the spelling and try again";
                            embed.setColor(Color.RED);
                        }
                    }

                    break;

                case "get_dates":
                    ArrayList<String> datesArray = new ArrayList<>();

                    option1 = event.getOption("division");
                    division = option1 != null ? option1.getAsString() : "";

                    datesArray = getDivisionDates(division);

                    if(!(datesArray.isEmpty())){
                        message = "The following dates are on " + division + ": ";
                        embed.setDescription(datesArray.toString());
                        embed.setColor(Color.LIGHT_GRAY);
                    } else {
                        message = "Error: Unable to fetch dates. It's likely that the division you typed was wrong, so check your spelling and try again.";
                        embed.setColor(Color.RED);
                    }
                    
                    break;

                case "member_list":
                    message = "These are all the members: ";
                    embed.setDescription(getMembers().toString());
                    embed.setColor(Color.LIGHT_GRAY);
                    break;

                case "division_list":
                    message = "These are all the divisions: ";
                    embed.setDescription(getAllDivisions().toString());
                    embed.setColor(Color.LIGHT_GRAY);
                    break;

                case "resolve":
                    if((getAdmins()).contains(username)){
                        formatError = false;

                        option1 = event.getOption("date");
                        option2 = event.getOption("member");

                        date = option1 != null ? option1.getAsString() : "";
                        member = option2 != null ? option2.getAsString() : "";

                        try {
                            date = getFormattedDate(date);
                        } catch (Exception e) {
                            e.printStackTrace();
    
                            message = "Error: Invalid date format. Check the date you entered and try again.";
                            embed.setColor(Color.RED);
                            formatError = true;
                        }

                        if(!getMembers().contains(member)){
                            message = "Error: Member not found. Check the member you entered and try again.";
                            embed.setColor(Color.RED);
                            formatError = true;
                        }

                        if(!formatError){
                            if(updatedDatesSet.contains(date)){
                                for (String divisionInArray : memberDivisionsArray) {
                                    try {
                                        statement.executeUpdate("UPDATE " + divisionInArray + " SET " + date + " = null WHERE memberName='" + member + "';");
                                        divisionsAccountedFor++;
                                    } catch (Exception e) {
                                        continue;
                                    }
                                }
            
                                if(divisionsAccountedFor == 0){
                                    message = "Error: Unknown. If this persists, contact Jose probably lol.";
                                    embed.setColor(Color.RED);
                                } else {
                                    message = "Success! " + member + "'s attendance has been resolved for " + date;
                                    embed.setColor(Color.GREEN);
                                }
                            } else {
                                message = "Error: Invalid date. Please check the format (MM_DD_YY) and try again.";
                                embed.setColor(Color.RED);
                            }
                        }
                    } else {
                        message = "Error: Permission denied. Please contact a mentor to resolve attendance issues.";
                        embed.setColor(Color.RED);
                    }

                    break;

                case "add_date":
                    if((getAdmins()).contains(username)){
                        String timeFrame;

                        option1 = event.getOption("date");
                        option2 = event.getOption("division");
                        option3 = event.getOption("time");

                        date = option1 != null ? option1.getAsString() : "";
                        division = option2 != null ? option2.getAsString() : "";
                        timeFrame = option3 != null ? option3.getAsString() : "";

                        try {
                            date = getFormattedDate(date);
                            timeFrame = getFormattedTime(timeFrame);

                            statement.executeUpdate("ALTER TABLE " + division + " ADD " + date + " varchar(50);");
                            statement.executeUpdate("UPDATE " + division + " SET " + date + " = '" + timeFrame + "' WHERE memberName='time';");
                            message = "Success! " + date + " has been added to " + division;
                            embed.setColor(Color.GREEN);
                        } catch (Exception e) {
                            message = "Error: Unable to add date. Please try again and make sure the command is in the right format.";
                            embed.setColor(Color.RED);
                            e.printStackTrace();
                        }
                    } else {
                        message = "Error: Permission denied. Please contact a mentor to change dates.";
                        embed.setColor(Color.RED);
                    }
    
                    break;

                case "remove_date":
                    if((getAdmins()).contains(username)){
                        option1 = event.getOption("date");
                        option2 = event.getOption("division");

                        date = option1 != null ? option1.getAsString() : "";
                        division = option2 != null ? option2.getAsString() : "";

                        try {
                            date = getFormattedDate(date);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if((getAllDivisions()).contains(division) && getDivisionDates(division).contains(date)){
                            try {
                                statement.executeUpdate("ALTER TABLE " + division + " DROP COLUMN " +  date);

                                message = "Success! " + date + " has been removed from " + division;
                                embed.setColor(Color.GREEN);
                            } catch (Exception e) {
                                message = "Error: Unable to add date. Please try again and make sure the command is in the right format.";
                                embed.setColor(Color.RED);
                                e.printStackTrace();
                            }
                        } else {
                            message = "Error: Format error. Please check your spelling of the command and try again.";
                            embed.setColor(Color.RED);
                        }
                    } else {
                        message = "Error: Permission denied. Please contact a mentor to change dates.";
                        embed.setColor(Color.RED);
                    }
    
                    break;

                case "add_member":
                    if((getAdmins()).contains(username)){
                        ArrayList<String> unaffectedDivisions = new ArrayList<>();
                        String divisionsString;

                        option1 = event.getOption("member");
                        option2 = event.getOption("divisions");

                        member = option1 != null ? option1.getAsString() : "nothing";
                        divisionsString = option2 != null ? option2.getAsString() : "nothing";
    
                        // splits list into the seperate divisions
                        variableDivisionsArray = new ArrayList<>(Arrays.asList(divisionsString.split(" ")));
                        variableDivisionsArray.add(0, "allteam");

                        System.out.println(variableDivisionsArray.toString());
                        variableDivisionsArray.remove("nothing");
                        System.out.println(variableDivisionsArray.toString());
                        
                        for (String divisionInArray : variableDivisionsArray) {
                            try {
                                statement.executeUpdate("INSERT INTO " + divisionInArray +" (memberName) VALUES (\"" + member + "\");");
                            } catch (Exception e) {
                                unaffectedDivisions.add(divisionInArray);
                            }
                        }
    
                        if(unaffectedDivisions.size() > 0){
                            message = member + " has been added to the database. \nHowever, they were not added to " + unaffectedDivisions + " as these divisions don't exist.";
                            embed.setColor(Color.YELLOW);
                        } else {
                            message = "Success! " + member + " has been added to the database.";
                            embed.setColor(Color.GREEN);
                        }
                        
                    } else {
                        message = "Error: Permission denied. Please contact a mentor to add members.";
                        embed.setColor(Color.RED);
                    }
                    break;

                case "remove_member":
                    if((getAdmins()).contains(username)){
                        option1 = event.getOption("member");
                        member = option1 != null ? option1.getAsString() : "";
    
                        variableDivisionsArray = getMemberDivisions(member);

                        if((getMembers()).contains(member)){
                            for(String divisionInArray : variableDivisionsArray){
                                try {
                                    statement.executeUpdate("DELETE FROM " + divisionInArray + " WHERE memberName=\"" + member + "\"");

                                    message = "Success! " + member + " has been removed from the datebase";
                                    embed.setColor(Color.GREEN);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    message = "Error: Something went wrong. Please try again.";
                                    embed.setColor(Color.RED);
                                }
                            }
                        } else {
                            message = "Error: Member not found in database. Please try again.";
                            embed.setColor(Color.RED);
                        }
                    } else {
                        message = "Error: Permission denied. Please contact a mentor to remove members.";
                        embed.setColor(Color.RED);
                    }
                    break;

                case "add_div":
                    if((getAdmins()).contains(username)){
                        option1 = event.getOption("member");
                        option2 = event.getOption("division");

                        member = option1 != null ? option1.getAsString() : "";
                        division = option2 != null ? option2.getAsString() : "";
    
                        if((getMembers()).contains(member)){
                            try {
                                statement.executeUpdate("INSERT INTO " + division +" (memberName) VALUES (\"" + member + "\");");
                                message = "Success! " + member + " has been added to " + division;
                                embed.setColor(Color.GREEN);
                            } catch (Exception e) {
                                e.printStackTrace();
                                message = "Error: Unable to add user to that division. Please check your spelling and try again.";
                                embed.setColor(Color.RED);
                            }
                        } else {
                            message = "Error: Member doesn't exist. Please check your spelling and try again.";
                            embed.setColor(Color.RED);
                        }
                    } else {
                        message = "Error: Permission denied. Please contact a mentor to edit divisions";
                        embed.setColor(Color.RED);
                    }
    
                    break;
    
                case "remove_div":
                    if((getAdmins()).contains(username)){
                        int rowsAffected = 0;

                        option1 = event.getOption("member");
                        option2 = event.getOption("division");

                        member = option1 != null ? option1.getAsString() : "";
                        division = option2 != null ? option2.getAsString() : "";
    
                        if((getMembers()).contains(member)){
                            try {
                                statement.executeUpdate("DELETE FROM " + division + " WHERE memberName=\"" + member + "\"");
    
                                resultSet = statement.executeQuery("SELECT ROW_COUNT();");
                                resultSet.next();
                                rowsAffected = (resultSet.getInt("ROW_COUNT()"));
    
                                if(rowsAffected == 1){
                                    message = "Success! " + member + " has been removed from " + division + ".";
                                    embed.setColor(Color.GREEN);
                                } else {
                                    throw new Exception();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                message = "Error: Unable to remove user from the division. Check your spelling and try again.";
                                embed.setColor(Color.RED);
                            } 
                        } else {
                            message = "Error: Member doesn't exist. Please check your spelling and try again.";
                            embed.setColor(Color.RED);
                        }
                    } else {
                        message = "Error: Permission denied. Please contact a mentor to edit divisions";
                        embed.setColor(Color.RED);
                    }
                    break;

                case "get_member_divs":
                    option1 = event.getOption("member");

                    member = option1 != null ? option1.getAsString() : "";

                    if((member.isEmpty())){
                        variableDivisionsArray = getMemberDivisions(username);
                        message = "These are your divisions: \n" + variableDivisionsArray;
                        embed.setColor(Color.LIGHT_GRAY);
                    } else {
                        variableDivisionsArray = getMemberDivisions(member);
                        message = "These are " + member + "'s divisions: \n" + variableDivisionsArray;
                        embed.setColor(Color.LIGHT_GRAY);
                    }

                break;
                    
                case "export":
                    try {
                        String csvFilePath = "C:/Users/18163/Music/Cowlendar v2.0/Cowlendar.txt";
                        File file = new File("C:/Users/18163/Music/Cowlendar v2.0/Cowlendar.txt");
                        FileUpload fileUpload = FileUpload.fromData(file);
                        FileWriter fileWriter = new FileWriter(csvFilePath);

                        option1 = event.getOption("division");
                        division = option1 != null ? option1.getAsString() : "";

                        System.out.println("SELECT * FROM " + division + ";");

                        resultSet = statement.executeQuery("SELECT * FROM " + division + ";");
    
                        // Get column names
                        int columnCount = resultSet.getMetaData().getColumnCount();
                        
                        for (int i = 1; i <= columnCount; i++) {
                            fileWriter.append(resultSet.getMetaData().getColumnName(i));
                            if (i < columnCount) {
                                fileWriter.append(",");
                            }
                        }
                        fileWriter.append("\n");
            
                        // Get row data
                        while (resultSet.next()) {
                            for (int i = 1; i <= columnCount; i++) {
                                fileWriter.append(resultSet.getString(i));
                                if (i < columnCount) {
                                    fileWriter.append(",");
                                }
                            }
                            fileWriter.append("\n");
                        }
         
                        fileWriter.close();

                        message = "CSV file created successfully!";
                        embed.setColor(Color.GREEN);

                        event.getChannel().sendFiles(Collections.singleton(fileUpload)).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                        message = "There was a problem with exporting the data, please try again.";
                        embed.setColor(Color.RED);
                    }

                break;
            }
        }

        embed.setFooter("@" + username + ": /" + command);

        embed.setTitle(message);
        System.out.println(message);

        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();

        embed.clear();
    }

    public static String getFormattedTime(String time) throws Exception{
        String[] timeList = time.split("-");
        String[] timeOneSeperator = timeList[0].split(":");
        String[] timeTwoSeperator = timeList[1].split(":");
        String timeOneString;
        String timeTwoString;
        LocalTime localTimeObject;

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma");

        // adds a 0 to the beginning of the hour if it was forgotten
        timeOneSeperator[0] = timeOneSeperator[0].length() != 1 ? timeOneSeperator[0] : "0" + timeOneSeperator[0];
        timeTwoSeperator[0] = timeTwoSeperator[0].length() != 1 ? timeTwoSeperator[0] : "0" + timeTwoSeperator[0];

        // removes space in between the minutes and the am/pm declaration if added
        timeOneSeperator[1] = timeOneSeperator[1].replaceAll("\\s", "");
        timeTwoSeperator[1] = timeTwoSeperator[1].replaceAll("\\s", "");

        timeOneString = timeOneSeperator[0] + ":" + timeOneSeperator[1];
        localTimeObject = LocalTime.parse(timeOneString.toUpperCase().replaceAll("\\s", ""), timeFormatter);

        timeTwoString = timeTwoSeperator[0] + ":" + timeTwoSeperator[1];
        localTimeObject = LocalTime.parse(timeTwoString.toUpperCase().replaceAll("\\s", ""), timeFormatter);

        time = timeOneSeperator[0] + ":" + timeOneSeperator[1] + "-" + timeTwoSeperator[0] + ":" + timeTwoSeperator[1];

        return time;
    }

    public static String getFormattedDate(String date) throws Exception{
        date = date.replaceAll("/", "_");
        date = date.replaceAll("-", "_");

        String[] dateSeperator = date.split("_");
        dateSeperator[0] = dateSeperator[0].length() != 1 ? dateSeperator[0] : "0" + dateSeperator[0];
        dateSeperator[1] = dateSeperator[1].length() != 1 ? dateSeperator[1] : "0" + dateSeperator[1];

        date = (dateSeperator[0] + "_" + dateSeperator[1] + "_" + dateSeperator[2]).toUpperCase();

        return date;
    }

    public static ArrayList<String> getMemberDivisions(String username){
        ArrayList<String> allDivisions = new ArrayList<>();
        ArrayList<String> memberDivisions = new ArrayList<>();
        int rowsAffected = 0;

        allDivisions = getAllDivisions();

        for(String division : allDivisions){
            try {
                resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + division + " WHERE memberName=\"" + username + "\";");
                resultSet.next();
                rowsAffected = (resultSet.getInt("COUNT(*)"));

                if(rowsAffected == 1){
                    memberDivisions.add(division);
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                continue;
            }
        }

        return memberDivisions;
    }

    public static ArrayList<String> getAllDivisions(){
        ArrayList<String> allDivisions = new ArrayList<>();

        try {
            resultSet = statement.executeQuery("SHOW TABLES");

            while (resultSet.next()) {
                allDivisions.add(resultSet.getString("Tables_in_mydb"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allDivisions;
    }

    public static HashSet<String> getMemberDates(ArrayList<String> divisionArray){
        String unconfirmedDateString;
        LocalDate unconfirmedDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_dd_yy");
        ArrayList<String> allDatesArray = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        try {
            for(String division : divisionArray){
                resultSet = statement.executeQuery("SHOW COLUMNS FROM mydb." + division + ";");
        
                while (resultSet.next()) {
                    unconfirmedDateString = resultSet.getString("Field");
                    
                    try {
                        unconfirmedDate = LocalDate.parse(unconfirmedDateString, formatter);
                    } catch (Exception e) {
                        continue;
                    }
    
                    if (unconfirmedDate.isBefore(currentDate) || unconfirmedDate.equals(currentDate)) {
                        allDatesArray.add(unconfirmedDateString);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashSet<String> updatedDatesSet = new HashSet<String>(allDatesArray);
        updatedDatesSet.remove("memberName");

        return updatedDatesSet;
    }

    public static ArrayList<String> getDivisionDates(String division){
        ArrayList<String> datesArray = new ArrayList<>();

        try {
            resultSet = statement.executeQuery("SHOW COLUMNS FROM mydb." + division + ";");
    
            while (resultSet.next()) {
                datesArray.add(resultSet.getString("Field"));
            }
            datesArray.remove("memberName");
            } catch (Exception e) {
                e.printStackTrace();
            }

        return datesArray;
    }

    public static ArrayList<String> getMembers(){
        ArrayList<String> nameList = new ArrayList<>();

        try {
            resultSet = statement.executeQuery("select memberName from allteam");

            while (resultSet.next()) {
                nameList.add(resultSet.getString("memberName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nameList;
    }

    public static ArrayList<String> getAdmins(){
        ArrayList<String> nameList = new ArrayList<>();

        try {
            resultSet = statement.executeQuery("select memberName from admin");

            while (resultSet.next()) {
                nameList.add(resultSet.getString("memberName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nameList;
    }

    public static void initializeDatabase(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "",
                "", ""
            );

            connection.setNetworkTimeout(Executors.newFixedThreadPool(1), 0);
            System.out.println(connection.getNetworkTimeout());

            System.out.println("Success: Connection found!");
        } catch (Exception e) {
            System.out.println("Error: Connection not found!");
            e.printStackTrace();
        }

        try {
            statement = connection.createStatement();
            System.out.println("Success: Statement created!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Statement not created!");
        }
    }
}