package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepFurLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
    private static final ResourceLocation SHEEP_FUR_LOCATION = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
    private final SheepFurModel<Sheep> model = new SheepFurModel<>();

    public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Sheep param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        if (!param3.isSheared() && !param3.isInvisible()) {
            float var8;
            float var9;
            float var10;
            if (param3.hasCustomName() && "jeb_".equals(param3.getName().getContents())) {
                int var0 = 25;
                int var1 = param3.tickCount / 25 + param3.getId();
                int var2 = DyeColor.values().length;
                int var3 = var1 % var2;
                int var4 = (var1 + 1) % var2;
                float var5 = ((float)(param3.tickCount % 25) + param6) / 25.0F;
                float[] var6 = Sheep.getColorArray(DyeColor.byId(var3));
                float[] var7 = Sheep.getColorArray(DyeColor.byId(var4));
                var8 = var6[0] * (1.0F - var5) + var7[0] * var5;
                var9 = var6[1] * (1.0F - var5) + var7[1] * var5;
                var10 = var6[2] * (1.0F - var5) + var7[2] * var5;
            } else {
                float[] var11 = Sheep.getColorArray(param3.getColor());
                var8 = var11[0];
                var9 = var11[1];
                var10 = var11[2];
            }

            coloredCutoutModelCopyLayerRender(
                this.getParentModel(),
                this.model,
                SHEEP_FUR_LOCATION,
                param0,
                param1,
                param2,
                param3,
                param4,
                param5,
                param7,
                param8,
                param9,
                param6,
                var8,
                var9,
                var10
            );
        }
    }
}
