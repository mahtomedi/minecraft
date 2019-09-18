package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpinAttackEffectLayer<T extends LivingEntity> extends RenderLayer<T, PlayerModel<T>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
    private final SpinAttackEffectLayer.SpinAttackModel model = new SpinAttackEffectLayer.SpinAttackModel();

    public SpinAttackEffectLayer(RenderLayerParent<T, PlayerModel<T>> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.isAutoSpinAttack()) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture(TEXTURE);

            for(int var0 = 0; var0 < 3; ++var0) {
                RenderSystem.pushMatrix();
                RenderSystem.rotatef(param4 * (float)(-(45 + var0 * 5)), 0.0F, 1.0F, 0.0F);
                float var1 = 0.75F * (float)var0;
                RenderSystem.scalef(var1, var1, var1);
                RenderSystem.translatef(0.0F, -0.2F + 0.6F * (float)var0, 0.0F);
                this.model.render(param1, param2, param4, param5, param6, param7);
                RenderSystem.popMatrix();
            }

        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    static class SpinAttackModel extends Model {
        private final ModelPart box;

        public SpinAttackModel() {
            this.texWidth = 64;
            this.texHeight = 64;
            this.box = new ModelPart(this, 0, 0);
            this.box.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F);
        }

        public void render(float param0, float param1, float param2, float param3, float param4, float param5) {
            this.box.render(param5);
        }
    }
}
