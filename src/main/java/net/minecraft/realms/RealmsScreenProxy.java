package net.minecraft.realms;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsScreenProxy extends Screen {
    private final RealmsScreen screen;
    private static final Logger LOGGER = LogManager.getLogger();

    public RealmsScreenProxy(RealmsScreen param0) {
        super(NarratorChatListener.NO_TITLE);
        this.screen = param0;
    }

    public RealmsScreen getScreen() {
        return this.screen;
    }

    @Override
    public void init(Minecraft param0, int param1, int param2) {
        this.screen.init(param0, param1, param2);
        super.init(param0, param1, param2);
    }

    @Override
    public void init() {
        this.screen.init();
        super.init();
    }

    public void drawCenteredString(String param0, int param1, int param2, int param3) {
        super.drawCenteredString(this.font, param0, param1, param2, param3);
    }

    public void drawString(String param0, int param1, int param2, int param3, boolean param4) {
        if (param4) {
            super.drawString(this.font, param0, param1, param2, param3);
        } else {
            this.font.draw(param0, (float)param1, (float)param2, param3);
        }

    }

    @Override
    public void blit(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.screen.blit(param0, param1, param2, param3, param4, param5);
        super.blit(param0, param1, param2, param3, param4, param5);
    }

    public static void blit(int param0, int param1, float param2, float param3, int param4, int param5, int param6, int param7, int param8, int param9) {
        GuiComponent.blit(param0, param1, param6, param7, param2, param3, param4, param5, param8, param9);
    }

    public static void blit(int param0, int param1, float param2, float param3, int param4, int param5, int param6, int param7) {
        GuiComponent.blit(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    @Override
    public void fillGradient(int param0, int param1, int param2, int param3, int param4, int param5) {
        super.fillGradient(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void renderBackground() {
        super.renderBackground();
    }

    @Override
    public boolean isPauseScreen() {
        return super.isPauseScreen();
    }

    @Override
    public void renderBackground(int param0) {
        super.renderBackground(param0);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.screen.render(param0, param1, param2);
    }

    @Override
    public void renderTooltip(ItemStack param0, int param1, int param2) {
        super.renderTooltip(param0, param1, param2);
    }

    @Override
    public void renderTooltip(String param0, int param1, int param2) {
        super.renderTooltip(param0, param1, param2);
    }

    @Override
    public void renderTooltip(List<String> param0, int param1, int param2) {
        super.renderTooltip(param0, param1, param2);
    }

    @Override
    public void tick() {
        this.screen.tick();
        super.tick();
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int fontLineHeight() {
        return 9;
    }

    public int fontWidth(String param0) {
        return this.font.width(param0);
    }

    public void fontDrawShadow(String param0, int param1, int param2, int param3) {
        this.font.drawShadow(param0, (float)param1, (float)param2, param3);
    }

    public List<String> fontSplit(String param0, int param1) {
        return this.font.split(param0, param1);
    }

    public void childrenClear() {
        this.children.clear();
    }

    public void addWidget(RealmsGuiEventListener param0) {
        if (this.hasWidget(param0) || !this.children.add(param0.getProxy())) {
            LOGGER.error("Tried to add the same widget multiple times: " + param0);
        }

    }

    public void narrateLabels() {
        List<String> var0 = this.children
            .stream()
            .filter(param0 -> param0 instanceof RealmsLabelProxy)
            .map(param0 -> ((RealmsLabelProxy)param0).getLabel().getText())
            .collect(Collectors.toList());
        Realms.narrateNow(var0);
    }

    public void removeWidget(RealmsGuiEventListener param0) {
        if (!this.hasWidget(param0) || !this.children.remove(param0.getProxy())) {
            LOGGER.error("Tried to add the same widget multiple times: " + param0);
        }

    }

    public boolean hasWidget(RealmsGuiEventListener param0) {
        return this.children.contains(param0.getProxy());
    }

    public void buttonsAdd(AbstractRealmsButton<?> param0) {
        this.addButton(param0.getProxy());
    }

    public List<AbstractRealmsButton<?>> buttons() {
        List<AbstractRealmsButton<?>> var0 = Lists.newArrayListWithExpectedSize(this.buttons.size());

        for(AbstractWidget var1 : this.buttons) {
            var0.add(((RealmsAbstractButtonProxy)var1).getButton());
        }

        return var0;
    }

    public void buttonsClear() {
        Set<GuiEventListener> var0 = Sets.newHashSet(this.buttons);
        this.children.removeIf(var0::contains);
        this.buttons.clear();
    }

    public void removeButton(RealmsButton param0) {
        this.children.remove(param0.getProxy());
        this.buttons.remove(param0.getProxy());
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.screen.mouseClicked(param0, param1, param2) ? true : super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return this.screen.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return this.screen.mouseDragged(param0, param1, param2, param3, param4) ? true : super.mouseDragged(param0, param1, param2, param3, param4);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        return this.screen.keyPressed(param0, param1, param2) ? true : super.keyPressed(param0, param1, param2);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        return this.screen.charTyped(param0, param1) ? true : super.charTyped(param0, param1);
    }

    @Override
    public void removed() {
        this.screen.removed();
        super.removed();
    }

    public int draw(String param0, int param1, int param2, int param3, boolean param4) {
        return param4 ? this.font.drawShadow(param0, (float)param1, (float)param2, param3) : this.font.draw(param0, (float)param1, (float)param2, param3);
    }

    public Font getFont() {
        return this.font;
    }
}
