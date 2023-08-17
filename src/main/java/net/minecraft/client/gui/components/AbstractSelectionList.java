package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSelectionList<E extends AbstractSelectionList.Entry<E>>
    extends AbstractContainerEventHandler
    implements Renderable,
    NarratableEntry {
    protected static final int SCROLLBAR_WIDTH = 6;
    private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("widget/scroller");
    protected final Minecraft minecraft;
    protected final int itemHeight;
    private final List<E> children = new AbstractSelectionList.TrackedList();
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected boolean centerListVertically = true;
    private double scrollAmount;
    private boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;
    @Nullable
    private E selected;
    private boolean renderBackground = true;
    @Nullable
    private E hovered;

    public AbstractSelectionList(Minecraft param0, int param1, int param2, int param3, int param4, int param5) {
        this.minecraft = param0;
        this.width = param1;
        this.height = param2;
        this.y0 = param3;
        this.y1 = param4;
        this.itemHeight = param5;
        this.x0 = 0;
        this.x1 = param1;
    }

    protected void setRenderHeader(boolean param0, int param1) {
        this.renderHeader = param0;
        this.headerHeight = param1;
        if (!param0) {
            this.headerHeight = 0;
        }

    }

    public int getRowWidth() {
        return 220;
    }

    @Nullable
    public E getSelected() {
        return this.selected;
    }

    public void setSelected(@Nullable E param0) {
        this.selected = param0;
    }

    public E getFirstElement() {
        return this.children.get(0);
    }

    public void setRenderBackground(boolean param0) {
        this.renderBackground = param0;
    }

    @Nullable
    public E getFocused() {
        return (E)super.getFocused();
    }

    @Override
    public final List<E> children() {
        return this.children;
    }

    protected void clearEntries() {
        this.children.clear();
        this.selected = null;
    }

    protected void replaceEntries(Collection<E> param0) {
        this.clearEntries();
        this.children.addAll(param0);
    }

    protected E getEntry(int param0) {
        return this.children().get(param0);
    }

    protected int addEntry(E param0) {
        this.children.add(param0);
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E param0) {
        double var0 = (double)this.getMaxScroll() - this.getScrollAmount();
        this.children.add(0, param0);
        this.setScrollAmount((double)this.getMaxScroll() - var0);
    }

    protected boolean removeEntryFromTop(E param0) {
        double var0 = (double)this.getMaxScroll() - this.getScrollAmount();
        boolean var1 = this.removeEntry(param0);
        this.setScrollAmount((double)this.getMaxScroll() - var0);
        return var1;
    }

    protected int getItemCount() {
        return this.children().size();
    }

    protected boolean isSelectedItem(int param0) {
        return Objects.equals(this.getSelected(), this.children().get(param0));
    }

    @Nullable
    protected final E getEntryAtPosition(double param0, double param1) {
        int var0 = this.getRowWidth() / 2;
        int var1 = this.x0 + this.width / 2;
        int var2 = var1 - var0;
        int var3 = var1 + var0;
        int var4 = Mth.floor(param1 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
        int var5 = var4 / this.itemHeight;
        return param0 < (double)this.getScrollbarPosition()
                && param0 >= (double)var2
                && param0 <= (double)var3
                && var5 >= 0
                && var4 >= 0
                && var5 < this.getItemCount()
            ? this.children().get(var5)
            : null;
    }

    public void updateSize(int param0, int param1, int param2, int param3) {
        this.width = param0;
        this.height = param1;
        this.y0 = param2;
        this.y1 = param3;
        this.x0 = 0;
        this.x1 = param0;
    }

    public void setLeftPos(int param0) {
        this.x0 = param0;
        this.x1 = param0 + this.width;
    }

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected void clickedHeader(int param0, int param1) {
    }

    protected void renderHeader(GuiGraphics param0, int param1, int param2) {
    }

    protected void renderDecorations(GuiGraphics param0, int param1, int param2) {
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.hovered = this.isMouseOver((double)param1, (double)param2) ? this.getEntryAtPosition((double)param1, (double)param2) : null;
        if (this.renderBackground) {
            param0.setColor(0.125F, 0.125F, 0.125F, 1.0F);
            int var0 = 32;
            param0.blit(
                Screen.BACKGROUND_LOCATION,
                this.x0,
                this.y0,
                (float)this.x1,
                (float)(this.y1 + (int)this.getScrollAmount()),
                this.x1 - this.x0,
                this.y1 - this.y0,
                32,
                32
            );
            param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            int var1 = 4;
            param0.fillGradient(RenderType.guiOverlay(), this.x0, this.y0, this.x1, this.y0 + 4, -16777216, 0, 0);
            param0.fillGradient(RenderType.guiOverlay(), this.x0, this.y1 - 4, this.x1, this.y1, 0, -16777216, 0);
        }

        int var2 = this.getRowLeft();
        int var3 = this.y0 + 4 - (int)this.getScrollAmount();
        this.enableScissor(param0);
        if (this.renderHeader) {
            this.renderHeader(param0, var2, var3);
        }

        this.renderList(param0, param1, param2, param3);
        param0.disableScissor();
        int var4 = this.getMaxScroll();
        if (var4 > 0) {
            int var5 = this.getScrollbarPosition();
            int var6 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            var6 = Mth.clamp(var6, 32, this.y1 - this.y0 - 8);
            int var7 = (int)this.getScrollAmount() * (this.y1 - this.y0 - var6) / var4 + this.y0;
            if (var7 < this.y0) {
                var7 = this.y0;
            }

            param0.fill(var5, this.y0, var5 + 6, this.y1, -16777216);
            param0.blitSprite(SCROLLER_SPRITE, var5, var7, 6, var6);
        }

        this.renderDecorations(param0, param1, param2);
        RenderSystem.disableBlend();
    }

    protected void enableScissor(GuiGraphics param0) {
        param0.enableScissor(this.x0, this.y0, this.x1, this.y1);
    }

    protected void centerScrollOn(E param0) {
        this.setScrollAmount((double)(this.children().indexOf(param0) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2));
    }

    protected void ensureVisible(E param0) {
        int var0 = this.getRowTop(this.children().indexOf(param0));
        int var1 = var0 - this.y0 - 4 - this.itemHeight;
        if (var1 < 0) {
            this.scroll(var1);
        }

        int var2 = this.y1 - var0 - this.itemHeight - this.itemHeight;
        if (var2 < 0) {
            this.scroll(-var2);
        }

    }

    private void scroll(int param0) {
        this.setScrollAmount(this.getScrollAmount() + (double)param0);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double param0) {
        this.scrollAmount = Mth.clamp(param0, 0.0, (double)this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public int getScrollBottom() {
        return (int)this.getScrollAmount() - this.height - this.headerHeight;
    }

    protected void updateScrollingState(double param0, double param1, int param2) {
        this.scrolling = param2 == 0 && param0 >= (double)this.getScrollbarPosition() && param0 < (double)(this.getScrollbarPosition() + 6);
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        this.updateScrollingState(param0, param1, param2);
        if (!this.isMouseOver(param0, param1)) {
            return false;
        } else {
            E var0 = this.getEntryAtPosition(param0, param1);
            if (var0 != null) {
                if (var0.mouseClicked(param0, param1, param2)) {
                    E var1 = this.getFocused();
                    if (var1 != var0 && var1 instanceof ContainerEventHandler var2) {
                        var2.setFocused(null);
                    }

                    this.setFocused(var0);
                    this.setDragging(true);
                    return true;
                }
            } else if (param2 == 0) {
                this.clickedHeader(
                    (int)(param0 - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)),
                    (int)(param1 - (double)this.y0) + (int)this.getScrollAmount() - 4
                );
                return true;
            }

            return this.scrolling;
        }
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(param0, param1, param2);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (super.mouseDragged(param0, param1, param2, param3, param4)) {
            return true;
        } else if (param2 == 0 && this.scrolling) {
            if (param1 < (double)this.y0) {
                this.setScrollAmount(0.0);
            } else if (param1 > (double)this.y1) {
                this.setScrollAmount((double)this.getMaxScroll());
            } else {
                double var0 = (double)Math.max(1, this.getMaxScroll());
                int var1 = this.y1 - this.y0;
                int var2 = Mth.clamp((int)((float)(var1 * var1) / (float)this.getMaxPosition()), 32, var1 - 8);
                double var3 = Math.max(1.0, var0 / (double)(var1 - var2));
                this.setScrollAmount(this.getScrollAmount() + param4 * var3);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2, double param3) {
        this.setScrollAmount(this.getScrollAmount() - param3 * (double)this.itemHeight / 2.0);
        return true;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener param0) {
        super.setFocused(param0);
        int var0 = this.children.indexOf(param0);
        if (var0 >= 0) {
            E var1 = this.children.get(var0);
            this.setSelected(var1);
            if (this.minecraft.getLastInputType().isKeyboard()) {
                this.ensureVisible(var1);
            }
        }

    }

    @Nullable
    protected E nextEntry(ScreenDirection param0) {
        return this.nextEntry(param0, param0x -> true);
    }

    @Nullable
    protected E nextEntry(ScreenDirection param0, Predicate<E> param1) {
        return this.nextEntry(param0, param1, this.getSelected());
    }

    @Nullable
    protected E nextEntry(ScreenDirection param0, Predicate<E> param1, @Nullable E param2) {
        int var0 = switch(param0) {
            case RIGHT, LEFT -> 0;
            case UP -> -1;
            case DOWN -> 1;
        };
        if (!this.children().isEmpty() && var0 != 0) {
            int var1;
            if (param2 == null) {
                var1 = var0 > 0 ? 0 : this.children().size() - 1;
            } else {
                var1 = this.children().indexOf(param2) + var0;
            }

            for(int var3 = var1; var3 >= 0 && var3 < this.children.size(); var3 += var0) {
                E var4 = this.children().get(var3);
                if (param1.test(var4)) {
                    return var4;
                }
            }
        }

        return null;
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return param1 >= (double)this.y0 && param1 <= (double)this.y1 && param0 >= (double)this.x0 && param0 <= (double)this.x1;
    }

    protected void renderList(GuiGraphics param0, int param1, int param2, float param3) {
        int var0 = this.getRowLeft();
        int var1 = this.getRowWidth();
        int var2 = this.itemHeight - 4;
        int var3 = this.getItemCount();

        for(int var4 = 0; var4 < var3; ++var4) {
            int var5 = this.getRowTop(var4);
            int var6 = this.getRowBottom(var4);
            if (var6 >= this.y0 && var5 <= this.y1) {
                this.renderItem(param0, param1, param2, param3, var4, var0, var5, var1, var2);
            }
        }

    }

    protected void renderItem(GuiGraphics param0, int param1, int param2, float param3, int param4, int param5, int param6, int param7, int param8) {
        E var0 = this.getEntry(param4);
        var0.renderBack(param0, param4, param6, param5, param7, param8, param1, param2, Objects.equals(this.hovered, var0), param3);
        if (this.isSelectedItem(param4)) {
            int var1 = this.isFocused() ? -1 : -8355712;
            this.renderSelection(param0, param6, param7, param8, var1, -16777216);
        }

        var0.render(param0, param4, param6, param5, param7, param8, param1, param2, Objects.equals(this.hovered, var0), param3);
    }

    protected void renderSelection(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5) {
        int var0 = this.x0 + (this.width - param2) / 2;
        int var1 = this.x0 + (this.width + param2) / 2;
        param0.fill(var0, param1 - 2, var1, param1 + param3 + 2, param4);
        param0.fill(var0 + 1, param1 - 1, var1 - 1, param1 + param3 + 1, param5);
    }

    public int getRowLeft() {
        return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    protected int getRowTop(int param0) {
        return this.y0 + 4 - (int)this.getScrollAmount() + param0 * this.itemHeight + this.headerHeight;
    }

    protected int getRowBottom(int param0) {
        return this.getRowTop(param0) + this.itemHeight;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        } else {
            return this.hovered != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

    @Nullable
    protected E remove(int param0) {
        E var0 = this.children.get(param0);
        return this.removeEntry(this.children.get(param0)) ? var0 : null;
    }

    protected boolean removeEntry(E param0) {
        boolean var0 = this.children.remove(param0);
        if (var0 && param0 == this.getSelected()) {
            this.setSelected((E)null);
        }

        return var0;
    }

    @Nullable
    protected E getHovered() {
        return this.hovered;
    }

    void bindEntryToSelf(AbstractSelectionList.Entry<E> param0) {
        param0.list = this;
    }

    protected void narrateListElementPosition(NarrationElementOutput param0, E param1) {
        List<E> var0 = this.children();
        if (var0.size() > 1) {
            int var1 = var0.indexOf(param1);
            if (var1 != -1) {
                param0.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.list", var1 + 1, var0.size()));
            }
        }

    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0);
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract static class Entry<E extends AbstractSelectionList.Entry<E>> implements GuiEventListener {
        @Deprecated
        AbstractSelectionList<E> list;

        @Override
        public void setFocused(boolean param0) {
        }

        @Override
        public boolean isFocused() {
            return this.list.getFocused() == this;
        }

        public abstract void render(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10);

        public void renderBack(
            GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
        ) {
        }

        @Override
        public boolean isMouseOver(double param0, double param1) {
            return Objects.equals(this.list.getEntryAtPosition(param0, param1), this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class TrackedList extends AbstractList<E> {
        private final List<E> delegate = Lists.newArrayList();

        public E get(int param0) {
            return this.delegate.get(param0);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        public E set(int param0, E param1) {
            E var0 = this.delegate.set(param0, param1);
            AbstractSelectionList.this.bindEntryToSelf(param1);
            return var0;
        }

        public void add(int param0, E param1) {
            this.delegate.add(param0, param1);
            AbstractSelectionList.this.bindEntryToSelf(param1);
        }

        public E remove(int param0) {
            return this.delegate.remove(param0);
        }
    }
}
