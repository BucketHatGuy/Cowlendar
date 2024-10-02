package roboticsattendance;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;


public class Cowlendar extends ListenerAdapter {
    static Connection connection = null;
    static Statement statement;
    static ResultSet resultSet;

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event){
        String command = event.getName();
        String username = event.getUser().getName();
        ArrayList<String> divisionArray;
        HashSet<String> updatedDatesSet = getMemberDates(getMemberDivisions(username));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_dd_yy");  
        LocalDate currentDate = LocalDate.now();
        String message = "";
        OptionMapping option1;
        OptionMapping option2;
        EmbedBuilder embed = new EmbedBuilder();

        divisionArray = getMemberDivisions(username);

        event.deferReply().queue();

        if(!(getMembers()).contains(username)){
            message = "Error: You weren't found in the database. Please contact a mentor to get added.";
            embed.setColor(Color.RED);
        } else {
            switch (command) {
                case "log_meet":
                    String attemptedDate;
                    int divisionsLoggedInFor;
                    String day;

                    option1 = event.getOption("date");
                    day = option1 != null ? option1.getAsString() : "";

                    attemptedDate = !(day == "") ? day : currentDate.format(formatter).toString();
                    divisionsLoggedInFor = 0;

                    System.out.println("attemptedDate is: " + attemptedDate);
                    System.out.println(updatedDatesSet.contains(attemptedDate));

                    if(updatedDatesSet.contains(attemptedDate)){
                        for (String division : divisionArray) {
                            try {
                                statement.executeUpdate("UPDATE " + division + " SET " + attemptedDate + " = 1 WHERE memberName='" + username + "';");
                                divisionsLoggedInFor++;
                            } catch (Exception e) {
                                continue;
                            }
                        }
    
                        if(divisionsLoggedInFor == 0){
                            message = "Error: Unknown. If this persists, contact Jose probably lol.";
                            embed.setColor(Color.RED);
                        } else {
                            message = "Success! You have logged in for the meeting on " + attemptedDate;
                            embed.setColor(Color.GREEN);
                        }

                    } else {
                        message = "Error: Invalid date. Check the date you entered and try again.";
                        embed.setColor(Color.RED);
                    }
                break;

                case "log_other":
                    String realDate;
                    String notRealDate;

                    option1 = event.getOption("date");
                    notRealDate = option1 != null ? option1.getAsString() : "";

                    realDate = !(notRealDate == "") ? notRealDate : currentDate.format(formatter).toString();

                    System.out.println("attemptedDate is: " + realDate);

                    if((getDivisionDates("other")).contains(realDate)){
                        try {
                            statement.executeUpdate("UPDATE other SET " + realDate + " = 1 WHERE memberName='" + username + "';");
                            message = "Success! You have logged for the event on " + realDate;
                            embed.setColor(Color.GREEN);
                        } catch (Exception e) {
                            message = "Error: Unknown. If this persists, contact Jose probably lol.";
                            embed.setColor(Color.RED);
                            e.printStackTrace();
                        }
                    } else {
                        message = "Error: Invalid date. Check the date you entered and try again.";
                        embed.setColor(Color.RED);
                    }
                break;

                case "percent":
                    ArrayList<Double> attendanceArray = new ArrayList<>();
                    Double totalNumber = 0.0;
                    int dateCheck = 0;
                    String attemptedMember;

                    option1 = event.getOption("member");
                    attemptedMember = option1 != null ? option1.getAsString() : "";

                    if(!(attemptedMember == "")){
                        if((getAdmins()).contains(username)){
                            if((getMembers()).contains(attemptedMember)){
                                for (String date : updatedDatesSet) {
                                    for (String division : divisionArray) {
                                        try {
                                            resultSet = statement.executeQuery("select " + date + " from " + division + " where memberName = '" + attemptedMember + "';");
                                            resultSet.next();
                                            dateCheck = resultSet.getInt(date);
                                        } catch (Exception e) {
                                            // null check. if it errors here, almost all of the time it's because the value it returns is null.
                                            dateCheck = 0;
                                        }
            
                                        if(dateCheck == 1){
                                            attendanceArray.add(1.0);
                                            break;
                                        }
            
                                        if(division == divisionArray.getLast()){
                                            attendanceArray.add(0.0);
                                            break;
                                        }
                                    }
                                }
                                for(Double value : attendanceArray){
                                    totalNumber += value;
                                }

                                message = attemptedMember + "'s attendance rate is: " + Math.round((totalNumber / attendanceArray.size()) * 100) + "%";
                                embed.setColor(Color.LIGHT_GRAY);
                            } else {
                                message = "Error: Member not found. Please check the spelling and try again";
                                embed.setColor(Color.RED);
                            }
                        } else {
                            message = "Error: Permission denied. Only admins are allowed to check other member's attendance rates.";
                            embed.setColor(Color.RED);
                        }
                    } else {
                        for (String date : updatedDatesSet) {
                            for (String division : divisionArray) {
                                try {
                                    resultSet = statement.executeQuery("select " + date + " from " + division + " where memberName = '" + username + "';");
                                    resultSet.next();
                                    dateCheck = resultSet.getInt(date);
                                } catch (Exception e) {
                                    // null check. if it errors here, almost all of the time it's because the value it returns is null.
                                    dateCheck = 0;
                                }
    
                                if(dateCheck == 1){
                                    attendanceArray.add(1.0);
                                    break;
                                }
    
                                if(division == divisionArray.getLast()){
                                    attendanceArray.add(0.0);
                                    break;
                                }
                            }
                        }
                        for(Double value : attendanceArray){
                            totalNumber += value;
                        }

                        message = "Your attendance rate is: " + Math.round((totalNumber / attendanceArray.size()) * 100) + "%";
                        embed.setColor(Color.LIGHT_GRAY);
                    }

                    break;

                case "get_dates":
                    ArrayList<String> datesArray = new ArrayList<>();
                    String attemptedDivision;

                    option1 = event.getOption("division");
                    attemptedDivision = option1 != null ? option1.getAsString() : "";

                    datesArray = getDivisionDates(attemptedDivision);

                    if(!(datesArray.isEmpty())){
                        message = "The following dates are on " + attemptedDivision + ": ";
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
                        int divisionsResolvedFor = 0;
                        String date = "";
                        String member = "";
                        boolean isOther = false;

                        option1 = event.getOption("date");
                        option2 = event.getOption("member");
                        OptionMapping option3 = event.getOption("other");

                        date = option1 != null ? option1.getAsString() : "";
                        member = option2 != null ? option2.getAsString() : "";
                        isOther = option3 != null ? option3.getAsBoolean() : false;

                        if(!isOther){
                            if(updatedDatesSet.contains(date)){
                                for (String division : divisionArray) {
                                    try {
                                        statement.executeUpdate("UPDATE " + division + " SET " + date + " = 0 WHERE memberName='" + member + "';");
                                        divisionsResolvedFor++;
                                    } catch (Exception e) {
                                        continue;
                                    }
                                }
        
                                if(divisionsResolvedFor == 0){
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
                        } else {
                            if(getDivisionDates("other").contains(date)){
                                try {
                                    statement.executeUpdate("UPDATE " + "other" + " SET " + date + " = 0 WHERE memberName='" + member + "';");
                                    message = "Success! " + member + "'s attendance has been resolved for " + date;
                                    embed.setColor(Color.GREEN);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    message = "Error: Unknown. If this persists, contact Jose probably lol.";
                                    embed.setColor(Color.RED);
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
                        String date;
                        String division;

                        option1 = event.getOption("date");
                        option2 = event.getOption("division");

                        date = option1 != null ? option1.getAsString() : "";
                        division = option2 != null ? option2.getAsString() : "";

                            try {
                                statement.executeUpdate("ALTER TABLE " + division + " ADD " + date + " tinyint(1);");
                                message = "Success! " + date + " has been added to " + division;
                                embed.setColor(Color.GREEN);
                            } catch (Exception e) {
                                message = "Error: Unable to add date. Please try again and make sure the command is in the right format. \n";
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
                        String date;
                        String division;

                        option1 = event.getOption("date");
                        option2 = event.getOption("division");

                        date = option1 != null ? option1.getAsString() : "";
                        division = option2 != null ? option2.getAsString() : "";

                        if((getAllDivisions()).contains(division) && getDivisionDates(division).contains(date)){
                            try {
                                statement.executeUpdate("SET SQL_SAFE_UPDATES = 0;");
                                statement.executeUpdate("ALTER TABLE " + division + " DROP COLUMN " +  date);
                                statement.executeUpdate("SET SQL_SAFE_UPDATES = 1;");

                                message = "Success! " + date + " has been removed from " + division;
                                embed.setColor(Color.GREEN);
                            } catch (Exception e) {
                                message = "Error: Unknown. Please try again. \n";
                                embed.setColor(Color.RED);
                                e.printStackTrace();
                            }
                        } else {
                            message = "Error: Unable to remove date. Please check the format of the command and try again.";
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
                        ArrayList<String> divisionsArray;
                        String member;
                        String divisions;

                        option1 = event.getOption("member");
                        option2 = event.getOption("divisions");

                        member = option1 != null ? option1.getAsString() : "nothing";
                        divisions = option2 != null ? option2.getAsString() : "nothing";
    
                        // splits list into the seperate divisions
                        divisionsArray = new ArrayList<>(Arrays.asList(divisions.split(" ")));
                        divisionsArray.add(0, "allteam");
                        divisionsArray.add(0, "other");

                        System.out.println(divisionsArray.toString());
                        divisionsArray.remove("nothing");
                        System.out.println(divisionsArray.toString());
                        
                        for (String division : divisionsArray) {
                            try {
                                statement.executeUpdate("INSERT INTO " + division +" (memberName) VALUES (\"" + member + "\");");
                            } catch (Exception e) {
                                unaffectedDivisions.add(division);
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
                        ArrayList<String> divisions = new ArrayList<>();
                        String member;
                        int rowsAffected = 0;

                        option1 = event.getOption("member");
                        member = option1 != null ? option1.getAsString() : "";
    
                        divisions = getMemberDivisions(member);
                        divisions.add("other");

                        if((getMembers()).contains(member)){
                            for(String division : divisions){
                                try {
                                    statement.executeUpdate("SET SQL_SAFE_UPDATES = 0;");

                                    statement.executeUpdate("DELETE FROM " + division + " WHERE memberName=\"" + member + "\"");

                                    resultSet = statement.executeQuery("SELECT ROW_COUNT();");
                                    resultSet.next();
                                    rowsAffected = (resultSet.getInt("ROW_COUNT()"));

                                    statement.executeUpdate("SET SQL_SAFE_UPDATES = 1;");

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
                        String member;
                        String division;

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
                        String member;
                        String division;
                        int rowsAffected = 0;

                        option1 = event.getOption("member");
                        option2 = event.getOption("division");

                        member = option1 != null ? option1.getAsString() : "";
                        division = option2 != null ? option2.getAsString() : "";
    
                        if((getMembers()).contains(member)){
                            try {
                                statement.executeUpdate("SET SQL_SAFE_UPDATES = 0;");
                                statement.executeUpdate("DELETE FROM " + division + " WHERE memberName=\"" + member + "\"");
    
                                resultSet = statement.executeQuery("SELECT ROW_COUNT();");
                                resultSet.next();
                                rowsAffected = (resultSet.getInt("ROW_COUNT()"));
    
                                statement.executeUpdate("SET SQL_SAFE_UPDATES = 1;");
    
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
                    ArrayList<String> divisions = new ArrayList<>();
                    String member;
                    option1 = event.getOption("member");

                    member = option1 != null ? option1.getAsString() : "";

                    if((member.isEmpty())){
                        divisions = getMemberDivisions(username);
                        message = "These are your divisions: \n" + divisions;
                        embed.setColor(Color.LIGHT_GRAY);
                    } else {
                        divisions = getMemberDivisions(member);
                        message = "These are " + member + "'s divisions: \n" + divisions;
                        embed.setColor(Color.LIGHT_GRAY);
                    }
            }
            // System.out.println("Message is supposed to be: " + message);
            // event.getHook().sendMessage(message).setEphemeral(true).queue();
        }

        embed.setFooter("@" + username + ": /" + command);

        embed.setTitle(message);
        System.out.println(message);

        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();

        embed.clear();
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

        memberDivisions.remove("other");

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