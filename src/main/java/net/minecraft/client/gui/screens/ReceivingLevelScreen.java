package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReceivingLevelScreen extends Screen {
    public ReceivingLevelScreen() {
        super(NarratorChatListener.NO_TITLE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderDirtBackground(0);
        this.drawCenteredString(this.font, I18n.get("multiplayer.downloadingTerrain"), this.width / 2, this.height / 2 - 50, 16777215);
        super.render(param0, param1, param2);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
