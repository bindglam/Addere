package io.github.bindglam.test;

import io.github.bindglam.addere.api.Addere;
import io.github.bindglam.addere.api.addons.AddonInfo;
import io.github.bindglam.addere.api.items.CustomItem;
import io.github.bindglam.addere.api.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class TestAddon {
    @AddonInfo(name = "test-addon")
    public TestAddon(Addere addere){
        addere.getPlugin().getLogger().info("Test Addon is initialized!");

        addere.getCustomItemManager().register(new CustomItem(new NamespacedKey("test-addon", "test-item"), new ItemBuilder(Material.PAPER).itemName(Component.text("test"))
                .setModelPath("test-addon:item/test-model")));
        addere.getCustomItemManager().register(new CustomItem(new NamespacedKey("test-addon", "test-item2"), new ItemBuilder(Material.PAPER).itemName(Component.text("테스트"))
                .lore(Component.text("tests"))
                .setModelPath("test-addon:item/test-model2")));
    }
}
