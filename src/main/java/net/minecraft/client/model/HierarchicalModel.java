package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        return this.root().getAllParts().filter(param1 -> param1.hasChild(param0)).findFirst().map(param1 -> param1.getChild(param0));
    }

    protected void animate(AnimationState param0, AnimationDefinition param1) {
        this.animate(param0, param1, 1.0F);
    }

    protected void animate(AnimationState param0, AnimationDefinition param1, float param2) {
        param0.updateTime(Minecraft.getInstance().isPaused(), param2);
        param0.ifStarted(param1x -> KeyframeAnimations.animate(this, param1, param1x.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE));
    }
}
