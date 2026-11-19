package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.CommandSender;

public class RemoveFarm extends SubCommand {
    private RegionsSkyblock plugin;

    public RemoveFarm(RegionsSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (strings.length >= 3) {
            String key = strings[2];
            plugin.getConfig().set("farms." + key,null);
            plugin.removeStar(key);
            plugin.saveConfig();
            send(sender, "Removed " + key);
            return;
        }
        send(sender, "Not enough arguments");

    }

    @Override
    public String getName() {
        return "farm";
    }

    @Override
    public String getSyntax() {
        return "/regions remove farm <key>";
    }

    @Override
    public String getDesc() {
        return "remove a farm region";
    }
}
