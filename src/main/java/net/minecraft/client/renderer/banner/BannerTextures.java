package net.minecraft.client.renderer.banner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerTextures {
    public static final BannerTextures.TextureCache BANNER_CACHE = new BannerTextures.TextureCache(
        "banner_", new ResourceLocation("textures/entity/banner_base.png"), "textures/entity/banner/"
    );
    public static final BannerTextures.TextureCache SHIELD_CACHE = new BannerTextures.TextureCache(
        "shield_", new ResourceLocation("textures/entity/shield_base.png"), "textures/entity/shield/"
    );
    public static final ResourceLocation NO_PATTERN_SHIELD = new ResourceLocation("textures/entity/shield_base_nopattern.png");
    public static final ResourceLocation DEFAULT_PATTERN_BANNER = new ResourceLocation("textures/entity/banner/base.png");

    @OnlyIn(Dist.CLIENT)
    public static class TextureCache {
        private final Map<String, BannerTextures.TimestampedBannerTexture> cache = Maps.newLinkedHashMap();
        private final ResourceLocation baseResource;
        private final String resourceNameBase;
        private final String hashPrefix;

        public TextureCache(String param0, ResourceLocation param1, String param2) {
            this.hashPrefix = param0;
            this.baseResource = param1;
            this.resourceNameBase = param2;
        }

        @Nullable
        public ResourceLocation getTextureLocation(String param0, List<BannerPattern> param1, List<DyeColor> param2) {
            if (param0.isEmpty()) {
                return null;
            } else if (!param1.isEmpty() && !param2.isEmpty()) {
                param0 = this.hashPrefix + param0;
                BannerTextures.TimestampedBannerTexture var0 = this.cache.get(param0);
                if (var0 == null) {
                    if (this.cache.size() >= 256 && !this.freeCacheSlot()) {
                        return BannerTextures.DEFAULT_PATTERN_BANNER;
                    }

                    List<String> var1 = Lists.newArrayList();

                    for(BannerPattern var2 : param1) {
                        var1.add(this.resourceNameBase + var2.getFilename() + ".png");
                    }

                    var0 = new BannerTextures.TimestampedBannerTexture();
                    var0.textureLocation = new ResourceLocation(param0);
                    Minecraft.getInstance().getTextureManager().register(var0.textureLocation, new LayeredColorMaskTexture(this.baseResource, var1, param2));
                    this.cache.put(param0, var0);
                }

                var0.lastUseMilliseconds = Util.getMillis();
                return var0.textureLocation;
            } else {
                return MissingTextureAtlasSprite.getLocation();
            }
        }

        private boolean freeCacheSlot() {
            long var0 = Util.getMillis();
            Iterator<String> var1 = this.cache.keySet().iterator();

            while(var1.hasNext()) {
                String var2 = var1.next();
                BannerTextures.TimestampedBannerTexture var3 = this.cache.get(var2);
                if (var0 - var3.lastUseMilliseconds > 5000L) {
                    Minecraft.getInstance().getTextureManager().release(var3.textureLocation);
                    var1.remove();
                    return true;
                }
            }

            return this.cache.size() < 256;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class TimestampedBannerTexture {
        public long lastUseMilliseconds;
        public ResourceLocation textureLocation;

        private TimestampedBannerTexture() {
        }
    }
}
