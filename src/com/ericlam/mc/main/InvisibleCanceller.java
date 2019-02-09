package com.ericlam.mc.main;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class InvisibleCanceller extends JavaPlugin implements Listener, CommandExecutor {
    private String cancel,reload_success,point_success,point_removed,no_perm,no_players,point_player;
    private int nearest_size;
    private Location compassDefaultLoc;

    @Override
    public void onEnable() {
        this.getLogger().info("InvisibleCanceller Enabled.");
        this.getServer().getPluginManager().registerEvents(this,this);
        this.saveDefaultConfig();
        loadVar();
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e){
        if (e.getItem() == null || e.getItem().getType() == Material.AIR) return;
        Player player = e.getPlayer();
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.sendMessage(cancel);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if (e.getFrom() == e.getTo()) return;
        Player player = e.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
            player.sendMessage(cancel);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    private String translate(String path){
        return ChatColor.translateAlternateColorCodes('&',this.getConfig().getString(path));
    }

    private void loadVar(){
        cancel = translate("cancel");
        reload_success = translate("reload-success");
        point_success = translate("point-success");
        point_removed = translate("point-removed");
        no_perm = translate("no-perm");
        nearest_size = this.getConfig().getInt("nearest-size");
        no_players = translate("no-players");
        point_player = translate("point-player");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //console can do
        if (command.getName().equalsIgnoreCase("ic-reload")){
            if (!sender.hasPermission("ic.admin")) {
                sender.sendMessage(no_perm);
                return false;
            }
            this.reloadConfig();
            loadVar();
            sender.sendMessage(reload_success);
            return true;
        }
        //console can't do
        if (!(sender instanceof Player)){
            sender.sendMessage("not player");
            return false;
        }

        //only have perm
        if (!sender.hasPermission("ic.point")){
            sender.sendMessage(no_perm);
            return false;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("ic-point")){
            if (args.length < 3){
                player.sendMessage(ChatColor.GREEN + "/ic-point <x> <y> <z>");
                return false;
            }
            double x,y,z;
            try{
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
            }catch (NumberFormatException e){
                player.sendMessage(ChatColor.RED + "x, y 或者 z 輸入的不是數值！");
                return false;
            }
            if (compassDefaultLoc==null) compassDefaultLoc = player.getCompassTarget();
            Location loc = new Location(player.getWorld(), x,y,z);
            player.setCompassTarget(loc);
            player.sendMessage(point_success);
            return true;
        }

        if (command.getName().equalsIgnoreCase("ic-point-remove")){
            Location restore;
            if (compassDefaultLoc==null) restore = player.getCompassTarget();
            else restore = compassDefaultLoc;
            player.setCompassTarget(restore);
            player.sendMessage(point_removed);
            return true;
        }

        if (command.getName().equalsIgnoreCase("ic-point-nearest")){
            List<Entity> entites = player.getNearbyEntities(nearest_size,nearest_size,nearest_size);
            Player nearest_target = null;
            double nearest_distance = Double.MAX_VALUE;
            for (Entity entity : entites) {
                if (!(entity instanceof Player)) continue;

                Player target = (Player) entity;

                double distance = player.getLocation().distance(target.getLocation());

                if (distance < nearest_distance){
                    nearest_distance = distance;
                    nearest_target = target;
                }

            }
            if (nearest_target == null){
                player.sendMessage(no_players.replace("<size>",nearest_size+""));
                return false;
            }

            Location tarloc = nearest_target.getLocation();
            player.setCompassTarget(tarloc);
            player.sendMessage(point_player.replace("<player>",nearest_target.getName()));
            return true;
        }


        return false;
    }
}
