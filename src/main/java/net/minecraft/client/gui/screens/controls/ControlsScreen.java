package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
    public KeyMapping selectedKey;
    public long lastKeySelection;
    private ControlList controlList;
    private Button resetButton;

    public ControlsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("controls.title"));
    }

    @Override
    protected void init() {
        this.addButton(
            new Button(
                this.width / 2 - 155,
                18,
                150,
                20,
                new TranslatableComponent("options.mouse_settings"),
                param0 -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))
            )
        );
        this.addButton(Option.AUTO_JUMP.createButton(this.options, this.width / 2 - 155 + 160, 18, 150));
        this.controlList = new ControlList(this, this.minecraft);
        this.children.add(this.controlList);
        this.resetButton = this.addButton(
            new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("controls.resetAll"), param0 -> {
                for(KeyMapping var0 : this.options.keyMappings) {
                    var0.setKey(var0.getDefaultKey());
                }
    
                KeyMapping.resetMapping();
            })
        );
        this.addButton(
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
        this.controlList.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
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
