package io.github.bindglam.addere.pack;

import io.github.bindglam.addere.AdderePlugin;
import io.github.bindglam.addere.api.items.CustomItem;
import io.github.bindglam.addere.api.utils.JarUtil;
import io.github.bindglam.addere.api.utils.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PackGenerator {
    public static void generate(){
        try {
            JarUtil.copyFolderFromJar(PackGenerator.class, "assets", new File("plugins/Addere/resourcepack"), JarUtil.CopyOption.REPLACE_IF_EXIST);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy addon's resources!", e);
        }

        try {
            updatePackMeta();
        } catch (IOException e) {
            throw new RuntimeException("Failed to update pack.mcmeta!", e);
        }

        for(CustomItem item : AdderePlugin.INSTANCE.getCustomItemManager().get().values()){
            if(item.getItemBuilder().getModelPath() == null) continue;
            ItemStack itemStack = item.getItemBuilder().getItemStack();
            File originModel = new File("plugins/Addere/resourcepack/assets/minecraft/models/item/" + itemStack.getType().name().toLowerCase() + ".json");

            JSONParser parser = new JSONParser();
            JSONObject obj;

            try{
                obj = (JSONObject) parser.parse(FileUtils.readFileToString(originModel, Charset.defaultCharset()));
            } catch (ParseException | IOException e){
                throw new RuntimeException("Failed to load minecraft item's model data!", e);
            }

            JSONArray overrides;
            if(obj.containsKey("overrides")){
                overrides = (JSONArray) obj.get("overrides");
            } else {
                overrides = new JSONArray();
            }
            JSONObject cmdData = new JSONObject();
            cmdData.put("predicate", new JSONObject(){{
                put("custom_model_data", itemStack.getItemMeta().getCustomModelData());
            }});
            cmdData.put("model", item.getItemBuilder().getModelPath());
            overrides.add(cmdData);
            obj.put("overrides", overrides);

            try {
                FileUtils.writeStringToFile(originModel, obj.toJSONString(), Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException("Failed to update resourcepack!", e);
            }
        }

        try {
            ZipUtil.pack("plugins/Addere/resourcepack", "plugins/Addere/resourcepack.zip");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create resourcepack zip!", e);
        }
    }

    private static void updatePackMeta() throws IOException {
        File packMcmeta = new File("plugins/Addere/resourcepack/pack.mcmeta");
        if(!packMcmeta.exists()) {
            try {
                packMcmeta.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        JSONObject mcmeta = new JSONObject();
        mcmeta.put("pack", new JSONObject(){{
            put("pack_format", 5);
            put("description", "Addere");
        }});
        FileUtils.writeStringToFile(packMcmeta, mcmeta.toJSONString(), Charset.defaultCharset());
    }
}
