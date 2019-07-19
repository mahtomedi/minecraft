package net.minecraft.client.resources.model;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BuiltInModel implements BakedModel {
    private final ItemTransforms itemTransforms;
    private final ItemOverrides overrides;
    private final TextureAtlasSprite particleTexture;

    public BuiltInModel(ItemTransforms param0, ItemOverrides param1, TextureAtlasSprite param2) {
        this.itemTransforms = param0;
        this.overrides = param1;
        this.particleTexture = param2;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState param0, @Nullable Direction param1, Random param2) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleTexture;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.itemTransforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }
}
