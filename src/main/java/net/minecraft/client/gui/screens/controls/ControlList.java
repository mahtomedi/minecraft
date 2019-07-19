package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public class ControlList extends ContainerObjectSelectionList<ControlList.Entry> {
    private final ControlsScreen controlsScreen;
    private int maxNameWidth;

    public ControlList(ControlsScreen param0, Minecraft param1) {
        super(param1, param0.width + 45, param0.height, 43, param0.height - 32, 20);
        this.controlsScreen = param0;
        KeyMapping[] var0 = ArrayUtils.clone(param1.options.keyMappings);
        Arrays.sort((Object[])var0);
        String var1 = null;

        for(KeyMapping var2 : var0) {
            String var3 = var2.getCategory();
            if (!var3.equals(var1)) {
                var1 = var3;
                this.addEntry(new ControlList.CategoryEntry(var3));
            }

            int var4 = param1.font.width(I18n.get(var2.getName()));
            if (var4 > this.maxNameWidth) {
                this.maxNameWidth = var4;
            }

            this.addEntry(new ControlList.KeyEntry(var2));
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
    public class CategoryEntry extends ControlList.Entry {
        private final String name;
        private final int width;

        public CategoryEntry(String param1) {
            this.name = I18n.get(param1);
            this.width = ControlList.this.minecraft.font.width(this.name);
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            ControlList.this.minecraft
                .font
                .draw(this.name, (float)(ControlList.this.minecraft.screen.width / 2 - this.width / 2), (float)(param1 + param4 - 9 - 1), 16777215);
        }

        @Override
        public boolean changeFocus(boolean param0) {
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<ControlList.Entry> {
    }

    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends ControlList.Entry {
        private final KeyMapping key;
        private final String name;
        private final Button changeButton;
        private final Button resetButton;

        private KeyEntry(final KeyMapping param1) {
            this.key = param1;
            this.name = I18n.get(param1.getName());
            this.changeButton = new Button(0, 0, 75, 20, this.name, param1x -> ControlList.this.controlsScreen.selectedKey = param1) {
                @Override
                protected String getNarrationMessage() {
                    return param1.isUnbound()
                        ? I18n.get("narrator.controls.unbound", KeyEntry.this.name)
                        : I18n.get("narrator.controls.bound", KeyEntry.this.name, super.getNarrationMessage());
                }
            };
            this.resetButton = new Button(0, 0, 50, 20, I18n.get("controls.reset"), param1x -> {
                ControlList.this.minecraft.options.setKey(param1, param1.getDefaultKey());
                KeyMapping.resetMapping();
            }) {
                @Override
                protected String getNarrationMessage() {
                    return I18n.get("narrator.controls.reset", KeyEntry.this.name);
                }
            };
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            boolean var0 = ControlList.this.controlsScreen.selectedKey == this.key;
            ControlList.this.minecraft
                .font
                .draw(this.name, (float)(param2 + 90 - ControlList.this.maxNameWidth), (float)(param1 + param4 / 2 - 9 / 2), 16777215);
            this.resetButton.x = param2 + 190;
            this.resetButton.y = param1;
            this.resetButton.active = !this.key.isDefault();
            this.resetButton.render(param5, param6, param8);
            this.changeButton.x = param2 + 105;
            this.changeButton.y = param1;
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            boolean var1 = false;
            if (!this.key.isUnbound()) {
                for(KeyMapping var2 : ControlList.this.minecraft.options.keyMappings) {
                    if (var2 != this.key && this.key.same(var2)) {
                        var1 = true;
                        break;
                    }
                }
            }

            if (var0) {
                this.changeButton
                    .setMessage(ChatFormatting.WHITE + "> " + ChatFormatting.YELLOW + this.changeButton.getMessage() + ChatFormatting.WHITE + " <");
            } else if (var1) {
                this.changeButton.setMessage(ChatFormatting.RED + this.changeButton.getMessage());
            }

            this.changeButton.render(param5, param6, param8);
        }

        @Override
        public List<? extends GuiEventListener> children() {
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
