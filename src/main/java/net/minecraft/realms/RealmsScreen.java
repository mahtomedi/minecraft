package net.minecraft.realms;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsScreen extends RealmsGuiEventListener implements RealmsConfirmResultListener {
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;
    private Minecraft minecraft;
    public int width;
    public int height;
    private final RealmsScreenProxy proxy = new RealmsScreenProxy(this);

    public RealmsScreenProxy getProxy() {
        return this.proxy;
    }

    public void init() {
    }

    public void init(Minecraft param0, int param1, int param2) {
        this.minecraft = param0;
    }

    public void drawCenteredString(String param0, int param1, int param2, int param3) {
        this.proxy.drawCenteredString(param0, param1, param2, param3);
    }

    public int draw(String param0, int param1, int param2, int param3, boolean param4) {
        return this.proxy.draw(param0, param1, param2, param3, param4);
    }

    public void drawString(String param0, int param1, int param2, int param3) {
        this.drawString(param0, param1, param2, param3, true);
    }

    public void drawString(String param0, int param1, int param2, int param3, boolean param4) {
        this.proxy.drawString(param0, param1, param2, param3, false);
    }

    public void blit(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.proxy.blit(param0, param1, param2, param3, param4, param5);
    }

    public static void blit(int param0, int param1, float param2, float param3, int param4, int param5, int param6, int param7, int param8, int param9) {
        GuiComponent.blit(param0, param1, param6, param7, param2, param3, param4, param5, param8, param9);
    }

    public static void blit(int param0, int param1, float param2, float param3, int param4, int param5, int param6, int param7) {
        GuiComponent.blit(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public void fillGradient(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.proxy.fillGradient(param0, param1, param2, param3, param4, param5);
    }

    public void renderBackground() {
        this.proxy.renderBackground();
    }

    public boolean isPauseScreen() {
        return this.proxy.isPauseScreen();
    }

    public void renderBackground(int param0) {
        this.proxy.renderBackground(param0);
    }

    public void render(int param0, int param1, float param2) {
        for(int var0 = 0; var0 < this.proxy.buttons().size(); ++var0) {
            this.proxy.buttons().get(var0).render(param0, param1, param2);
        }

    }

    public void renderTooltip(ItemStack param0, int param1, int param2) {
        this.proxy.renderTooltip(param0, param1, param2);
    }

    public void renderTooltip(String param0, int param1, int param2) {
        this.proxy.renderTooltip(param0, param1, param2);
    }

    public void renderTooltip(List<String> param0, int param1, int param2) {
        this.proxy.renderTooltip(param0, param1, param2);
    }

    public static void bind(String param0) {
        Realms.bind(param0);
    }

    public void tick() {
        this.tickButtons();
    }

    protected void tickButtons() {
        for(AbstractRealmsButton<?> var0 : this.buttons()) {
            var0.tick();
        }

    }

    public int width() {
        return this.proxy.width;
    }

    public int height() {
        return this.proxy.height;
    }

    public int fontLineHeight() {
        return this.proxy.fontLineHeight();
    }

    public int fontWidth(String param0) {
        return this.proxy.fontWidth(param0);
    }

    public void fontDrawShadow(String param0, int param1, int param2, int param3) {
        this.proxy.fontDrawShadow(param0, param1, param2, param3);
    }

    public List<String> fontSplit(String param0, int param1) {
        return this.proxy.fontSplit(param0, param1);
    }

    public void childrenClear() {
        this.proxy.childrenClear();
    }

    public void addWidget(RealmsGuiEventListener param0) {
        this.proxy.addWidget(param0);
    }

    public void removeWidget(RealmsGuiEventListener param0) {
        this.proxy.removeWidget(param0);
    }

    public boolean hasWidget(RealmsGuiEventListener param0) {
        return this.proxy.hasWidget(param0);
    }

    public void buttonsAdd(AbstractRealmsButton<?> param0) {
        this.proxy.buttonsAdd(param0);
    }

    public List<AbstractRealmsButton<?>> buttons() {
        return this.proxy.buttons();
    }

    protected void buttonsClear() {
        this.proxy.buttonsClear();
    }

    protected void focusOn(RealmsGuiEventListener param0) {
        this.proxy.magicalSpecialHackyFocus(param0.getProxy());
    }

    public RealmsEditBox newEditBox(int param0, int param1, int param2, int param3, int param4) {
        return this.newEditBox(param0, param1, param2, param3, param4, "");
    }

    public RealmsEditBox newEditBox(int param0, int param1, int param2, int param3, int param4, String param5) {
        return new RealmsEditBox(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void confirmResult(boolean param0, int param1) {
    }

    public static String getLocalizedString(String param0) {
        return Realms.getLocalizedString(param0);
    }

    public static String getLocalizedString(String param0, Object... param1) {
        return Realms.getLocalizedString(param0, param1);
    }

    public List<String> getLocalizedStringWithLineWidth(String param0, int param1) {
        return this.minecraft.font.split(I18n.get(param0), param1);
    }

    public RealmsAnvilLevelStorageSource getLevelStorageSource() {
        return new RealmsAnvilLevelStorageSource(Minecraft.getInstance().getLevelSource());
    }

    public void removed() {
    }

    protected void removeButton(RealmsButton param0) {
        this.proxy.removeButton(param0);
    }

    protected void setKeyboardHandlerSendRepeatsToGui(boolean param0) {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(param0);
    }

    protected boolean isKeyDown(int param0) {
        return InputConstants.isKeyDown(Minecraft.getInstance().window.getWindow(), param0);
    }

    protected void narrateLabels() {
        this.getProxy().narrateLabels();
    }

    public boolean isFocused(RealmsGuiEventListener param0) {
        return this.getProxy().getFocused() == param0.getProxy();
    }
}
