package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatCollarLayer extends RenderLayer<Cat, CatModel<Cat>> {
    private static final ResourceLocation CAT_COLLAR_LOCATION = new ResourceLocation("textures/entity/cat/cat_collar.png");
    private final CatModel<Cat> catModel = new CatModel<>(0.01F);

    public CatCollarLayer(RenderLayerParent<Cat, CatModel<Cat>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Cat param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        if (param3.isTame()) {
            float[] var0 = param3.getCollarColor().getTextureDiffuseColors();
            coloredCutoutModelCopyLayerRender(
                this.getParentModel(),
                this.catModel,
                CAT_COLLAR_LOCATION,
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
                param6,
                var0[0],
                var0[1],
                var0[2]
            );
        }
    }
}
