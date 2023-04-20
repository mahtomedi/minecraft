package net.minecraft.client.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
    private static final Component WARNING_LABEL = Component.literal("(")
        .append(Component.translatable("options.languageWarning"))
        .append(")")
        .withStyle(ChatFormatting.GRAY);
    private LanguageSelectScreen.LanguageSelectionList packSelectionList;
    final LanguageManager languageManager;

    public LanguageSelectScreen(Screen param0, Options param1, LanguageManager param2) {
        super(param0, param1, Component.translatable("options.language"));
        this.languageManager = param2;
    }

    @Override
    protected void init() {
        this.packSelectionList = new LanguageSelectScreen.LanguageSelectionList(this.minecraft);
        this.addWidget(this.packSelectionList);
        this.addRenderableWidget(this.options.forceUnicodeFont().createButton(this.options, this.width / 2 - 155, this.height - 38, 150));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> {
            LanguageSelectScreen.LanguageSelectionList.Entry var0 = this.packSelectionList.getSelected();
            if (var0 != null && !var0.code.equals(this.languageManager.getSelected())) {
                this.languageManager.setSelected(var0.code);
                this.options.languageCode = var0.code;
                this.minecraft.reloadResourcePacks();
                this.options.save();
            }

            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 155 + 160, this.height - 38, 150, 20).build());
        super.init();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.packSelectionList.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
        param0.drawCenteredString(this.font, WARNING_LABEL, this.width / 2, this.height - 56, 8421504);
        super.render(param0, param1, param2, param3);
    }

    @OnlyIn(Dist.CLIENT)
    class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
        public LanguageSelectionList(Minecraft param0) {
            super(param0, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);
            String param1 = LanguageSelectScreen.this.languageManager.getSelected();
            LanguageSelectScreen.this.languageManager.getLanguages().forEach((param1x, param2) -> {
                LanguageSelectScreen.LanguageSelectionList.Entry var0 = new LanguageSelectScreen.LanguageSelectionList.Entry(param1x, param2);
                this.addEntry(var0);
                if (param1.equals(param1x)) {
                    this.setSelected(var0);
                }

            });
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }

        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Override
        protected void renderBackground(GuiGraphics param0) {
            LanguageSelectScreen.this.renderBackground(param0);
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
            final String code;
            private final Component language;

            public Entry(String param1, LanguageInfo param2) {
                this.code = param1;
                this.language = param2.toComponent();
            }

            @Override
            public void render(
                GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                param0.drawCenteredString(LanguageSelectScreen.this.font, this.language, LanguageSelectionList.this.width / 2, param2 + 1, 16777215);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }

            private void select() {
                LanguageSelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.language);
            }
        }
    }
}
