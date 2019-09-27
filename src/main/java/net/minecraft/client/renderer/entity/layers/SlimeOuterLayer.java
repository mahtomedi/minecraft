package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeOuterLayer<T extends LivingEntity> extends RenderLayer<T, SlimeModel<T>> {
    private final EntityModel<T> model = new SlimeModel<>(0);

    public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        T param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        coloredModelCopyLayerRender(
            this.getParentModel(),
            this.model,
            this.getTextureLocation(param3),
            param0,
            param1,
            param2,
            param3,
            param4,
            param5,
            param7,
            param8,
            param9,
            param10,
            param6
        );
    }
}
