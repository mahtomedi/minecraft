package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IllagerModel;
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
        this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this) {
            public void render(Illusioner param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
                if (param0.isCastingSpell() || param0.isAggressive()) {
                    super.render(param0, param1, param2, param3, param4, param5, param6, param7);
                }

            }
        });
        this.model.getHat().visible = true;
    }

    protected ResourceLocation getTextureLocation(Illusioner param0) {
        return ILLUSIONER;
    }

    public void render(Illusioner param0, double param1, double param2, double param3, float param4, float param5) {
        if (param0.isInvisible()) {
            Vec3[] var0 = param0.getIllusionOffsets(param5);
            float var1 = this.getBob(param0, param5);

            for(int var2 = 0; var2 < var0.length; ++var2) {
                super.render(
                    param0,
                    param1 + var0[var2].x + (double)Mth.cos((float)var2 + var1 * 0.5F) * 0.025,
                    param2 + var0[var2].y + (double)Mth.cos((float)var2 + var1 * 0.75F) * 0.0125,
                    param3 + var0[var2].z + (double)Mth.cos((float)var2 + var1 * 0.7F) * 0.025,
                    param4,
                    param5
                );
            }
        } else {
            super.render(param0, param1, param2, param3, param4, param5);
        }

    }

    protected boolean isVisible(Illusioner param0) {
        return true;
    }
}
