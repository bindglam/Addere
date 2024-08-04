package io.github.bindglam.addere.api.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomItem implements Listener, Keyed {
    private final NamespacedKey key;
    private final ItemBuilder itemBuilder;

    public CustomItem(NamespacedKey key, ItemBuilder itemBuilder) {
        this.key = key;
        this.itemBuilder = itemBuilder;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }
}
