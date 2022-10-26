package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
    public OptionsList(Minecraft param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3, param4, param5);
        this.centerListVertically = false;
    }

    public int addBig(OptionInstance<?> param0) {
        return this.addEntry(OptionsList.Entry.big(this.minecraft.options, this.width, param0));
    }

    public void addSmall(OptionInstance<?> param0, @Nullable OptionInstance<?> param1) {
        this.addEntry(OptionsList.Entry.small(this.minecraft.options, this.width, param0, param1));
    }

    public void addSmall(OptionInstance<?>[] param0) {
        for(int var0 = 0; var0 < param0.length; var0 += 2) {
            this.addSmall(param0[var0], var0 < param0.length - 1 ? param0[var0 + 1] : null);
        }

    }

    @Override
    public int getRowWidth() {
        return 400;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }

    @Nullable
    public AbstractWidget findOption(OptionInstance<?> param0) {
        for(OptionsList.Entry var0 : this.children()) {
            AbstractWidget var1 = var0.options.get(param0);
            if (var1 != null) {
                return var1;
            }
        }

        return null;
    }

    public Optional<AbstractWidget> getMouseOver(double param0, double param1) {
        for(OptionsList.Entry var0 : this.children()) {
            for(AbstractWidget var1 : var0.children) {
                if (var1.isMouseOver(param0, param1)) {
                    return Optional.of(var1);
                }
            }
        }

        return Optional.empty();
    }

    @OnlyIn(Dist.CLIENT)
    protected static class Entry extends ContainerObjectSelectionList.Entry<OptionsList.Entry> {
        final Map<OptionInstance<?>, AbstractWidget> options;
        final List<AbstractWidget> children;

        private Entry(Map<OptionInstance<?>, AbstractWidget> param0) {
            this.options = param0;
            this.children = ImmutableList.copyOf(param0.values());
        }

        public static OptionsList.Entry big(Options param0, int param1, OptionInstance<?> param2) {
            return new OptionsList.Entry(ImmutableMap.of(param2, param2.createButton(param0, param1 / 2 - 155, 0, 310)));
        }

        public static OptionsList.Entry small(Options param0, int param1, OptionInstance<?> param2, @Nullable OptionInstance<?> param3) {
            AbstractWidget var0 = param2.createButton(param0, param1 / 2 - 155, 0, 150);
            return param3 == null
                ? new OptionsList.Entry(ImmutableMap.of(param2, var0))
                : new OptionsList.Entry(ImmutableMap.of(param2, var0, param3, param3.createButton(param0, param1 / 2 - 155 + 160, 0, 150)));
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.children.forEach(param5x -> {
                param5x.setY(param2);
                param5x.render(param0, param6, param7, param9);
            });
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}
