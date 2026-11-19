package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.CommandSender;

public class RemoveTree extends SubCommand {
    private RegionsSkyblock plugin;

    public RemoveTree(RegionsSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (strings.length >= 3) {
            String key = strings[2];
            plugin.getConfig().set("trees." + key,null);
            plugin.saveConfig();
            send(sender, "Removed " + key);
            return;
        }
        send(sender, "Not enough arguments");
    }

    @Override
    public String getName() {
        return "tree";
    }

    @Override
    public String getSyntax() {
        return "/regions remove tree <region>";
    }

    @Override
    public String getDesc() {
        return "remove a tree region";
    }
}
