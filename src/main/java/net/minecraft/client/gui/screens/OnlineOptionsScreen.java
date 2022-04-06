package net.minecraft.client.gui.screens;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OnlineOptionsScreen extends SimpleOptionsSubScreen {
    public OnlineOptionsScreen(Screen param0, Options param1) {
        super(
            param0, param1, new TranslatableComponent("options.online.title"), new OptionInstance[]{param1.realmsNotifications(), param1.allowServerListing()}
        );
    }

    @Override
    protected void createFooter() {
        if (this.minecraft.level != null) {
            CycleButton<Difficulty> var0 = this.addRenderableWidget(
                OptionsScreen.createDifficultyButton(this.smallOptions.length, this.width, this.height, "options.difficulty.online", this.minecraft)
            );
            var0.active = false;
        }

        super.createFooter();
    }
}
