package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerMenuItem implements SpectatorMenuItem {
    private final GameProfile profile;
    private final ResourceLocation location;
    private final Component name;

    public PlayerMenuItem(GameProfile param0) {
        this.profile = param0;
        Minecraft var0 = Minecraft.getInstance();
        this.location = var0.getSkinManager().getInsecureSkinLocation(param0);
        this.name = Component.literal(param0.getName());
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
        RenderSystem.setShaderTexture(0, this.location);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)param2 / 255.0F);
        PlayerFaceRenderer.draw(param0, 2, 2, 12);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
