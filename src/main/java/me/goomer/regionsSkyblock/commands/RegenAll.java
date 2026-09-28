package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.CommandSender;

public class RegenAll extends SubCommand {

    private final RegionsSkyblock plugin;

    public RegenAll(RegionsSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        int restored = plugin.regenerateEverything();
        send(sender, "Regenerated " + restored + " pending trees/farms/mines");
    }

    @Override
    public String getName() {
        return "regenall";
    }

    @Override
    public String getSyntax() {
        return "/regions regenall";
    }

    @Override
    public String getDesc() {
        return "instantly regenerate every broken tree, farm crop and mine block";
    }
}
