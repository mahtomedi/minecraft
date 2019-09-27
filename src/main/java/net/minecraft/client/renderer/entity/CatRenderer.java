package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatRenderer extends MobRenderer<Cat, CatModel<Cat>> {
    public CatRenderer(EntityRenderDispatcher param0) {
        super(param0, new CatModel<>(0.0F), 0.4F);
        this.addLayer(new CatCollarLayer(this));
    }

    public ResourceLocation getTextureLocation(Cat param0) {
        return param0.getResourceLocation();
    }

    protected void scale(Cat param0, PoseStack param1, float param2) {
        super.scale(param0, param1, param2);
        param1.scale(0.8F, 0.8F, 0.8F);
    }

    protected void setupRotations(Cat param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        float var0 = param0.getLieDownAmount(param4);
        if (var0 > 0.0F) {
            param1.translate((double)(0.4F * var0), (double)(0.15F * var0), (double)(0.1F * var0));
            param1.mulPose(Vector3f.ZP.rotation(Mth.rotLerp(var0, 0.0F, 90.0F), true));
            BlockPos var1 = new BlockPos(param0);

            for(Player var3 : param0.level.getEntitiesOfClass(Player.class, new AABB(var1).inflate(2.0, 2.0, 2.0))) {
                if (var3.isSleeping()) {
                    param1.translate((double)(0.15F * var0), 0.0, 0.0);
                    break;
                }
            }
        }

    }
}
