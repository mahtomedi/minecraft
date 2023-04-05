package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisconnectedScreen extends Screen {
    private static final Component TO_SERVER_LIST = Component.translatable("gui.toMenu");
    private static final Component TO_TITLE = Component.translatable("gui.toTitle");
    private final Screen parent;
    private final Component reason;
    private final Component buttonText;
    private final GridLayout layout = new GridLayout();

    public DisconnectedScreen(Screen param0, Component param1, Component param2) {
        this(param0, param1, param2, TO_SERVER_LIST);
    }

    public DisconnectedScreen(Screen param0, Component param1, Component param2, Component param3) {
        super(param1);
        this.parent = param0;
        this.reason = param2;
        this.buttonText = param3;
    }

    @Override
    protected void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
        GridLayout.RowHelper var0 = this.layout.createRowHelper(1);
        var0.addChild(new StringWidget(this.title, this.font));
        var0.addChild(new MultiLineTextWidget(this.reason, this.font).setMaxWidth(this.width - 50).setCentered(true));
        Button var1;
        if (this.minecraft.allowsMultiplayer()) {
            var1 = Button.builder(this.buttonText, param0 -> this.minecraft.setScreen(this.parent)).build();
        } else {
            var1 = Button.builder(TO_TITLE, param0 -> this.minecraft.setScreen(new TitleScreen())).build();
        }

        var0.addChild(var1);
        this.layout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.title, this.reason);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
    }
}
