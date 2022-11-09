package net.minecraft.client.resources;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DefaultPlayerSkin {
    private static final DefaultPlayerSkin.SkinType[] DEFAULT_SKINS = new DefaultPlayerSkin.SkinType[]{
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/alex.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/ari.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/efe.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/kai.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/makena.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/noor.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/steve.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/sunny.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/slim/zuri.png", DefaultPlayerSkin.ModelType.SLIM),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/alex.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/ari.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/efe.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/kai.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/makena.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/noor.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/steve.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/sunny.png", DefaultPlayerSkin.ModelType.WIDE),
        new DefaultPlayerSkin.SkinType("textures/entity/player/wide/zuri.png", DefaultPlayerSkin.ModelType.WIDE)
    };

    public static ResourceLocation getDefaultSkin() {
        return DEFAULT_SKINS[6].texture();
    }

    public static ResourceLocation getDefaultSkin(UUID param0) {
        return getSkinType(param0).texture;
    }

    public static String getSkinModelName(UUID param0) {
        return getSkinType(param0).model.id;
    }

    private static DefaultPlayerSkin.SkinType getSkinType(UUID param0) {
        return DEFAULT_SKINS[Math.floorMod(param0.hashCode(), DEFAULT_SKINS.length)];
    }

    @OnlyIn(Dist.CLIENT)
    static enum ModelType {
        SLIM("slim"),
        WIDE("default");

        final String id;

        private ModelType(String param0) {
            this.id = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record SkinType(ResourceLocation texture, DefaultPlayerSkin.ModelType model) {
        public SkinType(String param0, DefaultPlayerSkin.ModelType param1) {
            this(new ResourceLocation(param0), param1);
        }
    }
}
