package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.Language;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageSelectScreen extends Screen {
    protected final Screen lastScreen;
    private LanguageSelectScreen.LanguageSelectionList packSelectionList;
    private final Options options;
    private final LanguageManager languageManager;
    private OptionButton forceUnicodeButton;
    private Button doneButton;

    public LanguageSelectScreen(Screen param0, Options param1, LanguageManager param2) {
        super(new TranslatableComponent("options.language"));
        this.lastScreen = param0;
        this.options = param1;
        this.languageManager = param2;
    }

    @Override
    protected void init() {
        this.packSelectionList = new LanguageSelectScreen.LanguageSelectionList(this.minecraft);
        this.children.add(this.packSelectionList);
        this.forceUnicodeButton = this.addButton(
            new OptionButton(
                this.width / 2 - 155, this.height - 38, 150, 20, Option.FORCE_UNICODE_FONT, Option.FORCE_UNICODE_FONT.getMessage(this.options), param0 -> {
                    Option.FORCE_UNICODE_FONT.toggle(this.options);
                    this.options.save();
                    param0.setMessage(Option.FORCE_UNICODE_FONT.getMessage(this.options));
                    this.minecraft.resizeDisplay();
                }
            )
        );
        this.doneButton = this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 38, 150, 20, I18n.get("gui.done"), param0 -> {
            LanguageSelectScreen.LanguageSelectionList.Entry var0 = this.packSelectionList.getSelected();
            if (var0 != null && !var0.language.getCode().equals(this.languageManager.getSelected().getCode())) {
                this.languageManager.setSelected(var0.language);
                this.options.languageCode = var0.language.getCode();
                this.minecraft.reloadResourcePacks();
                this.font.setBidirectional(this.languageManager.isBidirectional());
                this.doneButton.setMessage(I18n.get("gui.done"));
                this.forceUnicodeButton.setMessage(Option.FORCE_UNICODE_FONT.getMessage(this.options));
                this.options.save();
            }

            this.minecraft.setScreen(this.lastScreen);
        }));
        super.init();
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.packSelectionList.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.font, "(" + I18n.get("options.languageWarning") + ")", this.width / 2, this.height - 56, 8421504);
        super.render(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
        public LanguageSelectionList(Minecraft param0) {
            super(param0, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);

            for(Language param1 : LanguageSelectScreen.this.languageManager.getLanguages()) {
                LanguageSelectScreen.LanguageSelectionList.Entry var0 = new LanguageSelectScreen.LanguageSelectionList.Entry(param1);
                this.addEntry(var0);
                if (LanguageSelectScreen.this.languageManager.getSelected().getCode().equals(param1.getCode())) {
                    this.setSelected(var0);
                }
            }

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

        public void setSelected(@Nullable LanguageSelectScreen.LanguageSelectionList.Entry param0) {
            super.setSelected(param0);
            if (param0 != null) {
                NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", param0.language).getString());
            }

        }

        @Override
        protected void renderBackground() {
            LanguageSelectScreen.this.renderBackground();
        }

        @Override
        protected boolean isFocused() {
            return LanguageSelectScreen.this.getFocused() == this;
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
            private final Language language;

            public Entry(Language param1) {
                this.language = param1;
            }

            @Override
            public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
                LanguageSelectScreen.this.font.setBidirectional(true);
                LanguageSelectionList.this.drawCenteredString(
                    LanguageSelectScreen.this.font, this.language.toString(), LanguageSelectionList.this.width / 2, param1 + 1, 16777215
                );
                LanguageSelectScreen.this.font.setBidirectional(LanguageSelectScreen.this.languageManager.getSelected().isBidirectional());
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
        }
    }
}
