package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenericDirtMessageScreen extends Screen {
    public GenericDirtMessageScreen(Component param0) {
        super(param0);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 70, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
