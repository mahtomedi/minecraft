package net.minecraft.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DemoIntroScreen extends Screen {
    private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");
    private MultiLineLabel movementMessage = MultiLineLabel.EMPTY;
    private MultiLineLabel durationMessage = MultiLineLabel.EMPTY;

    public DemoIntroScreen() {
        super(Component.translatable("demo.help.title"));
    }

    @Override
    protected void init() {
        int var0 = -16;
        this.addRenderableWidget(Button.builder(Component.translatable("demo.help.buy"), param0 -> {
            param0.active = false;
            Util.getPlatform().openUri("https://aka.ms/BuyMinecraftJava");
        }).bounds(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("demo.help.later"), param0 -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }).bounds(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20).build());
        Options var1 = this.minecraft.options;
        this.movementMessage = MultiLineLabel.create(
            this.font,
            Component.translatable(
                "demo.help.movementShort",
                var1.keyUp.getTranslatedKeyMessage(),
                var1.keyLeft.getTranslatedKeyMessage(),
                var1.keyDown.getTranslatedKeyMessage(),
                var1.keyRight.getTranslatedKeyMessage()
            ),
            Component.translatable("demo.help.movementMouse"),
            Component.translatable("demo.help.jump", var1.keyJump.getTranslatedKeyMessage()),
            Component.translatable("demo.help.inventory", var1.keyInventory.getTranslatedKeyMessage())
        );
        this.durationMessage = MultiLineLabel.create(this.font, Component.translatable("demo.help.fullWrapped"), 218);
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        super.renderBackground(param0, param1, param2, param3);
        int var0 = (this.width - 248) / 2;
        int var1 = (this.height - 166) / 2;
        param0.blit(DEMO_BACKGROUND_LOCATION, var0, var1, 0, 0, 248, 166);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        int var0 = (this.width - 248) / 2 + 10;
        int var1 = (this.height - 166) / 2 + 8;
        param0.drawString(this.font, this.title, var0, var1, 2039583, false);
        var1 = this.movementMessage.renderLeftAlignedNoShadow(param0, var0, var1 + 12, 12, 5197647);
        this.durationMessage.renderLeftAlignedNoShadow(param0, var0, var1 + 20, 9, 2039583);
    }
}
