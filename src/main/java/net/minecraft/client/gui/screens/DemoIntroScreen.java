package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DemoIntroScreen extends Screen {
    private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");

    public DemoIntroScreen() {
        super(new TranslatableComponent("demo.help.title"));
    }

    @Override
    protected void init() {
        int var0 = -16;
        this.addButton(new Button(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, I18n.get("demo.help.buy"), param0 -> {
            param0.active = false;
            Util.getPlatform().openUri("http://www.minecraft.net/store?source=demo");
        }));
        this.addButton(new Button(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, I18n.get("demo.help.later"), param0 -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));
    }

    @Override
    public void renderBackground() {
        super.renderBackground();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(DEMO_BACKGROUND_LOCATION);
        int var0 = (this.width - 248) / 2;
        int var1 = (this.height - 166) / 2;
        this.blit(var0, var1, 0, 0, 248, 166);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        int var0 = (this.width - 248) / 2 + 10;
        int var1 = (this.height - 166) / 2 + 8;
        this.font.draw(this.title.getColoredString(), (float)var0, (float)var1, 2039583);
        var1 += 12;
        Options var2 = this.minecraft.options;
        this.font
            .draw(
                I18n.get(
                    "demo.help.movementShort",
                    var2.keyUp.getTranslatedKeyMessage(),
                    var2.keyLeft.getTranslatedKeyMessage(),
                    var2.keyDown.getTranslatedKeyMessage(),
                    var2.keyRight.getTranslatedKeyMessage()
                ),
                (float)var0,
                (float)var1,
                5197647
            );
        this.font.draw(I18n.get("demo.help.movementMouse"), (float)var0, (float)(var1 + 12), 5197647);
        this.font.draw(I18n.get("demo.help.jump", var2.keyJump.getTranslatedKeyMessage()), (float)var0, (float)(var1 + 24), 5197647);
        this.font.draw(I18n.get("demo.help.inventory", var2.keyInventory.getTranslatedKeyMessage()), (float)var0, (float)(var1 + 36), 5197647);
        this.font.drawWordWrap(I18n.get("demo.help.fullWrapped"), var0, var1 + 68, 218, 2039583);
        super.render(param0, param1, param2);
    }
}
