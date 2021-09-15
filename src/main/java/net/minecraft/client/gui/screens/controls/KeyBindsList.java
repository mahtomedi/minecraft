package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
    final KeyBindsScreen keyBindsScreen;
    int maxNameWidth;

    public KeyBindsList(KeyBindsScreen param0, Minecraft param1) {
        super(param1, param0.width + 45, param0.height, 20, param0.height - 32, 20);
        this.keyBindsScreen = param0;
        KeyMapping[] var0 = ArrayUtils.clone(param1.options.keyMappings);
        Arrays.sort((Object[])var0);
        String var1 = null;

        for(KeyMapping var2 : var0) {
            String var3 = var2.getCategory();
            if (!var3.equals(var1)) {
                var1 = var3;
                this.addEntry(new KeyBindsList.CategoryEntry(new TranslatableComponent(var3)));
            }

            Component var4 = new TranslatableComponent(var2.getName());
            int var5 = param1.font.width(var4);
            if (var5 > this.maxNameWidth) {
                this.maxNameWidth = var5;
            }

            this.addEntry(new KeyBindsList.KeyEntry(var2, var4));
        }

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
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            KeyBindsList.this.minecraft
                .font
                .draw(param0, this.name, (float)(KeyBindsList.this.minecraft.screen.width / 2 - this.width / 2), (float)(param2 + param5 - 9 - 1), 16777215);
        }

        @Override
        public boolean changeFocus(boolean param0) {
            return false;
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
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindsList.Entry> {
    }

    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends KeyBindsList.Entry {
        private final KeyMapping key;
        private final Component name;
        private final Button changeButton;
        private final Button resetButton;

        KeyEntry(final KeyMapping param1, final Component param2) {
            this.key = param1;
            this.name = param2;
            this.changeButton = new Button(0, 0, 75, 20, param2, param1x -> KeyBindsList.this.keyBindsScreen.selectedKey = param1) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return param1.isUnbound()
                        ? new TranslatableComponent("narrator.controls.unbound", param2)
                        : new TranslatableComponent("narrator.controls.bound", param2, super.createNarrationMessage());
                }
            };
            this.resetButton = new Button(0, 0, 50, 20, new TranslatableComponent("controls.reset"), param1x -> {
                KeyBindsList.this.minecraft.options.setKey(param1, param1.getDefaultKey());
                KeyMapping.resetMapping();
            }) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return new TranslatableComponent("narrator.controls.reset", param2);
                }
            };
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            boolean var0 = KeyBindsList.this.keyBindsScreen.selectedKey == this.key;
            float var10003 = (float)(param3 + 90 - KeyBindsList.this.maxNameWidth);
            KeyBindsList.this.minecraft.font.draw(param0, this.name, var10003, (float)(param2 + param5 / 2 - 9 / 2), 16777215);
            this.resetButton.x = param3 + 190;
            this.resetButton.y = param2;
            this.resetButton.active = !this.key.isDefault();
            this.resetButton.render(param0, param6, param7, param9);
            this.changeButton.x = param3 + 105;
            this.changeButton.y = param2;
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            boolean var1 = false;
            if (!this.key.isUnbound()) {
                for(KeyMapping var2 : KeyBindsList.this.minecraft.options.keyMappings) {
                    if (var2 != this.key && this.key.same(var2)) {
                        var1 = true;
                        break;
                    }
                }
            }

            if (var0) {
                this.changeButton
                    .setMessage(
                        new TextComponent("> ")
                            .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.YELLOW))
                            .append(" <")
                            .withStyle(ChatFormatting.YELLOW)
                    );
            } else if (var1) {
                this.changeButton.setMessage(this.changeButton.getMessage().copy().withStyle(ChatFormatting.RED));
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
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.changeButton.mouseClicked(param0, param1, param2)) {
                return true;
            } else {
                return this.resetButton.mouseClicked(param0, param1, param2);
            }
        }

        @Override
        public boolean mouseReleased(double param0, double param1, int param2) {
            return this.changeButton.mouseReleased(param0, param1, param2) || this.resetButton.mouseReleased(param0, param1, param2);
        }
    }
}
