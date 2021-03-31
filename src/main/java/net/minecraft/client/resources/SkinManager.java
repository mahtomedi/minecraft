package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinManager {
    public static final String PROPERTY_TEXTURES = "textures";
    private final TextureManager textureManager;
    private final File skinsDirectory;
    private final MinecraftSessionService sessionService;
    private final LoadingCache<String, Map<Type, MinecraftProfileTexture>> insecureSkinCache;

    public SkinManager(TextureManager param0, File param1, final MinecraftSessionService param2) {
        this.textureManager = param0;
        this.skinsDirectory = param1;
        this.sessionService = param2;
        this.insecureSkinCache = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.SECONDS)
            .build(new CacheLoader<String, Map<Type, MinecraftProfileTexture>>() {
                public Map<Type, MinecraftProfileTexture> load(String param0) {
                    GameProfile var0 = new GameProfile(null, "dummy_mcdummyface");
                    var0.getProperties().put("textures", new Property("textures", param0, ""));
    
                    try {
                        return param2.getTextures(var0, false);
                    } catch (Throwable var4) {
                        return ImmutableMap.of();
                    }
                }
            });
    }

    public ResourceLocation registerTexture(MinecraftProfileTexture param0, Type param1) {
        return this.registerTexture(param0, param1, null);
    }

    private ResourceLocation registerTexture(MinecraftProfileTexture param0, Type param1, @Nullable SkinManager.SkinTextureCallback param2) {
        String var0 = Hashing.sha1().hashUnencodedChars(param0.getHash()).toString();
        ResourceLocation var1 = new ResourceLocation("skins/" + var0);
        AbstractTexture var2 = this.textureManager.getTexture(var1, MissingTextureAtlasSprite.getTexture());
        if (var2 == MissingTextureAtlasSprite.getTexture()) {
            File var3 = new File(this.skinsDirectory, var0.length() > 2 ? var0.substring(0, 2) : "xx");
            File var4 = new File(var3, var0);
            HttpTexture var5 = new HttpTexture(var4, param0.getUrl(), DefaultPlayerSkin.getDefaultSkin(), param1 == Type.SKIN, () -> {
                if (param2 != null) {
                    param2.onSkinTextureAvailable(param1, var1, param0);
                }

            });
            this.textureManager.register(var1, var5);
        } else if (param2 != null) {
            param2.onSkinTextureAvailable(param1, var1, param0);
        }

        return var1;
    }

    public void registerSkins(GameProfile param0, SkinManager.SkinTextureCallback param1, boolean param2) {
        Runnable var0 = () -> {
            Map<Type, MinecraftProfileTexture> var0x = Maps.newHashMap();

            try {
                var0x.putAll(this.sessionService.getTextures(param0, param2));
            } catch (InsecureTextureException var7) {
            }

            if (var0x.isEmpty()) {
                param0.getProperties().clear();
                if (param0.getId().equals(Minecraft.getInstance().getUser().getGameProfile().getId())) {
                    param0.getProperties().putAll(Minecraft.getInstance().getProfileProperties());
                    var0x.putAll(this.sessionService.getTextures(param0, false));
                } else {
                    this.sessionService.fillProfileProperties(param0, param2);

                    try {
                        var0x.putAll(this.sessionService.getTextures(param0, param2));
                    } catch (InsecureTextureException var6) {
                    }
                }
            }

            Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(() -> ImmutableList.of(Type.SKIN, Type.CAPE).forEach(param2x -> {
                        if (var0x.containsKey(param2x)) {
                            this.registerTexture(var0x.get(param2x), param2x, param1);
                        }

                    })));
        };
        Util.backgroundExecutor().execute(var0);
    }

    public Map<Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile param0) {
        Property var0 = Iterables.getFirst(param0.getProperties().get("textures"), null);
        return (Map<Type, MinecraftProfileTexture>)(var0 == null ? ImmutableMap.of() : this.insecureSkinCache.getUnchecked(var0.getValue()));
    }

    @OnlyIn(Dist.CLIENT)
    public interface SkinTextureCallback {
        void onSkinTextureAvailable(Type var1, ResourceLocation var2, MinecraftProfileTexture var3);
    }
}
