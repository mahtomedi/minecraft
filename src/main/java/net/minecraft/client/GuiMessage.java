package net.minecraft.client;

import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMessage {
    private final int addedTime;
    private final FormattedText message;
    private final int id;

    public GuiMessage(int param0, FormattedText param1, int param2) {
        this.message = param1;
        this.addedTime = param0;
        this.id = param2;
    }

    public FormattedText getMessage() {
        return this.message;
    }

    public int getAddedTime() {
        return this.addedTime;
    }

    public int getId() {
        return this.id;
    }
}
