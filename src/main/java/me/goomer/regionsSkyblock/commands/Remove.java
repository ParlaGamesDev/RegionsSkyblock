package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class Remove extends SubCommand  {
    private ArrayList<SubCommand> commands;

    public Remove(RegionsSkyblock plugin) {
        java.util.Objects.requireNonNull(plugin);
        commands = new ArrayList<>();
        commands.add(new RemoveFarm(plugin));
        commands.add(new RemoveMine(plugin));
        commands.add(new RemoveTree(plugin));
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
        return "remove";
    }

    @Override
    public String getSyntax() {
        return "/regions remove <region - type>";
    }

    @Override
    public String getDesc() {
        return "remove a region";
    }
}
