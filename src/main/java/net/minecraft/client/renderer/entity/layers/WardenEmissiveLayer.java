package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends WardenModel<T>> extends RenderLayer<T, M> {
    private final ResourceLocation texture;
    private final WardenEmissiveLayer.AlphaFunction<T> alphaFunction;
    private final WardenEmissiveLayer.DrawSelector<T, M> drawSelector;

    public WardenEmissiveLayer(
        RenderLayerParent<T, M> param0, ResourceLocation param1, WardenEmissiveLayer.AlphaFunction<T> param2, WardenEmissiveLayer.DrawSelector<T, M> param3
    ) {
        super(param0);
        this.texture = param1;
        this.alphaFunction = param2;
        this.drawSelector = param3;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (!param3.isInvisible()) {
            this.onlyDrawSelectedParts();
            VertexConsumer var0 = param1.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
            this.getParentModel()
                .renderToBuffer(
                    param0,
                    var0,
                    param2,
                    LivingEntityRenderer.getOverlayCoords(param3, 0.0F),
                    1.0F,
                    1.0F,
                    1.0F,
                    this.alphaFunction.apply(param3, param6, param7)
                );
            this.resetDrawForAllParts();
        }
    }

    private void onlyDrawSelectedParts() {
        List<ModelPart> var0 = this.drawSelector.getPartsToDraw(this.getParentModel());
        this.getParentModel().root().getAllParts().forEach(param0 -> param0.skipDraw = true);
        var0.forEach(param0 -> param0.skipDraw = false);
    }

    private void resetDrawForAllParts() {
        this.getParentModel().root().getAllParts().forEach(param0 -> param0.skipDraw = false);
    }

    @OnlyIn(Dist.CLIENT)
    public interface AlphaFunction<T extends Warden> {
        float apply(T var1, float var2, float var3);
    }

    @OnlyIn(Dist.CLIENT)
    public interface DrawSelector<T extends Warden, M extends EntityModel<T>> {
        List<ModelPart> getPartsToDraw(M var1);
    }
}
