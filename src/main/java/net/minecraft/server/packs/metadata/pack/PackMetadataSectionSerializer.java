package net.minecraft.server.packs.metadata.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public class PackMetadataSectionSerializer implements MetadataSectionSerializer<PackMetadataSection> {
    public PackMetadataSection fromJson(JsonObject param0) {
        Component var0 = Component.Serializer.fromJson(param0.get("description"));
        if (var0 == null) {
            throw new JsonParseException("Invalid/missing description!");
        } else {
            int var1 = GsonHelper.getAsInt(param0, "pack_format");
            return new PackMetadataSection(var0, var1);
        }
    }

    @Override
    public String getMetadataSectionName() {
        return "pack";
    }
}
