package roboticsattendance;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Connectors {
        public static void main(String[] args) throws Exception{
        
        Cowlendar.initializeDatabase();

        JDA jda = (JDA) JDABuilder.createDefault("")
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .setActivity(Activity.competing("CowTown ThrowDown"))
            .addEventListeners(new Cowlendar())
            .build().awaitReady();

            try {
                // jda.upsertCommand("log_meet","log a day in which you were here").addOption(OptionType.STRING, "date", "format: MM_DD_YY", false).queue();
                // jda.upsertCommand("log_other","log a day in which you were here").addOption(OptionType.STRING, "date", "format: MM_DD_YY", false).queue();
                // jda.upsertCommand("percent","check your attendance rate").addOption(OptionType.STRING, "member", "name of member", false).queue();
                // jda.upsertCommand("get_dates","see all of the dates for a division").addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("member_list","see all of the members currently on the database").queue();
                // jda.upsertCommand("division_list","see all of the divisions currently on the database").queue();
                // jda.upsertCommand("resolve","change member's attendance from present to not for a date").addOption(OptionType.STRING, "date", "format: MM_DD_YY", true).addOption(OptionType.STRING, "member", "name of member", true).addOption(OptionType.BOOLEAN, "other", "is this for a non meeting", true).queue();
                // jda.upsertCommand("add_date","add a date for a division").addOption(OptionType.STRING, "date", "format: MM_DD_YY", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("remove_date","remove a date for a division (\"everyone\" is all team)").addOption(OptionType.STRING, "date", "format: MM_DD_YY", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("add_member","add member with divisions (\"everyone\" already included)").addOption(OptionType.STRING, "member", "name of member", true).addOption(OptionType.STRING, "divisions", "name of division (plural) (ex. cad programming)", false).queue();
                // jda.upsertCommand("remove_member","removes member from database").addOption(OptionType.STRING, "member", "name of member", true).queue();
                // jda.upsertCommand("add_div","add member to division").addOption(OptionType.STRING, "member", "name of member", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("remove_div","remove member from division").addOption(OptionType.STRING, "member", "name of member", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("get_member_divs","returns a list of divisions a member is in").addOption(OptionType.STRING, "member", "name of member", true).queue();

                jda.deleteCommandById("1287499890905055329").queue();
                jda.deleteCommandById("1287544404386644079").queue();
            } catch(Exception e) {
                e.printStackTrace();
            }
    }
}