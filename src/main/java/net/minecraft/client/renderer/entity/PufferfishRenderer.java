package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PufferfishRenderer extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
    private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
    private int puffStateO = 3;
    private final EntityModel<Pufferfish> small;
    private final EntityModel<Pufferfish> mid;
    private final EntityModel<Pufferfish> big = this.getModel();

    public PufferfishRenderer(EntityRendererProvider.Context param0) {
        super(param0, new PufferfishBigModel<>(param0.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2F);
        this.mid = new PufferfishMidModel<>(param0.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
        this.small = new PufferfishSmallModel<>(param0.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
    }

    public ResourceLocation getTextureLocation(Pufferfish param0) {
        return PUFFER_LOCATION;
    }

    public void render(Pufferfish param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        int var0 = param0.getPuffState();
        if (var0 != this.puffStateO) {
            if (var0 == 0) {
                this.model = this.small;
            } else if (var0 == 1) {
                this.model = this.mid;
            } else {
                this.model = this.big;
            }
        }

        this.puffStateO = var0;
        this.shadowRadius = 0.1F + 0.1F * (float)var0;
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected void setupRotations(Pufferfish param0, PoseStack param1, float param2, float param3, float param4) {
        param1.translate(0.0, (double)(Mth.cos(param2 * 0.05F) * 0.08F), 0.0);
        super.setupRotations(param0, param1, param2, param3, param4);
    }
}
