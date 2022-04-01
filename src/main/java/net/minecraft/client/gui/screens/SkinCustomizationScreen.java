package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinCustomizationScreen extends OptionsSubScreen {
    public SkinCustomizationScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.skinCustomisation.title"));
    }

    @Override
    protected void init() {
        int var0 = 0;

        for(PlayerModelPart var1 : PlayerModelPart.values()) {
            this.addRenderableWidget(
                CycleButton.onOffBuilder(this.options.isModelPartEnabled(var1))
                    .create(
                        this.width / 2 - 155 + var0 % 2 * 160,
                        this.height / 6 + 24 * (var0 >> 1),
                        150,
                        20,
                        var1.getName(),
                        (param1, param2) -> this.options.toggleModelPart(var1, param2)
                    )
            );
            ++var0;
        }

        this.addRenderableWidget(Option.MAIN_HAND.createButton(this.options, this.width / 2 - 155 + var0 % 2 * 160, this.height / 6 + 24 * (var0 >> 1), 150));
        if (++var0 % 2 == 1) {
            ++var0;
        }

        this.addRenderableWidget(
            new Button(
                this.width / 2 - 100,
                this.height / 6 + 24 * (var0 >> 1),
                200,
                20,
                CommonComponents.GUI_DONE,
                param0 -> this.minecraft.setScreen(this.lastScreen)
            )
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
