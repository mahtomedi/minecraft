package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
    private final Component reason;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Screen parent;
    private int textHeight;

    public DisconnectedRealmsScreen(Screen param0, Component param1, Component param2) {
        super(param1);
        this.parent = param0;
        this.reason = param2;
    }

    @Override
    public void init() {
        Minecraft var0 = Minecraft.getInstance();
        var0.setConnectedToRealms(false);
        var0.getClientPackSource().clearServerPack();
        this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
        this.textHeight = this.message.getLineCount() * 9;
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 100, this.height / 2 + this.textHeight / 2 + 9, 200, 20, CommonComponents.GUI_BACK, param1 -> var0.setScreen(this.parent)
            )
        );
    }

    @Override
    public Component getNarrationMessage() {
        return Component.empty().append(this.title).append(": ").append(this.reason);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
        this.message.renderCentered(param0, this.width / 2, this.height / 2 - this.textHeight / 2);
        super.render(param0, param1, param2, param3);
    }
}
