package net.minecraft.client.resources.metadata.texture;

import com.google.gson.JsonObject;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureMetadataSectionSerializer implements MetadataSectionSerializer<TextureMetadataSection> {
    public TextureMetadataSection fromJson(JsonObject param0) {
        boolean var0 = GsonHelper.getAsBoolean(param0, "blur", false);
        boolean var1 = GsonHelper.getAsBoolean(param0, "clamp", false);
        return new TextureMetadataSection(var0, var1);
    }

    @Override
    public String getMetadataSectionName() {
        return "texture";
    }
}
