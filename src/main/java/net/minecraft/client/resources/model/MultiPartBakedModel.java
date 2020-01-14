package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

@OnlyIn(Dist.CLIENT)
public class MultiPartBakedModel implements BakedModel {
    private final List<Pair<Predicate<BlockState>, BakedModel>> selectors;
    protected final boolean hasAmbientOcclusion;
    protected final boolean isGui3d;
    protected final boolean usesBlockLight;
    protected final TextureAtlasSprite particleIcon;
    protected final ItemTransforms transforms;
    protected final ItemOverrides overrides;
    private final Map<BlockState, BitSet> selectorCache = new Object2ObjectOpenCustomHashMap<>(Util.identityStrategy());

    public MultiPartBakedModel(List<Pair<Predicate<BlockState>, BakedModel>> param0) {
        this.selectors = param0;
        BakedModel var0 = param0.iterator().next().getRight();
        this.hasAmbientOcclusion = var0.useAmbientOcclusion();
        this.isGui3d = var0.isGui3d();
        this.usesBlockLight = var0.usesBlockLight();
        this.particleIcon = var0.getParticleIcon();
        this.transforms = var0.getTransforms();
        this.overrides = var0.getOverrides();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState param0, @Nullable Direction param1, Random param2) {
        if (param0 == null) {
            return Collections.emptyList();
        } else {
            BitSet var0 = this.selectorCache.get(param0);
            if (var0 == null) {
                var0 = new BitSet();

                for(int var1 = 0; var1 < this.selectors.size(); ++var1) {
                    Pair<Predicate<BlockState>, BakedModel> var2 = this.selectors.get(var1);
                    if (var2.getLeft().test(param0)) {
                        var0.set(var1);
                    }
                }

                this.selectorCache.put(param0, var0);
            }

            List<BakedQuad> var3 = Lists.newArrayList();
            long var4 = param2.nextLong();

            for(int var5 = 0; var5 < var0.length(); ++var5) {
                if (var0.get(var5)) {
                    var3.addAll(this.selectors.get(var5).getRight().getQuads(param0, param1, new Random(var4)));
                }
            }

            return var3;
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.hasAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return this.isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return this.usesBlockLight;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleIcon;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final List<Pair<Predicate<BlockState>, BakedModel>> selectors = Lists.newArrayList();

        public void add(Predicate<BlockState> param0, BakedModel param1) {
            this.selectors.add(Pair.of(param0, param1));
        }

        public BakedModel build() {
            return new MultiPartBakedModel(this.selectors);
        }
    }
}
