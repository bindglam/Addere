package io.github.bindglam.addere.api.utils;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.Map;

public interface IEnrollmentManager<T extends Keyed> {
    void register(T item);

    T get(NamespacedKey key);

    Map<NamespacedKey, T> get();
}
