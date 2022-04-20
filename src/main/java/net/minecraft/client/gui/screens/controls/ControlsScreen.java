package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
    private static final int ROW_SPACING = 24;

    public ControlsScreen(Screen param0, Options param1) {
        super(param0, param1, Component.translatable("controls.title"));
    }

    @Override
    protected void init() {
        super.init();
        int var0 = this.width / 2 - 155;
        int var1 = var0 + 160;
        int var2 = this.height / 6 - 12;
        this.addRenderableWidget(
            new Button(
                var0,
                var2,
                150,
                20,
                Component.translatable("options.mouse_settings"),
                param0 -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))
            )
        );
        this.addRenderableWidget(
            new Button(
                var1, var2, 150, 20, Component.translatable("controls.keybinds"), param0 -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options))
            )
        );
        var2 += 24;
        this.addRenderableWidget(this.options.toggleCrouch().createButton(this.options, var0, var2, 150));
        this.addRenderableWidget(this.options.toggleSprint().createButton(this.options, var1, var2, 150));
        var2 += 24;
        this.addRenderableWidget(this.options.autoJump().createButton(this.options, var0, var2, 150));
        var2 += 24;
        this.addRenderableWidget(
            new Button(this.width / 2 - 100, var2, 200, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
