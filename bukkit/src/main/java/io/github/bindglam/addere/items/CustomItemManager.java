package io.github.bindglam.addere.items;

import io.github.bindglam.addere.api.items.CustomItem;
import io.github.bindglam.addere.api.utils.IEnrollmentManager;
import org.bukkit.NamespacedKey;

import java.util.Map;
import java.util.WeakHashMap;

public class CustomItemManager implements IEnrollmentManager<CustomItem> {
    private final WeakHashMap<NamespacedKey, CustomItem> customItems = new WeakHashMap<>();

    @Override
    public void register(CustomItem item) {
        customItems.put(item.getKey(), item);
    }

    @Override
    public CustomItem get(NamespacedKey key) {
        return customItems.get(key);
    }

    @Override
    public Map<NamespacedKey, CustomItem> get() {
        return customItems;
    }
}
