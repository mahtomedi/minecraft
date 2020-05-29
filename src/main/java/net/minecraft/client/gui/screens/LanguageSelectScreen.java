package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
    private LanguageSelectScreen.LanguageSelectionList packSelectionList;
    private final LanguageManager languageManager;
    private OptionButton forceUnicodeButton;
    private Button doneButton;

    public LanguageSelectScreen(Screen param0, Options param1, LanguageManager param2) {
        super(param0, param1, new TranslatableComponent("options.language"));
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
        this.doneButton = this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 38, 150, 20, CommonComponents.GUI_DONE, param0 -> {
            LanguageSelectScreen.LanguageSelectionList.Entry var0 = this.packSelectionList.getSelected();
            if (var0 != null && !var0.language.getCode().equals(this.languageManager.getSelected().getCode())) {
                this.languageManager.setSelected(var0.language);
                this.options.languageCode = var0.language.getCode();
                this.minecraft.reloadResourcePacks();
                this.doneButton.setMessage(CommonComponents.GUI_DONE);
                this.forceUnicodeButton.setMessage(Option.FORCE_UNICODE_FONT.getMessage(this.options));
                this.options.save();
            }

            this.minecraft.setScreen(this.lastScreen);
        }));
        super.init();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.packSelectionList.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 16, 16777215);
        this.drawCenteredString(param0, this.font, "(" + I18n.get("options.languageWarning") + ")", this.width / 2, this.height - 56, 8421504);
        super.render(param0, param1, param2, param3);
    }

    @OnlyIn(Dist.CLIENT)
    class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
        public LanguageSelectionList(Minecraft param0) {
            super(param0, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);

            for(LanguageInfo param1 : LanguageSelectScreen.this.languageManager.getLanguages()) {
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
        protected void renderBackground(PoseStack param0) {
            LanguageSelectScreen.this.renderBackground(param0);
        }

        @Override
        protected boolean isFocused() {
            return LanguageSelectScreen.this.getFocused() == this;
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
            private final LanguageInfo language;

            public Entry(LanguageInfo param1) {
                this.language = param1;
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                String var0 = this.language.toString();
                LanguageSelectScreen.this.font
                    .drawShadow(
                        param0,
                        var0,
                        (float)(LanguageSelectionList.this.width / 2 - LanguageSelectScreen.this.font.width(var0) / 2),
                        (float)(param2 + 1),
                        16777215,
                        true
                    );
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
