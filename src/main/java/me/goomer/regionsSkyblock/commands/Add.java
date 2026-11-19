package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Add extends SubCommand {

    private ArrayList<SubCommand> commands;

    public Add(RegionsSkyblock plugin) {
        java.util.Objects.requireNonNull(plugin);
        commands = new ArrayList<>();
        commands.add(new AddFarm(plugin));
        commands.add(new AddMine(plugin));
        commands.add(new AddTree(plugin));

    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (strings.length > 1) {
            for (SubCommand cmd : commands) {
                if (cmd.getName().equals(strings[1])) {
                    cmd.execute(sender, strings);
                }
            }
        } else {
            for (SubCommand cmd : commands) {
                send(sender, cmd.getSyntax() + " - " + cmd.getDesc());
            }
        }
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getSyntax() {
        return "/regions add <region - type>";
    }

    @Override
    public String getDesc() {
        return "add a new region";
    }

    public void send(CommandSender commandSender, String msg) {
        if (commandSender instanceof Player p) {
            p.sendMessage(msg);
        } else {
            System.out.println(msg);
        }
    }
}
