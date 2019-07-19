package net.minecraft.client;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMessage {
    private final int addedTime;
    private final Component message;
    private final int id;

    public GuiMessage(int param0, Component param1, int param2) {
        this.message = param1;
        this.addedTime = param0;
        this.id = param2;
    }

    public Component getMessage() {
        return this.message;
    }

    public int getAddedTime() {
        return this.addedTime;
    }

    public int getId() {
        return this.id;
    }
}
