package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
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
    private final LinearLayout layout = LinearLayout.vertical();

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
        this.layout.addChild(new StringWidget(this.title, this.font));
        this.layout.addChild(new MultiLineTextWidget(this.reason, this.font).setMaxWidth(this.width - 50).setCentered(true));
        Button var0;
        if (this.minecraft.allowsMultiplayer()) {
            var0 = Button.builder(this.buttonText, param0 -> this.minecraft.setScreen(this.parent)).build();
        } else {
            var0 = Button.builder(TO_TITLE, param0 -> this.minecraft.setScreen(new TitleScreen())).build();
        }

        this.layout.addChild(var0);
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
}
