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
                // jda.upsertCommand("login","log in with a date and time").addOption(OptionType.STRING, "time", "example: 02:20pm-04:30pm", true).addOption(OptionType.STRING, "date", "example: 01_25_24", false).queue();
                // jda.upsertCommand("impersonate","log in for someone else with a date and time").addOption(OptionType.STRING, "member", "member being impersonated", true).addOption(OptionType.STRING, "time", "time you got here and time you left (example: 02:20pm-04:30pm)", true).addOption(OptionType.STRING, "date", "example: 01_25_24", false).queue();
                // jda.upsertCommand("percent","check attendance rates for a division").addOption(OptionType.STRING, "division", "name of division (singular)", true).addOption(OptionType.STRING, "member", "name of member", false).queue();
                // jda.upsertCommand("get_dates","see all of the dates for a division").addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("member_list","see all of the members currently on the database").queue();
                // jda.upsertCommand("division_list","see all of the divisions currently on the database").queue();
                // jda.upsertCommand("resolve","revoke member's attendance for a date").addOption(OptionType.STRING, "date", "example: 01_25_24", true).addOption(OptionType.STRING, "member", "name of member", true).queue();
                // jda.upsertCommand("add_date","add a date for a division").addOption(OptionType.STRING, "date", "example: 01_25_24", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).addOption(OptionType.STRING, "time", "time you got here and time you left (example: 02:20pm-04:30pm)", true).queue();
                // jda.upsertCommand("remove_date","remove a date for a division (\"everyone\" is all team)").addOption(OptionType.STRING, "date", "example: 01_25_24", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("add_member","add member to database with divisions (\"allteam\" already included)").addOption(OptionType.STRING, "member", "name of member", true).addOption(OptionType.STRING, "divisions", "name of divisions (plural) (ex. cad programming)", false).queue();
                // jda.upsertCommand("remove_member","removes member from database").addOption(OptionType.STRING, "member", "name of member", true).queue();
                // jda.upsertCommand("add_div","add member to division").addOption(OptionType.STRING, "member", "name of member", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("remove_div","remove member from division").addOption(OptionType.STRING, "member", "name of member", true).addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();
                // jda.upsertCommand("get_member_divs","returns a list of divisions a member is in").addOption(OptionType.STRING, "member", "name of member", true).queue();
                // jda.upsertCommand("export","export a division's data as a csv file").addOption(OptionType.STRING, "division", "name of division (singular)", true).queue();

                // jda.deleteCommandById("1287615710415294476").queue();
                // jda.deleteCommandById("1289022793085616244").queue();
            } catch(Exception e) {
                e.printStackTrace();
            }
    }
}