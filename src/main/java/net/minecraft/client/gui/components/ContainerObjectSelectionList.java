package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ContainerObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    private boolean hasFocus;

    public ContainerObjectSelectionList(Minecraft param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean changeFocus(boolean param0) {
        this.hasFocus = super.changeFocus(param0);
        if (this.hasFocus) {
            this.ensureVisible(this.getFocused());
        }

        return this.hasFocus;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.hasFocus ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
    }

    @Override
    protected boolean isSelectedItem(int param0) {
        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        E var0 = this.getHovered();
        if (var0 != null) {
            var0.updateNarration(param0.nest());
            this.narrateListElementPosition(param0, var0);
        } else {
            E var1 = this.getFocused();
            if (var1 != null) {
                var1.updateNarration(param0.nest());
                this.narrateListElementPosition(param0, var1);
            }
        }

        param0.add(NarratedElementType.USAGE, (Component)(new TranslatableComponent("narration.component_list.usage")));
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements ContainerEventHandler {
        @Nullable
        private GuiEventListener focused;
        @Nullable
        private NarratableEntry lastNarratable;
        private boolean dragging;

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean param0) {
            this.dragging = param0;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener param0) {
            this.focused = param0;
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return this.focused;
        }

        public abstract List<? extends NarratableEntry> narratables();

        void updateNarration(NarrationElementOutput param0) {
            List<? extends NarratableEntry> var0 = this.narratables();
            Screen.NarratableSearchResult var1 = Screen.findNarratableWidget(var0, this.lastNarratable);
            if (var1 != null) {
                if (var1.priority.isTerminal()) {
                    this.lastNarratable = var1.entry;
                }

                if (var0.size() > 1) {
                    param0.add(
                        NarratedElementType.POSITION, (Component)(new TranslatableComponent("narrator.position.object_list", var1.index + 1, var0.size()))
                    );
                    if (var1.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                        param0.add(NarratedElementType.USAGE, (Component)(new TranslatableComponent("narration.component_list.usage")));
                    }
                }

                var1.entry.updateNarration(param0.nest());
            }

        }
    }
}
