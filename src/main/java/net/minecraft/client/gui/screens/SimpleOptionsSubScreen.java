package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SimpleOptionsSubScreen extends OptionsSubScreen {
    protected final OptionInstance<?>[] smallOptions;
    @Nullable
    private AbstractWidget narratorButton;
    protected OptionsList list;

    public SimpleOptionsSubScreen(Screen param0, Options param1, Component param2, OptionInstance<?>[] param3) {
        super(param0, param1, param2);
        this.smallOptions = param3;
    }

    @Override
    protected void init() {
        this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height - 64, 32, 25));
        this.list.addSmall(this.smallOptions);
        this.createFooter();
        this.narratorButton = this.list.findOption(this.options.narrator());
        if (this.narratorButton != null) {
            this.narratorButton.active = this.minecraft.getNarrator().isActive();
        }

    }

    protected void createFooter() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
    }

    public void updateNarratorButton() {
        if (this.narratorButton instanceof CycleButton) {
            ((CycleButton)this.narratorButton).setValue(this.options.narrator().get());
        }

    }
}
