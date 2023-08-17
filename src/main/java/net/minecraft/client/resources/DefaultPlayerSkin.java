package net.minecraft.client.resources;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DefaultPlayerSkin {
    private static final PlayerSkin[] DEFAULT_SKINS = new PlayerSkin[]{
        create("textures/entity/player/slim/alex.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/ari.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/efe.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/kai.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/makena.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/noor.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/steve.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/sunny.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/zuri.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/wide/alex.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/ari.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/efe.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/kai.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/makena.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/noor.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/steve.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/sunny.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/zuri.png", PlayerSkin.Model.WIDE)
    };

    public static ResourceLocation getDefaultTexture() {
        return DEFAULT_SKINS[6].texture();
    }

    public static PlayerSkin get(UUID param0) {
        return DEFAULT_SKINS[Math.floorMod(param0.hashCode(), DEFAULT_SKINS.length)];
    }

    public static PlayerSkin get(GameProfile param0) {
        return get(param0.getId());
    }

    private static PlayerSkin create(String param0, PlayerSkin.Model param1) {
        return new PlayerSkin(new ResourceLocation(param0), null, null, null, param1, true);
    }
}
