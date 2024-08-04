package io.github.bindglam.addere.listeners;

import io.github.bindglam.addere.AdderePlugin;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

public class PlayerListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(UUID.randomUUID().toString().getBytes());
        byte[] hash = md.digest();
        StringBuilder strBuilder = new StringBuilder();
        for (byte b : hash) {
            strBuilder.append(String.format("%02x", b));
        }

        String ip = null;
        if(AdderePlugin.INSTANCE.getPlugin().getConfig().getBoolean("self-host.enabled")) {
            ip = "http://" + AdderePlugin.INSTANCE.getPlugin().getConfig().getString("self-host.ip") + ":" + AdderePlugin.INSTANCE.getPlugin().getConfig().getInt("self-host.port") + "/resourcepack.zip";
        } else if(AdderePlugin.INSTANCE.getPlugin().getConfig().getBoolean("external-host.enabled")){
            ip = AdderePlugin.INSTANCE.getPlugin().getConfig().getString("external-host.ip");
        }

        if(ip != null) {
            player.sendResourcePacks(ResourcePackRequest.resourcePackRequest().packs(
                    ResourcePackInfo.resourcePackInfo().uri(URI.create(ip)).hash(strBuilder.toString())
            ).required(true).prompt(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(AdderePlugin.INSTANCE.getPlugin().getConfig().getString("resourcepack.prompt")))).build());
        }
    }
}
