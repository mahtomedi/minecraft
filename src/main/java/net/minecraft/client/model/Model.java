package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Model implements Consumer<ModelPart> {
    protected final Function<ResourceLocation, RenderType> renderType;
    public int texWidth = 64;
    public int texHeight = 32;

    public Model(Function<ResourceLocation, RenderType> param0) {
        this.renderType = param0;
    }

    public void accept(ModelPart param0) {
    }

    public final RenderType renderType(ResourceLocation param0) {
        return this.renderType.apply(param0);
    }

    public abstract void renderToBuffer(PoseStack var1, VertexConsumer var2, int var3, int var4, float var5, float var6, float var7, float var8);
}
