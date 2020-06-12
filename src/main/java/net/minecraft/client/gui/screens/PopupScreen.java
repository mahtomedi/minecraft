package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PopupScreen extends Screen {
    private final FormattedText message;
    private final ImmutableList<PopupScreen.ButtonOption> buttonOptions;
    private List<FormattedText> messageLines;
    private int contentTop;
    private int buttonWidth;

    protected PopupScreen(Component param0, List<FormattedText> param1, ImmutableList<PopupScreen.ButtonOption> param2) {
        super(param0);
        this.message = FormattedText.composite(param1);
        this.buttonOptions = param2;
    }

    @Override
    public String getNarrationMessage() {
        return super.getNarrationMessage() + ". " + this.message.getString();
    }

    @Override
    public void init(Minecraft param0, int param1, int param2) {
        super.init(param0, param1, param2);

        for(PopupScreen.ButtonOption var0 : this.buttonOptions) {
            this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(var0.message) + 20);
        }

        int var1 = 5 + this.buttonWidth + 5;
        int var2 = var1 * this.buttonOptions.size();
        this.messageLines = this.font.split(this.message, var2);
        int var3 = this.messageLines.size() * 9;
        this.contentTop = (int)((double)param2 / 2.0 - (double)var3 / 2.0);
        int var4 = this.contentTop + var3 + 9 * 2;
        int var5 = (int)((double)param1 / 2.0 - (double)var2 / 2.0);

        for(PopupScreen.ButtonOption var6 : this.buttonOptions) {
            this.addButton(new Button(var5, var4, this.buttonWidth, 20, var6.message, var6.onPress));
            var5 += var1;
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, this.contentTop - 9 * 2, -1);
        int var0 = this.contentTop;

        for(FormattedText var1 : this.messageLines) {
            this.drawCenteredString(param0, this.font, var1, this.width / 2, var0, -1);
            var0 += 9;
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class ButtonOption {
        private final Component message;
        private final Button.OnPress onPress;

        public ButtonOption(Component param0, Button.OnPress param1) {
            this.message = param0;
            this.onPress = param1;
        }
    }
}
