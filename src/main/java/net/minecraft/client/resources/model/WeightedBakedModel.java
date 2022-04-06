package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeightedBakedModel implements BakedModel {
    private final int totalWeight;
    private final List<WeightedEntry.Wrapper<BakedModel>> list;
    private final BakedModel wrapped;

    public WeightedBakedModel(List<WeightedEntry.Wrapper<BakedModel>> param0) {
        this.list = param0;
        this.totalWeight = WeightedRandom.getTotalWeight(param0);
        this.wrapped = param0.get(0).getData();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState param0, @Nullable Direction param1, RandomSource param2) {
        return WeightedRandom.getWeightedItem(this.list, Math.abs((int)param2.nextLong()) % this.totalWeight)
            .map(param3 -> param3.getData().getQuads(param0, param1, param2))
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
        private final List<WeightedEntry.Wrapper<BakedModel>> list = Lists.newArrayList();

        public WeightedBakedModel.Builder add(@Nullable BakedModel param0, int param1) {
            if (param0 != null) {
                this.list.add(WeightedEntry.wrap(param0, param1));
            }

            return this;
        }

        @Nullable
        public BakedModel build() {
            if (this.list.isEmpty()) {
                return null;
            } else {
                return (BakedModel)(this.list.size() == 1 ? this.list.get(0).getData() : new WeightedBakedModel(this.list));
            }
        }
    }
}
