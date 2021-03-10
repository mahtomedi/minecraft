package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DemoIntroScreen extends Screen {
    private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");
    private MultiLineLabel movementMessage = MultiLineLabel.EMPTY;
    private MultiLineLabel durationMessage = MultiLineLabel.EMPTY;

    public DemoIntroScreen() {
        super(new TranslatableComponent("demo.help.title"));
    }

    @Override
    protected void init() {
        int var0 = -16;
        this.addButton(new Button(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, new TranslatableComponent("demo.help.buy"), param0 -> {
            param0.active = false;
            Util.getPlatform().openUri("http://www.minecraft.net/store?source=demo");
        }));
        this.addButton(new Button(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, new TranslatableComponent("demo.help.later"), param0 -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));
        Options var1 = this.minecraft.options;
        this.movementMessage = MultiLineLabel.create(
            this.font,
            new TranslatableComponent(
                "demo.help.movementShort",
                var1.keyUp.getTranslatedKeyMessage(),
                var1.keyLeft.getTranslatedKeyMessage(),
                var1.keyDown.getTranslatedKeyMessage(),
                var1.keyRight.getTranslatedKeyMessage()
            ),
            new TranslatableComponent("demo.help.movementMouse"),
            new TranslatableComponent("demo.help.jump", var1.keyJump.getTranslatedKeyMessage()),
            new TranslatableComponent("demo.help.inventory", var1.keyInventory.getTranslatedKeyMessage())
        );
        this.durationMessage = MultiLineLabel.create(this.font, new TranslatableComponent("demo.help.fullWrapped"), 218);
    }

    @Override
    public void renderBackground(PoseStack param0) {
        super.renderBackground(param0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, DEMO_BACKGROUND_LOCATION);
        int var0 = (this.width - 248) / 2;
        int var1 = (this.height - 166) / 2;
        this.blit(param0, var0, var1, 0, 0, 248, 166);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        int var0 = (this.width - 248) / 2 + 10;
        int var1 = (this.height - 166) / 2 + 8;
        this.font.draw(param0, this.title, (float)var0, (float)var1, 2039583);
        var1 = this.movementMessage.renderLeftAlignedNoShadow(param0, var0, var1 + 12, 12, 5197647);
        this.durationMessage.renderLeftAlignedNoShadow(param0, var0, var1 + 20, 9, 2039583);
        super.render(param0, param1, param2, param3);
    }
}
