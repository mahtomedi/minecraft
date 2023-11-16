package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ContainerObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    public ContainerObjectSelectionList(Minecraft param0, int param1, int param2, int param3, int param4) {
        super(param0, param1, param2, param3, param4);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        if (this.getItemCount() == 0) {
            return null;
        } else if (!(param0 instanceof FocusNavigationEvent.ArrowNavigation)) {
            return super.nextFocusPath(param0);
        } else {
            FocusNavigationEvent.ArrowNavigation var0 = (FocusNavigationEvent.ArrowNavigation)param0;
            E var1 = this.getFocused();
            if (var0.direction().getAxis() == ScreenAxis.HORIZONTAL && var1 != null) {
                return ComponentPath.path(this, var1.nextFocusPath(param0));
            } else {
                int var2 = -1;
                ScreenDirection var3 = var0.direction();
                if (var1 != null) {
                    var2 = var1.children().indexOf(var1.getFocused());
                }

                if (var2 == -1) {
                    switch(var3) {
                        case LEFT:
                            var2 = Integer.MAX_VALUE;
                            var3 = ScreenDirection.DOWN;
                            break;
                        case RIGHT:
                            var2 = 0;
                            var3 = ScreenDirection.DOWN;
                            break;
                        default:
                            var2 = 0;
                    }
                }

                E var4 = var1;

                ComponentPath var5;
                do {
                    var4 = this.nextEntry(var3, param0x -> !param0x.children().isEmpty(), var4);
                    if (var4 == null) {
                        return null;
                    }

                    var5 = var4.focusPathAtIndex(var0, var2);
                } while(var5 == null);

                return ComponentPath.path(this, var5);
            }
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener param0) {
        super.setFocused(param0);
        if (param0 == null) {
            this.setSelected((E)null);
        }

    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.isFocused() ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
    }

    @Override
    protected boolean isSelectedItem(int param0) {
        return false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
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

        param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
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
        public boolean mouseClicked(double param0, double param1, int param2) {
            return ContainerEventHandler.super.mouseClicked(param0, param1, param2);
        }

        @Override
        public void setFocused(@Nullable GuiEventListener param0) {
            if (this.focused != null) {
                this.focused.setFocused(false);
            }

            if (param0 != null) {
                param0.setFocused(true);
            }

            this.focused = param0;
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Nullable
        public ComponentPath focusPathAtIndex(FocusNavigationEvent param0, int param1) {
            if (this.children().isEmpty()) {
                return null;
            } else {
                ComponentPath var0 = this.children().get(Math.min(param1, this.children().size() - 1)).nextFocusPath(param0);
                return ComponentPath.path(this, var0);
            }
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
            if (param0 instanceof FocusNavigationEvent.ArrowNavigation var0) {
                int var1 = switch(var0.direction()) {
                    case LEFT -> -1;
                    case RIGHT -> 1;
                    case UP, DOWN -> 0;
                };
                if (var1 == 0) {
                    return null;
                }

                int var2 = Mth.clamp(var1 + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1);

                for(int var3 = var2; var3 >= 0 && var3 < this.children().size(); var3 += var1) {
                    GuiEventListener var4 = this.children().get(var3);
                    ComponentPath var5 = var4.nextFocusPath(param0);
                    if (var5 != null) {
                        return ComponentPath.path(this, var5);
                    }
                }
            }

            return ContainerEventHandler.super.nextFocusPath(param0);
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
                    param0.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.object_list", var1.index + 1, var0.size()));
                    if (var1.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                        param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
                    }
                }

                var1.entry.updateNarration(param0.nest());
            }

        }
    }
}
