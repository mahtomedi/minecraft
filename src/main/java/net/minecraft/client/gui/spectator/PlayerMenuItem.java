package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerMenuItem implements SpectatorMenuItem {
    private final GameProfile profile;
    private final Supplier<PlayerSkin> skin;
    private final Component name;

    public PlayerMenuItem(GameProfile param0) {
        this.profile = param0;
        this.skin = Minecraft.getInstance().getSkinManager().lookupInsecure(param0);
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
    public void renderIcon(GuiGraphics param0, float param1, int param2) {
        param0.setColor(1.0F, 1.0F, 1.0F, (float)param2 / 255.0F);
        PlayerFaceRenderer.draw(param0, this.skin.get(), 2, 2, 12);
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
