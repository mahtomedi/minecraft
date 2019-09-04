package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
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

    public void render(Sheep param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (!param0.isSheared() && !param0.isInvisible()) {
            this.bindTexture(SHEEP_FUR_LOCATION);
            if (param0.hasCustomName() && "jeb_".equals(param0.getName().getContents())) {
                int var0 = 25;
                int var1 = param0.tickCount / 25 + param0.getId();
                int var2 = DyeColor.values().length;
                int var3 = var1 % var2;
                int var4 = (var1 + 1) % var2;
                float var5 = ((float)(param0.tickCount % 25) + param3) / 25.0F;
                float[] var6 = Sheep.getColorArray(DyeColor.byId(var3));
                float[] var7 = Sheep.getColorArray(DyeColor.byId(var4));
                RenderSystem.color3f(
                    var6[0] * (1.0F - var5) + var7[0] * var5, var6[1] * (1.0F - var5) + var7[1] * var5, var6[2] * (1.0F - var5) + var7[2] * var5
                );
            } else {
                float[] var8 = Sheep.getColorArray(param0.getColor());
                RenderSystem.color3f(var8[0], var8[1], var8[2]);
            }

            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(param0, param1, param2, param3);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
