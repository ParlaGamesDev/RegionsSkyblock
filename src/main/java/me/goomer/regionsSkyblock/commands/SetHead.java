package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHead extends SubCommand {

    private RegionsSkyblock plugin;

    public SetHead(RegionsSkyblock plugin){
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if(strings.length>1){
            plugin.getConfig().set("head",strings[1]);
            plugin.saveConfig();
            plugin.getStarManager().refreshAppearance();
            send(sender, "Done");
            return;
        }
        send(sender, "Not enough arguments");
    }

    @Override
    public String getName() {
        return "sethead";
    }

    @Override
    public String getSyntax() {
        return "/regions sethead <link>";
    }

    @Override
    public String getDesc() {
        return "set the head link for the star";
    }
}
