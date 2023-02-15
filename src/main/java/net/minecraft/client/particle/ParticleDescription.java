package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleDescription {
    private final List<ResourceLocation> textures;

    private ParticleDescription(List<ResourceLocation> param0) {
        this.textures = param0;
    }

    public List<ResourceLocation> getTextures() {
        return this.textures;
    }

    public static ParticleDescription fromJson(JsonObject param0) {
        JsonArray var0 = GsonHelper.getAsJsonArray(param0, "textures", null);
        if (var0 == null) {
            return new ParticleDescription(List.of());
        } else {
            List<ResourceLocation> var1 = Streams.stream(var0)
                .map(param0x -> GsonHelper.convertToString(param0x, "texture"))
                .map(ResourceLocation::new)
                .collect(ImmutableList.toImmutableList());
            return new ParticleDescription(var1);
        }
    }
}
