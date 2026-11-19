package me.goomer.regionsSkyblock.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class SubCommand  {
    public abstract void execute(CommandSender sender, String[] strings);
    public abstract String getName();
    public abstract String getSyntax();
    public abstract String getDesc();
    public void send(CommandSender commandSender, String msg) {
        if (commandSender instanceof Player p) {
            p.sendMessage(msg);
        } else {
            System.out.println(msg);
        }
    }
}
