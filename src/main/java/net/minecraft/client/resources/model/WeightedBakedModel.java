package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeightedBakedModel implements BakedModel {
    private final int totalWeight;
    private final List<WeightedBakedModel.WeightedModel> list;
    private final BakedModel wrapped;

    public WeightedBakedModel(List<WeightedBakedModel.WeightedModel> param0) {
        this.list = param0;
        this.totalWeight = WeighedRandom.getTotalWeight(param0);
        this.wrapped = param0.get(0).model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState param0, @Nullable Direction param1, Random param2) {
        return WeighedRandom.getWeightedItem(this.list, Math.abs((int)param2.nextLong()) % this.totalWeight)
            .map(param3 -> param3.model.getQuads(param0, param1, param2))
            .orElse(Collections.emptyList());
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.wrapped.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.wrapped.getOverrides();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final List<WeightedBakedModel.WeightedModel> list = Lists.newArrayList();

        public WeightedBakedModel.Builder add(@Nullable BakedModel param0, int param1) {
            if (param0 != null) {
                this.list.add(new WeightedBakedModel.WeightedModel(param0, param1));
            }

            return this;
        }

        @Nullable
        public BakedModel build() {
            if (this.list.isEmpty()) {
                return null;
            } else {
                return (BakedModel)(this.list.size() == 1 ? this.list.get(0).model : new WeightedBakedModel(this.list));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class WeightedModel extends WeighedRandom.WeighedRandomItem {
        protected final BakedModel model;

        public WeightedModel(BakedModel param0, int param1) {
            super(param1);
            this.model = param0;
        }
    }
}
