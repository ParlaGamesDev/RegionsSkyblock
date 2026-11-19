package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.CommandSender;

public class AddTree extends SubCommand  {

    private RegionsSkyblock plugin;

    public AddTree(RegionsSkyblock plugin) {
        this.plugin = plugin;
    }

    // /addtree <key> <x1> <y1> <z1> <x2> <y2> <z2> <minDelay> <maxDelay> <world>

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (strings.length >= 12) {
            try {

                String key = strings[2];

                int x1 = Integer.parseInt(strings[3]);
                int y1 = Integer.parseInt(strings[4]);
                int z1 = Integer.parseInt(strings[5]);

                int x2 = Integer.parseInt(strings[6]);
                int y2 = Integer.parseInt(strings[7]);
                int z2 = Integer.parseInt(strings[8]);

                int minDelay = Integer.parseInt(strings[9]);
                int maxDelay = Integer.parseInt(strings[10]);

                String world = strings[11];

                plugin.getConfig().set("trees." + key + ".world", world);
                plugin.getConfig().set("trees." + key + ".minDelay", minDelay);
                plugin.getConfig().set("trees." + key + ".maxDelay", maxDelay);
                plugin.getConfig().set("trees." + key + ".loc1.x", x1);
                plugin.getConfig().set("trees." + key + ".loc1.y", y1);
                plugin.getConfig().set("trees." + key + ".loc1.z", z1);
                plugin.getConfig().set("trees." + key + ".loc2.x", x2);
                plugin.getConfig().set("trees." + key + ".loc2.y", y2);
                plugin.getConfig().set("trees." + key + ".loc2.z", z2);

                plugin.saveConfig();

                send(sender, "Added " + key);

            } catch (NumberFormatException e) {
                send(sender, "You didn't enter the arguments correctly");

            }

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
        return "/regions add tree <key> <x1> <y1> <z1> <x2> <y2> <z2> <minDelay> <maxDelay> <world>";
    }

    @Override
    public String getDesc() {
        return "add a new tree region";
    }
}
