package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MouseSettingsScreen extends OptionsSubScreen {
    private OptionsList list;

    private static OptionInstance<?>[] options(Options param0) {
        return new OptionInstance[]{
            param0.sensitivity(), param0.invertYMouse(), param0.mouseWheelSensitivity(), param0.discreteMouseScroll(), param0.touchscreen()
        };
    }

    public MouseSettingsScreen(Screen param0, Options param1) {
        super(param0, param1, Component.translatable("options.mouse_settings.title"));
    }

    @Override
    protected void init() {
        this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height - 64, 32, 25));
        if (InputConstants.isRawMouseInputSupported()) {
            this.list
                .addSmall(
                    Stream.concat(Arrays.stream(options(this.options)), Stream.of(this.options.rawMouseInput())).toArray(param0 -> new OptionInstance[param0])
                );
        } else {
            this.list.addSmall(options(this.options));
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> {
            this.options.save();
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 5, 16777215);
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
    }
}
