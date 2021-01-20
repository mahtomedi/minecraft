package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AlertScreen extends Screen {
    private final Runnable callback;
    protected final Component text;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected final Component okButton;

    public AlertScreen(Runnable param0, Component param1, Component param2) {
        this(param0, param1, param2, CommonComponents.GUI_BACK);
    }

    public AlertScreen(Runnable param0, Component param1, Component param2, Component param3) {
        super(param1);
        this.callback = param0;
        this.text = param2;
        this.okButton = param3;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.okButton, param0 -> this.callback.run()));
        this.message = MultiLineLabel.create(this.font, this.text, this.width - 50);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 70, 16777215);
        this.message.renderCentered(param0, this.width / 2, 90);
        super.render(param0, param1, param2, param3);
    }
}
