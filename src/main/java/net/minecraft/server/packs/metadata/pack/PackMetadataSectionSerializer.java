package net.minecraft.server.packs.metadata.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.GsonHelper;

public class PackMetadataSectionSerializer implements MetadataSectionType<PackMetadataSection> {
    public PackMetadataSection fromJson(JsonObject param0) {
        Component var0 = Component.Serializer.fromJson(param0.get("description"));
        if (var0 == null) {
            throw new JsonParseException("Invalid/missing description!");
        } else {
            int var1 = GsonHelper.getAsInt(param0, "pack_format");
            return new PackMetadataSection(var0, var1);
        }
    }

    public JsonObject toJson(PackMetadataSection param0) {
        JsonObject var0 = new JsonObject();
        var0.add("description", Component.Serializer.toJsonTree(param0.getDescription()));
        var0.addProperty("pack_format", param0.getPackFormat());
        return var0;
    }

    @Override
    public String getMetadataSectionName() {
        return "pack";
    }
}
