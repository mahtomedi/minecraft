package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyBindsScreen extends OptionsSubScreen {
    @Nullable
    public KeyMapping selectedKey;
    public long lastKeySelection;
    private KeyBindsList keyBindsList;
    private Button resetButton;

    public KeyBindsScreen(Screen param0, Options param1) {
        super(param0, param1, Component.translatable("controls.keybinds.title"));
    }

    @Override
    protected void init() {
        this.keyBindsList = this.addRenderableWidget(new KeyBindsList(this, this.minecraft));
        this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("controls.resetAll"), param0 -> {
            for(KeyMapping var0 : this.options.keyMappings) {
                var0.setKey(var0.getDefaultKey());
            }

            this.keyBindsList.resetMappingAndUpdateButtons();
        }).bounds(this.width / 2 - 155, this.height - 29, 150, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
                .build()
        );
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.selectedKey != null) {
            this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(param2));
            this.selectedKey = null;
            this.keyBindsList.resetMappingAndUpdateButtons();
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.selectedKey != null) {
            if (param0 == 256) {
                this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
            } else {
                this.options.setKey(this.selectedKey, InputConstants.getKey(param0, param1));
            }

            this.selectedKey = null;
            this.lastKeySelection = Util.getMillis();
            this.keyBindsList.resetMappingAndUpdateButtons();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        boolean var0 = false;

        for(KeyMapping var1 : this.options.keyMappings) {
            if (!var1.isDefault()) {
                var0 = true;
                break;
            }
        }

        this.resetButton.active = var0;
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
    }
}
