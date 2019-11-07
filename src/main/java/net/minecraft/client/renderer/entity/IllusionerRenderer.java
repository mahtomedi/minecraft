package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IllusionerRenderer extends IllagerRenderer<Illusioner> {
    private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRenderDispatcher param0) {
        super(param0, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(
            new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this) {
                public void render(
                    PoseStack param0,
                    MultiBufferSource param1,
                    int param2,
                    Illusioner param3,
                    float param4,
                    float param5,
                    float param6,
                    float param7,
                    float param8,
                    float param9
                ) {
                    if (param3.isCastingSpell() || param3.isAggressive()) {
                        super.render(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
                    }
    
                }
            }
        );
        this.model.getHat().visible = true;
    }

    public ResourceLocation getTextureLocation(Illusioner param0) {
        return ILLUSIONER;
    }

    public void render(Illusioner param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        if (param0.isInvisible()) {
            Vec3[] var0 = param0.getIllusionOffsets(param2);
            float var1 = this.getBob(param0, param2);

            for(int var2 = 0; var2 < var0.length; ++var2) {
                param3.pushPose();
                param3.translate(
                    var0[var2].x + (double)Mth.cos((float)var2 + var1 * 0.5F) * 0.025,
                    var0[var2].y + (double)Mth.cos((float)var2 + var1 * 0.75F) * 0.0125,
                    var0[var2].z + (double)Mth.cos((float)var2 + var1 * 0.7F) * 0.025
                );
                super.render(param0, param1, param2, param3, param4, param5);
                param3.popPose();
            }
        } else {
            super.render(param0, param1, param2, param3, param4, param5);
        }

    }

    protected boolean isVisible(Illusioner param0, boolean param1) {
        return true;
    }
}
