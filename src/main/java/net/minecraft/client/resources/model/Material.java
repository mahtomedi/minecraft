package net.minecraft.client.resources.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Material {
    private final ResourceLocation atlasLocation;
    private final ResourceLocation texture;
    @Nullable
    private RenderType renderType;

    public Material(ResourceLocation param0, ResourceLocation param1) {
        this.atlasLocation = param0;
        this.texture = param1;
    }

    public ResourceLocation atlasLocation() {
        return this.atlasLocation;
    }

    public ResourceLocation texture() {
        return this.texture;
    }

    public TextureAtlasSprite sprite() {
        return Minecraft.getInstance().getTextureAtlas(this.atlasLocation()).apply(this.texture());
    }

    public RenderType renderType(Function<ResourceLocation, RenderType> param0) {
        if (this.renderType == null) {
            this.renderType = param0.apply(this.atlasLocation);
        }

        return this.renderType;
    }

    public VertexConsumer buffer(MultiBufferSource param0, Function<ResourceLocation, RenderType> param1) {
        return this.sprite().wrap(param0.getBuffer(this.renderType(param1)));
    }

    public VertexConsumer buffer(MultiBufferSource param0, Function<ResourceLocation, RenderType> param1, boolean param2) {
        return this.sprite().wrap(ItemRenderer.getFoilBufferDirect(param0, this.renderType(param1), false, param2));
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Material var0 = (Material)param0;
            return this.atlasLocation.equals(var0.atlasLocation) && this.texture.equals(var0.texture);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.atlasLocation, this.texture);
    }

    @Override
    public String toString() {
        return "Material{atlasLocation=" + this.atlasLocation + ", texture=" + this.texture + '}';
    }
}
