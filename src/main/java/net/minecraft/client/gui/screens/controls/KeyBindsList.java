package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
    final KeyBindsScreen keyBindsScreen;
    int maxNameWidth;

    public KeyBindsList(KeyBindsScreen param0, Minecraft param1) {
        super(param1, param0.width + 45, param0.height - 52, 20, 20);
        this.keyBindsScreen = param0;
        KeyMapping[] var0 = ArrayUtils.clone((KeyMapping[])param1.options.keyMappings);
        Arrays.sort((Object[])var0);
        String var1 = null;

        for(KeyMapping var2 : var0) {
            String var3 = var2.getCategory();
            if (!var3.equals(var1)) {
                var1 = var3;
                this.addEntry(new KeyBindsList.CategoryEntry(Component.translatable(var3)));
            }

            Component var4 = Component.translatable(var2.getName());
            int var5 = param1.font.width(var4);
            if (var5 > this.maxNameWidth) {
                this.maxNameWidth = var5;
            }

            this.addEntry(new KeyBindsList.KeyEntry(var2, var4));
        }

    }

    public void resetMappingAndUpdateButtons() {
        KeyMapping.resetMapping();
        this.refreshEntries();
    }

    public void refreshEntries() {
        this.children().forEach(KeyBindsList.Entry::refreshEntry);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryEntry extends KeyBindsList.Entry {
        final Component name;
        private final int width;

        public CategoryEntry(Component param1) {
            this.name = param1;
            this.width = KeyBindsList.this.minecraft.font.width(this.name);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            param0.drawString(
                KeyBindsList.this.minecraft.font,
                this.name,
                KeyBindsList.this.minecraft.screen.width / 2 - this.width / 2,
                param2 + param5 - 9 - 1,
                16777215,
                false
            );
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput param0) {
                    param0.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }

        @Override
        protected void refreshEntry() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindsList.Entry> {
        abstract void refreshEntry();
    }

    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends KeyBindsList.Entry {
        private final KeyMapping key;
        private final Component name;
        private final Button changeButton;
        private final Button resetButton;
        private boolean hasCollision = false;

        KeyEntry(KeyMapping param1, Component param2) {
            this.key = param1;
            this.name = param2;
            this.changeButton = Button.builder(param2, param1x -> {
                    KeyBindsList.this.keyBindsScreen.selectedKey = param1;
                    KeyBindsList.this.resetMappingAndUpdateButtons();
                })
                .bounds(0, 0, 75, 20)
                .createNarration(
                    param2x -> param1.isUnbound()
                            ? Component.translatable("narrator.controls.unbound", param2)
                            : Component.translatable("narrator.controls.bound", param2, param2x.get())
                )
                .build();
            this.resetButton = Button.builder(Component.translatable("controls.reset"), param1x -> {
                KeyBindsList.this.minecraft.options.setKey(param1, param1.getDefaultKey());
                KeyBindsList.this.resetMappingAndUpdateButtons();
            }).bounds(0, 0, 50, 20).createNarration(param1x -> Component.translatable("narrator.controls.reset", param2)).build();
            this.refreshEntry();
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            int var10003 = param3 + 90 - KeyBindsList.this.maxNameWidth;
            param0.drawString(KeyBindsList.this.minecraft.font, this.name, var10003, param2 + param5 / 2 - 9 / 2, 16777215, false);
            this.resetButton.setX(param3 + 190);
            this.resetButton.setY(param2);
            this.resetButton.render(param0, param6, param7, param9);
            this.changeButton.setX(param3 + 105);
            this.changeButton.setY(param2);
            if (this.hasCollision) {
                int var0 = 3;
                int var1 = this.changeButton.getX() - 6;
                param0.fill(var1, param2 + 2, var1 + 3, param2 + param5 + 2, ChatFormatting.RED.getColor() | 0xFF000000);
            }

            this.changeButton.render(param0, param6, param7, param9);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        protected void refreshEntry() {
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            this.resetButton.active = !this.key.isDefault();
            this.hasCollision = false;
            MutableComponent var0 = Component.empty();
            if (!this.key.isUnbound()) {
                for(KeyMapping var1 : KeyBindsList.this.minecraft.options.keyMappings) {
                    if (var1 != this.key && this.key.same(var1)) {
                        if (this.hasCollision) {
                            var0.append(", ");
                        }

                        this.hasCollision = true;
                        var0.append(Component.translatable(var1.getName()));
                    }
                }
            }

            if (this.hasCollision) {
                this.changeButton
                    .setMessage(
                        Component.literal("[ ")
                            .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE))
                            .append(" ]")
                            .withStyle(ChatFormatting.RED)
                    );
                this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", var0)));
            } else {
                this.changeButton.setTooltip(null);
            }

            if (KeyBindsList.this.keyBindsScreen.selectedKey == this.key) {
                this.changeButton
                    .setMessage(
                        Component.literal("> ")
                            .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                            .append(" <")
                            .withStyle(ChatFormatting.YELLOW)
                    );
            }

        }
    }
}
