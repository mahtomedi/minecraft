package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DatapackLoadFailureScreen extends Screen {
    private final List<FormattedText> lines = Lists.newArrayList();
    private final Runnable callback;

    public DatapackLoadFailureScreen(Runnable param0) {
        super(new TranslatableComponent("datapackFailure.title"));
        this.callback = param0;
    }

    @Override
    protected void init() {
        super.init();
        this.lines.clear();
        this.lines.addAll(this.font.split(this.getTitle(), this.width - 50));
        this.addButton(
            new Button(
                this.width / 2 - 155, this.height / 6 + 96, 150, 20, new TranslatableComponent("datapackFailure.safeMode"), param0 -> this.callback.run()
            )
        );
        this.addButton(
            new Button(
                this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, new TranslatableComponent("gui.toTitle"), param0 -> this.minecraft.setScreen(null)
            )
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        int var0 = 70;

        for(FormattedText var1 : this.lines) {
            this.drawCenteredString(param0, this.font, var1, this.width / 2, var0, 16777215);
            var0 += 9;
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
