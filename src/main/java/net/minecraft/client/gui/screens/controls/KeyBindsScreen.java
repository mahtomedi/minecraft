package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
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
        this.keyBindsList = new KeyBindsList(this, this.minecraft);
        this.addWidget(this.keyBindsList);
        this.resetButton = this.addRenderableWidget(
            new Button(this.width / 2 - 155, this.height - 29, 150, 20, Component.translatable("controls.resetAll"), param0 -> {
                for(KeyMapping var0 : this.options.keyMappings) {
                    var0.setKey(var0.getDefaultKey());
                }
    
                KeyMapping.resetMapping();
            })
        );
        this.addRenderableWidget(
            new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.selectedKey != null) {
            this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(param2));
            this.selectedKey = null;
            KeyMapping.resetMapping();
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
            KeyMapping.resetMapping();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.keyBindsList.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        boolean var0 = false;

        for(KeyMapping var1 : this.options.keyMappings) {
            if (!var1.isDefault()) {
                var0 = true;
                break;
            }
        }

        this.resetButton.active = var0;
        super.render(param0, param1, param2, param3);
    }
}
