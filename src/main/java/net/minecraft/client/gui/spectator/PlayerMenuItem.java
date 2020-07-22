package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerMenuItem implements SpectatorMenuItem {
    private final GameProfile profile;
    private final ResourceLocation location;
    private final TextComponent name;

    public PlayerMenuItem(GameProfile param0) {
        this.profile = param0;
        Minecraft var0 = Minecraft.getInstance();
        Map<Type, MinecraftProfileTexture> var1 = var0.getSkinManager().getInsecureSkinInformation(param0);
        if (var1.containsKey(Type.SKIN)) {
            this.location = var0.getSkinManager().registerTexture(var1.get(Type.SKIN), Type.SKIN);
        } else {
            this.location = DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(param0));
        }

        this.name = new TextComponent(param0.getName());
    }

    @Override
    public void selectItem(SpectatorMenu param0) {
        Minecraft.getInstance().getConnection().send(new ServerboundTeleportToEntityPacket(this.profile.getId()));
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public void renderIcon(PoseStack param0, float param1, int param2) {
        Minecraft.getInstance().getTextureManager().bind(this.location);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, (float)param2 / 255.0F);
        GuiComponent.blit(param0, 2, 2, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
        GuiComponent.blit(param0, 2, 2, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
