package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleBakedModel implements BakedModel {
    protected final List<BakedQuad> unculledFaces;
    protected final Map<Direction, List<BakedQuad>> culledFaces;
    protected final boolean hasAmbientOcclusion;
    protected final boolean isGui3d;
    protected final boolean usesBlockLight;
    protected final TextureAtlasSprite particleIcon;
    protected final ItemTransforms transforms;
    protected final ItemOverrides overrides;

    public SimpleBakedModel(
        List<BakedQuad> param0,
        Map<Direction, List<BakedQuad>> param1,
        boolean param2,
        boolean param3,
        boolean param4,
        TextureAtlasSprite param5,
        ItemTransforms param6,
        ItemOverrides param7
    ) {
        this.unculledFaces = param0;
        this.culledFaces = param1;
        this.hasAmbientOcclusion = param2;
        this.isGui3d = param4;
        this.usesBlockLight = param3;
        this.particleIcon = param5;
        this.transforms = param6;
        this.overrides = param7;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState param0, @Nullable Direction param1, Random param2) {
        return param1 == null ? this.unculledFaces : this.culledFaces.get(param1);
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
        private final List<BakedQuad> unculledFaces = Lists.newArrayList();
        private final Map<Direction, List<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
        private final ItemOverrides overrides;
        private final boolean hasAmbientOcclusion;
        private TextureAtlasSprite particleIcon;
        private final boolean usesBlockLight;
        private final boolean isGui3d;
        private final ItemTransforms transforms;

        public Builder(BlockModel param0, ItemOverrides param1, boolean param2) {
            this(param0.hasAmbientOcclusion(), param0.getGuiLight().lightLikeBlock(), param2, param0.getTransforms(), param1);
        }

        private Builder(boolean param0, boolean param1, boolean param2, ItemTransforms param3, ItemOverrides param4) {
            for(Direction var0 : Direction.values()) {
                this.culledFaces.put(var0, Lists.newArrayList());
            }

            this.overrides = param4;
            this.hasAmbientOcclusion = param0;
            this.usesBlockLight = param1;
            this.isGui3d = param2;
            this.transforms = param3;
        }

        public SimpleBakedModel.Builder addCulledFace(Direction param0, BakedQuad param1) {
            this.culledFaces.get(param0).add(param1);
            return this;
        }

        public SimpleBakedModel.Builder addUnculledFace(BakedQuad param0) {
            this.unculledFaces.add(param0);
            return this;
        }

        public SimpleBakedModel.Builder particle(TextureAtlasSprite param0) {
            this.particleIcon = param0;
            return this;
        }

        public BakedModel build() {
            if (this.particleIcon == null) {
                throw new RuntimeException("Missing particle!");
            } else {
                return new SimpleBakedModel(
                    this.unculledFaces,
                    this.culledFaces,
                    this.hasAmbientOcclusion,
                    this.usesBlockLight,
                    this.isGui3d,
                    this.particleIcon,
                    this.transforms,
                    this.overrides
                );
            }
        }
    }
}
