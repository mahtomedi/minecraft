package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobEffectTextureManager extends TextureAtlasHolder {
    public MobEffectTextureManager(TextureManager param0) {
        super(param0, TextureAtlas.LOCATION_MOB_EFFECTS, "textures/mob_effect");
    }

    @Override
    protected Iterable<ResourceLocation> getResourcesToLoad() {
        return Registry.MOB_EFFECT.keySet();
    }

    public TextureAtlasSprite get(MobEffect param0) {
        return this.getSprite(Registry.MOB_EFFECT.getKey(param0));
    }
}
