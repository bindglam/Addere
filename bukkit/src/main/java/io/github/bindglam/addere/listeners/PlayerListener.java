package io.github.bindglam.addere.listeners;

import io.github.bindglam.addere.AdderePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(AdderePlugin.INSTANCE.getPlugin().getConfig().getBoolean("self-host.enabled")) {
            player.setResourcePack("http://" + AdderePlugin.INSTANCE.getPlugin().getConfig().getString("self-host.ip") + ":" + AdderePlugin.INSTANCE.getPlugin().getConfig().getInt("self-host.port") + "/resourcepack.zip");
        }
    }
}
