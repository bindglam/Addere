package io.github.bindglam.addere.api.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;

public class ItemBuilder {
    private static final HashMap<Material, Integer> customModelDataMap = new HashMap<>();

    private final ItemStack itemStack;

    private String modelPath;
    private Integer customModelData;

    public ItemBuilder(Material material){
        itemStack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack itemStack){
        this.itemStack = new ItemStack(itemStack);
    }

    public ItemBuilder itemName(Component content){
        ItemMeta meta = itemStack.getItemMeta();
        meta.itemName(content);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(Component... content){
        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(Arrays.stream(content).toList());
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level){
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder setCustomModelData(Integer data){
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(data);
        customModelData = data;
        itemStack.setItemMeta(meta);

        if(customModelDataMap.containsKey(itemStack.getType())) {
            if (data > customModelDataMap.get(itemStack.getType())) {
                customModelDataMap.put(itemStack.getType(), data);
            }
        } else {
            customModelDataMap.put(itemStack.getType(), data);
        }
        return this;
    }

    public ItemBuilder setModelPath(String modelPath){
        this.modelPath = modelPath;

        if(customModelData == null){
            if(customModelDataMap.containsKey(itemStack.getType()))
                setCustomModelData(customModelDataMap.get(itemStack.getType())+1);
            else
                setCustomModelData(1);
        }
        return this;
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    public String getModelPath(){
        return modelPath;
    }
}
