package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MobSkinTextureProcessor;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractClientPlayer extends Player {
    private PlayerInfo playerInfo;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public final MultiPlayerLevel clientLevel;

    public AbstractClientPlayer(MultiPlayerLevel param0, GameProfile param1) {
        super(param0, param1);
        this.clientLevel = param0;
    }

    @Override
    public boolean isSpectator() {
        PlayerInfo var0 = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return var0 != null && var0.getGameMode() == GameType.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        PlayerInfo var0 = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return var0 != null && var0.getGameMode() == GameType.CREATIVE;
    }

    public boolean isCapeLoaded() {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    protected PlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
        }

        return this.playerInfo;
    }

    public boolean isSkinLoaded() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 != null && var0.isSkinLoaded();
    }

    public ResourceLocation getSkinTextureLocation() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : var0.getSkinLocation();
    }

    @Nullable
    public ResourceLocation getCloakTextureLocation() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? null : var0.getCapeLocation();
    }

    public boolean isElytraLoaded() {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    public ResourceLocation getElytraTextureLocation() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? null : var0.getElytraLocation();
    }

    public static HttpTexture registerSkinTexture(ResourceLocation param0, String param1) {
        TextureManager var0 = Minecraft.getInstance().getTextureManager();
        TextureObject var1 = var0.getTexture(param0);
        if (var1 == null) {
            var1 = new HttpTexture(
                null,
                String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(param1)),
                DefaultPlayerSkin.getDefaultSkin(createPlayerUUID(param1)),
                new MobSkinTextureProcessor()
            );
            var0.register(param0, var1);
        }

        return (HttpTexture)var1;
    }

    public static ResourceLocation getSkinLocation(String param0) {
        return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(param0)));
    }

    public String getModelName() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : var0.getModelName();
    }

    public float getFieldOfViewModifier() {
        float var0 = 1.0F;
        if (this.abilities.flying) {
            var0 *= 1.1F;
        }

        AttributeInstance var1 = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        var0 = (float)((double)var0 * ((var1.getValue() / (double)this.abilities.getWalkingSpeed() + 1.0) / 2.0));
        if (this.abilities.getWalkingSpeed() == 0.0F || Float.isNaN(var0) || Float.isInfinite(var0)) {
            var0 = 1.0F;
        }

        if (this.isUsingItem() && this.getUseItem().getItem() == Items.BOW) {
            int var2 = this.getTicksUsingItem();
            float var3 = (float)var2 / 20.0F;
            if (var3 > 1.0F) {
                var3 = 1.0F;
            } else {
                var3 *= var3;
            }

            var0 *= 1.0F - var3 * 0.15F;
        }

        return var0;
    }
}
