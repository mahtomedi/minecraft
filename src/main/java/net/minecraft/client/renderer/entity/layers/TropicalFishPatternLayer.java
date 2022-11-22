package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishPatternLayer extends RenderLayer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
    private static final ResourceLocation KOB_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_1.png");
    private static final ResourceLocation SUNSTREAK_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_2.png");
    private static final ResourceLocation SNOOPER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_3.png");
    private static final ResourceLocation DASHER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_4.png");
    private static final ResourceLocation BRINELY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_5.png");
    private static final ResourceLocation SPOTTY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_6.png");
    private static final ResourceLocation FLOPPER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_1.png");
    private static final ResourceLocation STRIPEY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_2.png");
    private static final ResourceLocation GLITTER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_3.png");
    private static final ResourceLocation BLOCKFISH_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_4.png");
    private static final ResourceLocation BETTY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_5.png");
    private static final ResourceLocation CLAYFISH_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_6.png");
    private final TropicalFishModelA<TropicalFish> modelA;
    private final TropicalFishModelB<TropicalFish> modelB;

    public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, ColorableHierarchicalModel<TropicalFish>> param0, EntityModelSet param1) {
        super(param0);
        this.modelA = new TropicalFishModelA<>(param1.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL_PATTERN));
        this.modelB = new TropicalFishModelB<>(param1.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        TropicalFish param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        TropicalFish.Pattern var0 = param3.getVariant();

        EntityModel<TropicalFish> var1 = (EntityModel<TropicalFish>)(switch(var0.base()) {
            case SMALL -> this.modelA;
            case LARGE -> this.modelB;
        });

        ResourceLocation var2 = switch(var0) {
            case KOB -> KOB_TEXTURE;
            case SUNSTREAK -> SUNSTREAK_TEXTURE;
            case SNOOPER -> SNOOPER_TEXTURE;
            case DASHER -> DASHER_TEXTURE;
            case BRINELY -> BRINELY_TEXTURE;
            case SPOTTY -> SPOTTY_TEXTURE;
            case FLOPPER -> FLOPPER_TEXTURE;
            case STRIPEY -> STRIPEY_TEXTURE;
            case GLITTER -> GLITTER_TEXTURE;
            case BLOCKFISH -> BLOCKFISH_TEXTURE;
            case BETTY -> BETTY_TEXTURE;
            case CLAYFISH -> CLAYFISH_TEXTURE;
        };
        float[] var3 = param3.getPatternColor().getTextureDiffuseColors();
        coloredCutoutModelCopyLayerRender(
            this.getParentModel(), var1, var2, param0, param1, param2, param3, param4, param5, param7, param8, param9, param6, var3[0], var3[1], var3[2]
        );
    }
}
