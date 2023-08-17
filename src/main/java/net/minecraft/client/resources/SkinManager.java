package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinManager {
    private static final String PROPERTY_TEXTURES = "textures";
    private final LoadingCache<SkinManager.CacheKey, CompletableFuture<PlayerSkin>> skinCache;
    private final SkinManager.TextureCache skinTextures;
    private final SkinManager.TextureCache capeTextures;
    private final SkinManager.TextureCache elytraTextures;

    public SkinManager(TextureManager param0, Path param1, final MinecraftSessionService param2, final Executor param3) {
        this.skinTextures = new SkinManager.TextureCache(param0, param1, Type.SKIN);
        this.capeTextures = new SkinManager.TextureCache(param0, param1, Type.CAPE);
        this.elytraTextures = new SkinManager.TextureCache(param0, param1, Type.ELYTRA);
        this.skinCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(15L))
            .build(new CacheLoader<SkinManager.CacheKey, CompletableFuture<PlayerSkin>>() {
                public CompletableFuture<PlayerSkin> load(SkinManager.CacheKey param0) {
                    GameProfile var0 = param0.profile();
                    return CompletableFuture.<SkinManager.TextureInfo>supplyAsync(() -> {
                        try {
                            try {
                                return SkinManager.TextureInfo.unpack(param2.getTextures(var0, true), true);
                            } catch (InsecurePublicKeyException var3) {
                                return SkinManager.TextureInfo.unpack(param2.getTextures(var0, false), false);
                            }
                        } catch (Throwable var4) {
                            return SkinManager.TextureInfo.EMPTY;
                        }
                    }, Util.backgroundExecutor()).thenComposeAsync(param1 -> SkinManager.this.registerTextures(var0, param1), param3);
                }
            });
    }

    public Supplier<PlayerSkin> lookupInsecure(GameProfile param0) {
        CompletableFuture<PlayerSkin> var0 = this.getOrLoad(param0);
        PlayerSkin var1 = DefaultPlayerSkin.get(param0);
        return () -> var0.getNow(var1);
    }

    public PlayerSkin getInsecureSkin(GameProfile param0) {
        PlayerSkin var0 = this.getOrLoad(param0).getNow(null);
        return var0 != null ? var0 : DefaultPlayerSkin.get(param0);
    }

    public CompletableFuture<PlayerSkin> getOrLoad(GameProfile param0) {
        return this.skinCache.getUnchecked(new SkinManager.CacheKey(param0));
    }

    CompletableFuture<PlayerSkin> registerTextures(GameProfile param0, SkinManager.TextureInfo param1) {
        MinecraftProfileTexture var0 = param1.skin();
        CompletableFuture<ResourceLocation> var1;
        PlayerSkin.Model var2;
        if (var0 != null) {
            var1 = this.skinTextures.getOrLoad(var0);
            var2 = PlayerSkin.Model.byName(var0.getMetadata("model"));
        } else {
            PlayerSkin var3 = DefaultPlayerSkin.get(param0);
            var1 = CompletableFuture.completedFuture(var3.texture());
            var2 = var3.model();
        }

        String var6 = Optionull.map(var0, MinecraftProfileTexture::getUrl);
        MinecraftProfileTexture var7 = param1.cape();
        CompletableFuture<ResourceLocation> var8 = var7 != null ? this.capeTextures.getOrLoad(var7) : CompletableFuture.completedFuture(null);
        MinecraftProfileTexture var9 = param1.elytra();
        CompletableFuture<ResourceLocation> var10 = var9 != null ? this.elytraTextures.getOrLoad(var9) : CompletableFuture.completedFuture(null);
        return CompletableFuture.allOf(var1, var8, var10)
            .thenApply(param6 -> new PlayerSkin(var1.join(), var6, var8.join(), var10.join(), var2, param1.secure()));
    }

    @Nullable
    static Property getTextureProperty(GameProfile param0) {
        return Iterables.getFirst(param0.getProperties().get("textures"), null);
    }

    @OnlyIn(Dist.CLIENT)
    static record CacheKey(GameProfile profile) {
        @Override
        public boolean equals(Object param0) {
            if (!(param0 instanceof SkinManager.CacheKey)) {
                return false;
            } else {
                SkinManager.CacheKey var0 = (SkinManager.CacheKey)param0;
                return this.profile.getId().equals(var0.profile.getId()) && Objects.equals(this.texturesData(), var0.texturesData());
            }
        }

        @Override
        public int hashCode() {
            return this.profile.getId().hashCode() + Objects.hashCode(this.texturesData()) * 31;
        }

        @Nullable
        private String texturesData() {
            Property var0 = SkinManager.getTextureProperty(this.profile);
            return var0 != null ? var0.value() : null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class TextureCache {
        private final TextureManager textureManager;
        private final Path root;
        private final Type type;
        private final Map<String, CompletableFuture<ResourceLocation>> textures = new Object2ObjectOpenHashMap<>();

        TextureCache(TextureManager param0, Path param1, Type param2) {
            this.textureManager = param0;
            this.root = param1;
            this.type = param2;
        }

        public CompletableFuture<ResourceLocation> getOrLoad(MinecraftProfileTexture param0) {
            String var0 = param0.getHash();
            CompletableFuture<ResourceLocation> var1 = this.textures.get(var0);
            if (var1 == null) {
                var1 = this.registerTexture(param0);
                this.textures.put(var0, var1);
            }

            return var1;
        }

        private CompletableFuture<ResourceLocation> registerTexture(MinecraftProfileTexture param0) {
            String var0 = Hashing.sha1().hashUnencodedChars(param0.getHash()).toString();
            ResourceLocation var1 = this.getTextureLocation(var0);
            Path var2 = this.root.resolve(var0.length() > 2 ? var0.substring(0, 2) : "xx").resolve(var0);
            CompletableFuture<ResourceLocation> var3 = new CompletableFuture<>();
            HttpTexture var4 = new HttpTexture(
                var2.toFile(), param0.getUrl(), DefaultPlayerSkin.getDefaultTexture(), this.type == Type.SKIN, () -> var3.complete(var1)
            );
            this.textureManager.register(var1, var4);
            return var3;
        }

        private ResourceLocation getTextureLocation(String param0) {
            String var0 = switch(this.type) {
                case SKIN -> "skins";
                case CAPE -> "capes";
                case ELYTRA -> "elytra";
            };
            return new ResourceLocation(var0 + "/" + param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record TextureInfo(
        @Nullable MinecraftProfileTexture skin, @Nullable MinecraftProfileTexture cape, @Nullable MinecraftProfileTexture elytra, boolean secure
    ) {
        public static final SkinManager.TextureInfo EMPTY = new SkinManager.TextureInfo(null, null, null, true);

        public static SkinManager.TextureInfo unpack(Map<Type, MinecraftProfileTexture> param0, boolean param1) {
            return param0.isEmpty() ? EMPTY : new SkinManager.TextureInfo(param0.get(Type.SKIN), param0.get(Type.CAPE), param0.get(Type.ELYTRA), param1);
        }
    }
}
