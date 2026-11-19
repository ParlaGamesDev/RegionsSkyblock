package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.CommandSender;

public class AddFarm extends SubCommand  {

    private RegionsSkyblock plugin;

    public AddFarm(RegionsSkyblock plugin) {
        this.plugin = plugin;
    }

    // /addfarm <key> <block> <minDelay> <maxDelay> [starX] [starY] [starZ] [world]

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (strings.length >= 7) {
            try {
                String key = strings[2];
                String block = strings[3];
                int minDelay = Integer.parseInt(strings[4]);
                int maxDelay = Integer.parseInt(strings[5]);

                plugin.getConfig().set("farms." + key + ".block", block);
                plugin.getConfig().set("farms." + key + ".minDelay", minDelay);
                plugin.getConfig().set("farms." + key + ".maxDelay", maxDelay);

                if (strings.length >= 10) {
                    int starX = Integer.parseInt(strings[6]);
                    int starY = Integer.parseInt(strings[7]);
                    int starZ = Integer.parseInt(strings[8]);
                    String world = strings[9];

                    plugin.getConfig().set("farms." + key + ".star.x", starX);
                    plugin.getConfig().set("farms." + key + ".star.y", starY);
                    plugin.getConfig().set("farms." + key + ".star.z", starZ);
                    plugin.getConfig().set("farms." + key + ".star.world", world);

                    plugin.addStar(key,starX, starY, starZ, world);
                }

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
        return "farm";
    }

    @Override
    public String getSyntax() {
        return "/regions add farm <key> <block> <minDelay> <maxDelay> [starX] [starY] [starZ] [world]";
    }

    @Override
    public String getDesc() {
        return "add a new farm region";
    }
}
