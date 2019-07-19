package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsEditBox extends RealmsGuiEventListener {
    private final EditBox editBox;

    public RealmsEditBox(int param0, int param1, int param2, int param3, int param4, String param5) {
        this.editBox = new EditBox(Minecraft.getInstance().font, param1, param2, param3, param4, null, param5);
    }

    public String getValue() {
        return this.editBox.getValue();
    }

    public void tick() {
        this.editBox.tick();
    }

    public void setValue(String param0) {
        this.editBox.setValue(param0);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        return this.editBox.charTyped(param0, param1);
    }

    @Override
    public GuiEventListener getProxy() {
        return this.editBox;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        return this.editBox.keyPressed(param0, param1, param2);
    }

    public boolean isFocused() {
        return this.editBox.isFocused();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.editBox.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return this.editBox.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return this.editBox.mouseDragged(param0, param1, param2, param3, param4);
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return this.editBox.mouseScrolled(param0, param1, param2);
    }

    public void render(int param0, int param1, float param2) {
        this.editBox.render(param0, param1, param2);
    }

    public void setMaxLength(int param0) {
        this.editBox.setMaxLength(param0);
    }

    public void setIsEditable(boolean param0) {
        this.editBox.setEditable(param0);
    }
}
