package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class HierarchicalModel<E extends Entity> extends EntityModel<E> {
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public HierarchicalModel() {
        this(RenderType::entityCutoutNoCull);
    }

    public HierarchicalModel(Function<ResourceLocation, RenderType> param0) {
        super(param0);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.root().render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public abstract ModelPart root();

    public Optional<ModelPart> getAnyDescendantWithName(String param0) {
        return param0.equals("root")
            ? Optional.of(this.root())
            : this.root().getAllParts().filter(param1 -> param1.hasChild(param0)).findFirst().map(param1 -> param1.getChild(param0));
    }

    protected void animate(AnimationState param0, AnimationDefinition param1, float param2) {
        this.animate(param0, param1, param2, 1.0F);
    }

    protected void animateWalk(AnimationDefinition param0, float param1, float param2, float param3, float param4) {
        long var0 = (long)(param1 * 50.0F * param3);
        float var1 = Math.min(param2 * param4, 1.0F);
        KeyframeAnimations.animate(this, param0, var0, var1, ANIMATION_VECTOR_CACHE);
    }

    protected void animate(AnimationState param0, AnimationDefinition param1, float param2, float param3) {
        param0.updateTime(param2, param3);
        param0.ifStarted(param1x -> KeyframeAnimations.animate(this, param1, param1x.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE));
    }

    protected void applyStatic(AnimationDefinition param0) {
        KeyframeAnimations.animate(this, param0, 0L, 1.0F, ANIMATION_VECTOR_CACHE);
    }
}
