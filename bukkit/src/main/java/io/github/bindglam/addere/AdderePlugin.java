package io.github.bindglam.addere;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import io.github.bindglam.addere.addons.AddereAddon;
import io.github.bindglam.addere.api.Addere;
import io.github.bindglam.addere.api.items.CustomItem;
import io.github.bindglam.addere.items.CustomItemManager;
import io.github.bindglam.addere.listeners.PlayerListener;
import io.github.bindglam.addere.loader.AddereAddonLoader;
import io.github.bindglam.addere.pack.PackGenerator;
import io.github.bindglam.addere.pack.WebServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AdderePlugin extends JavaPlugin implements Addere {
    public static Addere INSTANCE;

    private final AddereAddonLoader loader = new AddereAddonLoader();
    private final CustomItemManager itemManager = new CustomItemManager();

    @Override
    public void onLoad() {
        INSTANCE = this;
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true));

        registerCommands();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        if(!isFolia()){
            getLogger().warning("---------------------");
            getLogger().warning("You are not in Folia!");
            getLogger().warning("We can't support you!");
            getLogger().warning("---------------------");
            getLogger().info("Server Info : NOT Folia( " + getServerVersion() + " )");
        } else {
            getLogger().info("Server Info : Folia( " + getServerVersion() + " )");
        }
        getLogger().info("Addere initializing...");

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        LangManager.init();

        loader.loadAll();

        saveDefaultConfig();

        Bukkit.getAsyncScheduler().runNow(this, new WebServer());

        Bukkit.getAsyncScheduler().runDelayed(this, (task) -> PackGenerator.generate(), 2L, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        WebServer.close();
        saveConfig();
    }

    private void registerCommands(){
        new CommandAPICommand("addere")
                .withPermission(CommandPermission.OP)
                .withSubcommands(
                        new CommandAPICommand("reload")
                                .executes((sender, args) -> {
                                    sender.sendMessage(MiniMessage.miniMessage().deserialize(LangManager.get("reloading")));
                                    for(AddereAddon addon : getLoader().getAddons()) {
                                        getLoader().unload(addon);
                                    }
                                    getLoader().loadAll();
                                    PackGenerator.generate();
                                    sender.sendMessage(MiniMessage.miniMessage().deserialize(LangManager.get("reload_complete")));
                                }),
                        new CommandAPICommand("items")
                                .withSubcommands(
                                        new CommandAPICommand("get")
                                                .withArguments(new NamespacedKeyArgument("key"))
                                                .executesPlayer((player, args) -> {
                                                    NamespacedKey key = Objects.requireNonNull((NamespacedKey) args.get("key"));
                                                    CustomItem item = AdderePlugin.INSTANCE.getCustomItemManager().get(key);

                                                    if(item == null){
                                                        player.sendMessage(MiniMessage.miniMessage().deserialize(LangManager.get("fail_get_item").replace("%key%", key.toString())));
                                                        return;
                                                    }

                                                    player.getInventory().addItem(item.getItemBuilder().getItemStack());
                                                    player.sendMessage(MiniMessage.miniMessage().deserialize(LangManager.get("success_get_item").replace("%key%", key.toString())));
                                                }),
                                        new CommandAPICommand("give")
                                                .withArguments(new NamespacedKeyArgument("key"), new PlayerArgument("target"))
                                                .executes((sender, args) -> {
                                                    NamespacedKey key = Objects.requireNonNull((NamespacedKey) args.get("key"));
                                                    Player target = Objects.requireNonNull((Player) args.get("target"));
                                                    CustomItem item = AdderePlugin.INSTANCE.getCustomItemManager().get(key);

                                                    if(item == null){
                                                        sender.sendMessage(MiniMessage.miniMessage().deserialize(LangManager.get("fail_get_item").replace("%key%", key.toString())));
                                                        return;
                                                    }

                                                    target.getInventory().addItem(item.getItemBuilder().getItemStack());
                                                    sender.sendMessage(MiniMessage.miniMessage().deserialize(LangManager.get("success_get_item").replace("%key%", key.toString())));
                                                })
                                )
                )
                .register();
    }

    @Override
    public JavaPlugin getPlugin() {
        return this;
    }

    @Override
    public String getServerVersion() {
        return getServer().getMinecraftVersion();
    }

    @Override
    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public AddereAddonLoader getLoader() {
        return loader;
    }

    @Override
    public CustomItemManager getCustomItemManager() {
        return itemManager;
    }
}
