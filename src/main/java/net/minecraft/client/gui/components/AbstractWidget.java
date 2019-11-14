package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWidget extends GuiComponent implements Widget, GuiEventListener {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final int NARRATE_DELAY_MOUSE = 750;
    private static final int NARRATE_DELAY_FOCUS = 200;
    protected int width;
    protected int height;
    public int x;
    public int y;
    private String message;
    private boolean wasHovered;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    protected long nextNarration = Long.MAX_VALUE;
    private boolean focused;

    public AbstractWidget(int param0, int param1, String param2) {
        this(param0, param1, 200, 20, param2);
    }

    public AbstractWidget(int param0, int param1, int param2, int param3, String param4) {
        this.x = param0;
        this.y = param1;
        this.width = param2;
        this.height = param3;
        this.message = param4;
    }

    protected int getYImage(boolean param0) {
        int var0 = 1;
        if (!this.active) {
            var0 = 0;
        } else if (param0) {
            var0 = 2;
        }

        return var0;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        if (this.visible) {
            this.isHovered = param0 >= this.x && param1 >= this.y && param0 < this.x + this.width && param1 < this.y + this.height;
            if (this.wasHovered != this.isHovered()) {
                if (this.isHovered()) {
                    if (this.focused) {
                        this.queueNarration(200);
                    } else {
                        this.queueNarration(750);
                    }
                } else {
                    this.nextNarration = Long.MAX_VALUE;
                }
            }

            if (this.visible) {
                this.renderButton(param0, param1, param2);
            }

            this.narrate();
            this.wasHovered = this.isHovered();
        }
    }

    protected void narrate() {
        if (this.active && this.isHovered() && Util.getMillis() > this.nextNarration) {
            String var0 = this.getNarrationMessage();
            if (!var0.isEmpty()) {
                NarratorChatListener.INSTANCE.sayNow(var0);
                this.nextNarration = Long.MAX_VALUE;
            }
        }

    }

    protected String getNarrationMessage() {
        return this.getMessage().isEmpty() ? "" : I18n.get("gui.narrate.button", this.getMessage());
    }

    public void renderButton(int param0, int param1, float param2) {
        Minecraft var0 = Minecraft.getInstance();
        Font var1 = var0.font;
        var0.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int var2 = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.blit(this.x, this.y, 0, 46 + var2 * 20, this.width / 2, this.height);
        this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + var2 * 20, this.width / 2, this.height);
        this.renderBg(var0, param0, param1);
        int var3 = 16777215;
        if (!this.active) {
            var3 = 10526880;
        } else if (this.isHovered()) {
            var3 = 16777120;
        }

        this.drawCenteredString(var1, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, var3 | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    protected void renderBg(Minecraft param0, int param1, int param2) {
    }

    public void onClick(double param0, double param1) {
    }

    public void onRelease(double param0, double param1) {
    }

    protected void onDrag(double param0, double param1, double param2, double param3) {
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(param2)) {
                boolean var0 = this.clicked(param0, param1);
                if (var0) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(param0, param1);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        if (this.isValidClickButton(param2)) {
            this.onRelease(param0, param1);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int param0) {
        return param0 == 0;
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (this.isValidClickButton(param2)) {
            this.onDrag(param0, param1, param3, param4);
            return true;
        } else {
            return false;
        }
    }

    protected boolean clicked(double param0, double param1) {
        return this.active
            && this.visible
            && param0 >= (double)this.x
            && param1 >= (double)this.y
            && param0 < (double)(this.x + this.width)
            && param1 < (double)(this.y + this.height);
    }

    public boolean isHovered() {
        return this.isHovered || this.focused;
    }

    @Override
    public boolean changeFocus(boolean param0) {
        if (this.active && this.visible) {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    protected void onFocusedChanged(boolean param0) {
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return this.active
            && this.visible
            && param0 >= (double)this.x
            && param1 >= (double)this.y
            && param0 < (double)(this.x + this.width)
            && param1 < (double)(this.y + this.height);
    }

    public void renderToolTip(int param0, int param1) {
    }

    public void playDownSound(SoundManager param0) {
        param0.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int param0) {
        this.width = param0;
    }

    public void setAlpha(float param0) {
        this.alpha = param0;
    }

    public void setMessage(String param0) {
        if (!Objects.equals(param0, this.message)) {
            this.queueNarration(250);
        }

        this.message = param0;
    }

    public void queueNarration(int param0) {
        this.nextNarration = Util.getMillis() + (long)param0;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isFocused() {
        return this.focused;
    }

    protected void setFocused(boolean param0) {
        this.focused = param0;
    }
}
