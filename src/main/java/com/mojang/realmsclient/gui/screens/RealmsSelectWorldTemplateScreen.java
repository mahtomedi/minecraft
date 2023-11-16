package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation SLOT_FRAME_SPRITE = new ResourceLocation("widget/slot_frame");
    private static final Component SELECT_BUTTON_NAME = Component.translatable("mco.template.button.select");
    private static final Component TRAILER_BUTTON_NAME = Component.translatable("mco.template.button.trailer");
    private static final Component PUBLISHER_BUTTON_NAME = Component.translatable("mco.template.button.publisher");
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_SPACING = 10;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    final Consumer<WorldTemplate> callback;
    RealmsSelectWorldTemplateScreen.WorldTemplateList worldTemplateList;
    private final RealmsServer.WorldType worldType;
    private Button selectButton;
    private Button trailerButton;
    private Button publisherButton;
    @Nullable
    WorldTemplate selectedTemplate = null;
    @Nullable
    String currentLink;
    @Nullable
    private Component[] warning;
    @Nullable
    List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(Component param0, Consumer<WorldTemplate> param1, RealmsServer.WorldType param2) {
        this(param0, param1, param2, null);
    }

    public RealmsSelectWorldTemplateScreen(
        Component param0, Consumer<WorldTemplate> param1, RealmsServer.WorldType param2, @Nullable WorldTemplatePaginatedList param3
    ) {
        super(param0);
        this.callback = param1;
        this.worldType = param2;
        if (param3 == null) {
            this.worldTemplateList = new RealmsSelectWorldTemplateScreen.WorldTemplateList();
            this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
        } else {
            this.worldTemplateList = new RealmsSelectWorldTemplateScreen.WorldTemplateList(Lists.newArrayList(param3.templates));
            this.fetchTemplatesAsync(param3);
        }

    }

    public void setWarning(Component... param0) {
        this.warning = param0;
    }

    @Override
    public void init() {
        this.layout.addToHeader(new StringWidget(this.title, this.font));
        this.worldTemplateList = this.layout.addToContents(new RealmsSelectWorldTemplateScreen.WorldTemplateList(this.worldTemplateList.getTemplates()));
        LinearLayout var0 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
        var0.defaultCellSetting().alignHorizontallyCenter();
        this.trailerButton = var0.addChild(Button.builder(TRAILER_BUTTON_NAME, param0 -> this.onTrailer()).width(100).build());
        this.selectButton = var0.addChild(Button.builder(SELECT_BUTTON_NAME, param0 -> this.selectTemplate()).width(100).build());
        var0.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onClose()).width(100).build());
        this.publisherButton = var0.addChild(Button.builder(PUBLISHER_BUTTON_NAME, param0 -> this.onPublish()).width(100).build());
        this.updateButtonStates();
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.worldTemplateList.setSize(this.width, this.height - this.layout.getFooterHeight() - this.getHeaderHeight());
        this.layout.arrangeElements();
    }

    @Override
    public Component getNarrationMessage() {
        List<Component> var0 = Lists.newArrayListWithCapacity(2);
        var0.add(this.title);
        if (this.warning != null) {
            var0.addAll(Arrays.asList(this.warning));
        }

        return CommonComponents.joinLines(var0);
    }

    void updateButtonStates() {
        this.publisherButton.visible = this.selectedTemplate != null && !this.selectedTemplate.link.isEmpty();
        this.trailerButton.visible = this.selectedTemplate != null && !this.selectedTemplate.trailer.isEmpty();
        this.selectButton.active = this.selectedTemplate != null;
    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }

    private void selectTemplate() {
        if (this.selectedTemplate != null) {
            this.callback.accept(this.selectedTemplate);
        }

    }

    private void onTrailer() {
        if (this.selectedTemplate != null && !this.selectedTemplate.trailer.isBlank()) {
            ConfirmLinkScreen.confirmLinkNow(this, this.selectedTemplate.trailer);
        }

    }

    private void onPublish() {
        if (this.selectedTemplate != null && !this.selectedTemplate.link.isBlank()) {
            ConfirmLinkScreen.confirmLinkNow(this, this.selectedTemplate.link);
        }

    }

    private void fetchTemplatesAsync(final WorldTemplatePaginatedList param0) {
        (new Thread("realms-template-fetcher") {
                @Override
                public void run() {
                    WorldTemplatePaginatedList var0 = param0;
    
                    Either<WorldTemplatePaginatedList, Exception> var2;
                    for(RealmsClient var1 = RealmsClient.create();
                        var0 != null;
                        var0 = RealmsSelectWorldTemplateScreen.this.minecraft
                            .submit(
                                () -> {
                                    if (var2.right().isPresent()) {
                                        RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates", var2.right().get());
                                        if (RealmsSelectWorldTemplateScreen.this.worldTemplateList.isEmpty()) {
                                            RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(
                                                I18n.get("mco.template.select.failure")
                                            );
                                        }
                
                                        return null;
                                    } else {
                                        WorldTemplatePaginatedList var0x = var2.left().get();
                
                                        for(WorldTemplate var3x : var0x.templates) {
                                            RealmsSelectWorldTemplateScreen.this.worldTemplateList.addEntry(var3x);
                                        }
                
                                        if (var0x.templates.isEmpty()) {
                                            if (RealmsSelectWorldTemplateScreen.this.worldTemplateList.isEmpty()) {
                                                String var2x = I18n.get("mco.template.select.none", "%link");
                                                TextRenderingUtils.LineSegment var3 = TextRenderingUtils.LineSegment.link(
                                                    I18n.get("mco.template.select.none.linkTitle"), "https://aka.ms/MinecraftRealmsContentCreator"
                                                );
                                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(var2x, var3);
                                            }
                
                                            return null;
                                        } else {
                                            return var0x;
                                        }
                                    }
                                }
                            )
                            .join()
                    ) {
                        var2 = RealmsSelectWorldTemplateScreen.this.fetchTemplates(var0, var1);
                    }
    
                }
            })
            .start();
    }

    Either<WorldTemplatePaginatedList, Exception> fetchTemplates(WorldTemplatePaginatedList param0, RealmsClient param1) {
        try {
            return Either.left(param1.fetchWorldTemplates(param0.page + 1, param0.size, this.worldType));
        } catch (RealmsServiceException var4) {
            return Either.right(var4);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.currentLink = null;
        if (this.noTemplatesMessage != null) {
            this.renderMultilineMessage(param0, param1, param2, this.noTemplatesMessage);
        }

        if (this.warning != null) {
            for(int var0 = 0; var0 < this.warning.length; ++var0) {
                Component var1 = this.warning[var0];
                param0.drawCenteredString(this.font, var1, this.width / 2, row(-1 + var0), -6250336);
            }
        }

    }

    private void renderMultilineMessage(GuiGraphics param0, int param1, int param2, List<TextRenderingUtils.Line> param3) {
        for(int var0 = 0; var0 < param3.size(); ++var0) {
            TextRenderingUtils.Line var1 = param3.get(var0);
            int var2 = row(4 + var0);
            int var3 = var1.segments.stream().mapToInt(param0x -> this.font.width(param0x.renderedText())).sum();
            int var4 = this.width / 2 - var3 / 2;

            for(TextRenderingUtils.LineSegment var5 : var1.segments) {
                int var6 = var5.isLink() ? 3368635 : -1;
                int var7 = param0.drawString(this.font, var5.renderedText(), var4, var2, var6);
                if (var5.isLink() && param1 > var4 && param1 < var7 && param2 > var2 - 3 && param2 < var2 + 8) {
                    this.setTooltipForNextRenderPass(Component.literal(var5.getLinkUrl()));
                    this.currentLink = var5.getLinkUrl();
                }

                var4 = var7;
            }
        }

    }

    int getHeaderHeight() {
        return this.warning != null ? row(1) : 36;
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
        private static final WidgetSprites WEBSITE_LINK_SPRITES = new WidgetSprites(
            new ResourceLocation("icon/link"), new ResourceLocation("icon/link_highlighted")
        );
        private static final WidgetSprites TRAILER_LINK_SPRITES = new WidgetSprites(
            new ResourceLocation("icon/video_link"), new ResourceLocation("icon/video_link_highlighted")
        );
        private static final Component PUBLISHER_LINK_TOOLTIP = Component.translatable("mco.template.info.tooltip");
        private static final Component TRAILER_LINK_TOOLTIP = Component.translatable("mco.template.trailer.tooltip");
        public final WorldTemplate template;
        private long lastClickTime;
        @Nullable
        private ImageButton websiteButton;
        @Nullable
        private ImageButton trailerButton;

        public Entry(WorldTemplate param0) {
            this.template = param0;
            if (!param0.link.isBlank()) {
                this.websiteButton = new ImageButton(
                    15, 15, WEBSITE_LINK_SPRITES, ConfirmLinkScreen.confirmLink(RealmsSelectWorldTemplateScreen.this, param0.link), PUBLISHER_LINK_TOOLTIP
                );
                this.websiteButton.setTooltip(Tooltip.create(PUBLISHER_LINK_TOOLTIP));
            }

            if (!param0.trailer.isBlank()) {
                this.trailerButton = new ImageButton(
                    15, 15, TRAILER_LINK_SPRITES, ConfirmLinkScreen.confirmLink(RealmsSelectWorldTemplateScreen.this, param0.trailer), TRAILER_LINK_TOOLTIP
                );
                this.trailerButton.setTooltip(Tooltip.create(TRAILER_LINK_TOOLTIP));
            }

        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 == 0) {
                RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.template;
                RealmsSelectWorldTemplateScreen.this.updateButtonStates();
                if (Util.getMillis() - this.lastClickTime < 250L && this.isFocused()) {
                    RealmsSelectWorldTemplateScreen.this.callback.accept(this.template);
                }

                this.lastClickTime = Util.getMillis();
                if (this.websiteButton != null) {
                    this.websiteButton.mouseClicked(param0, param1, param2);
                }

                if (this.trailerButton != null) {
                    this.trailerButton.mouseClicked(param0, param1, param2);
                }

                return true;
            } else {
                return super.mouseClicked(param0, param1, param2);
            }
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            param0.blit(RealmsTextureManager.worldTemplate(this.template.id, this.template.image), param3 + 1, param2 + 1 + 1, 0.0F, 0.0F, 38, 38, 38, 38);
            param0.blitSprite(RealmsSelectWorldTemplateScreen.SLOT_FRAME_SPRITE, param3, param2 + 1, 40, 40);
            int var0 = 5;
            int var1 = RealmsSelectWorldTemplateScreen.this.font.width(this.template.version);
            if (this.websiteButton != null) {
                this.websiteButton.setPosition(param3 + param4 - var1 - this.websiteButton.getWidth() - 10, param2);
                this.websiteButton.render(param0, param6, param7, param9);
            }

            if (this.trailerButton != null) {
                this.trailerButton.setPosition(param3 + param4 - var1 - this.trailerButton.getWidth() * 2 - 15, param2);
                this.trailerButton.render(param0, param6, param7, param9);
            }

            int var2 = param3 + 45 + 20;
            int var3 = param2 + 5;
            param0.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.name, var2, var3, -1, false);
            param0.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.version, param3 + param4 - var1 - 5, var3, 7105644, false);
            param0.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.author, var2, var3 + 9 + 5, -6250336, false);
            if (!this.template.recommendedPlayers.isBlank()) {
                param0.drawString(
                    RealmsSelectWorldTemplateScreen.this.font, this.template.recommendedPlayers, var2, param2 + param5 - 9 / 2 - 5, 5000268, false
                );
            }

        }

        @Override
        public Component getNarration() {
            Component var0 = CommonComponents.joinLines(
                Component.literal(this.template.name),
                Component.translatable("mco.template.select.narrate.authors", this.template.author),
                Component.literal(this.template.recommendedPlayers),
                Component.translatable("mco.template.select.narrate.version", this.template.version)
            );
            return Component.translatable("narrator.select", var0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WorldTemplateList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
        public WorldTemplateList() {
            this(Collections.emptyList());
        }

        public WorldTemplateList(Iterable<WorldTemplate> param0) {
            super(
                RealmsSelectWorldTemplateScreen.this.width,
                RealmsSelectWorldTemplateScreen.this.height - 36 - RealmsSelectWorldTemplateScreen.this.getHeaderHeight(),
                RealmsSelectWorldTemplateScreen.this.getHeaderHeight(),
                46
            );
            param0.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate param0x) {
            this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(param0x));
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
                ConfirmLinkScreen.confirmLinkNow(RealmsSelectWorldTemplateScreen.this, RealmsSelectWorldTemplateScreen.this.currentLink);
                return true;
            } else {
                return super.mouseClicked(param0, param1, param2);
            }
        }

        public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry param0) {
            super.setSelected(param0);
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = param0 == null ? null : param0.template;
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 46;
        }

        @Override
        public int getRowWidth() {
            return 300;
        }

        public boolean isEmpty() {
            return this.getItemCount() == 0;
        }

        public List<WorldTemplate> getTemplates() {
            return this.children().stream().map(param0 -> param0.template).collect(Collectors.toList());
        }
    }
}
