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
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
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
    static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
    static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
    static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    static final Component PUBLISHER_LINK_TOOLTIP = Component.translatable("mco.template.info.tooltip");
    static final Component TRAILER_LINK_TOOLTIP = Component.translatable("mco.template.trailer.tooltip");
    private final Consumer<WorldTemplate> callback;
    RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
    int selectedTemplate = -1;
    private Button selectButton;
    private Button trailerButton;
    private Button publisherButton;
    @Nullable
    Component toolTip;
    @Nullable
    String currentLink;
    private final RealmsServer.WorldType worldType;
    int clicks;
    @Nullable
    private Component[] warning;
    private String warningURL;
    boolean displayWarning;
    private boolean hoverWarning;
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
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
            this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
        } else {
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(param3.templates));
            this.fetchTemplatesAsync(param3);
        }

    }

    public void setWarning(Component... param0) {
        this.warning = param0;
        this.displayWarning = true;
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.hoverWarning && this.warningURL != null) {
            Util.getPlatform().openUri("https://www.minecraft.net/realms/adventure-maps-in-1-9");
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public void init() {
        this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(
            this.worldTemplateObjectSelectionList.getTemplates()
        );
        this.trailerButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.template.button.trailer"), param0 -> this.onTrailer())
                .bounds(this.width / 2 - 206, this.height - 32, 100, 20)
                .build()
        );
        this.selectButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.template.button.select"), param0 -> this.selectTemplate())
                .bounds(this.width / 2 - 100, this.height - 32, 100, 20)
                .build()
        );
        Component var0 = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
        Button var1 = Button.builder(var0, param0 -> this.onClose()).bounds(this.width / 2 + 6, this.height - 32, 100, 20).build();
        this.addRenderableWidget(var1);
        this.publisherButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.template.button.publisher"), param0 -> this.onPublish())
                .bounds(this.width / 2 + 112, this.height - 32, 100, 20)
                .build()
        );
        this.selectButton.active = false;
        this.trailerButton.visible = false;
        this.publisherButton.visible = false;
        this.addWidget(this.worldTemplateObjectSelectionList);
        this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
    }

    @Override
    public Component getNarrationMessage() {
        List<Component> var0 = Lists.newArrayListWithCapacity(2);
        if (this.title != null) {
            var0.add(this.title);
        }

        if (this.warning != null) {
            var0.addAll(Arrays.asList(this.warning));
        }

        return CommonComponents.joinLines(var0);
    }

    void updateButtonStates() {
        this.publisherButton.visible = this.shouldPublisherBeVisible();
        this.trailerButton.visible = this.shouldTrailerBeVisible();
        this.selectButton.active = this.shouldSelectButtonBeActive();
    }

    private boolean shouldSelectButtonBeActive() {
        return this.selectedTemplate != -1;
    }

    private boolean shouldPublisherBeVisible() {
        return this.selectedTemplate != -1 && !this.getSelectedTemplate().link.isEmpty();
    }

    private WorldTemplate getSelectedTemplate() {
        return this.worldTemplateObjectSelectionList.get(this.selectedTemplate);
    }

    private boolean shouldTrailerBeVisible() {
        return this.selectedTemplate != -1 && !this.getSelectedTemplate().trailer.isEmpty();
    }

    @Override
    public void tick() {
        super.tick();
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }

    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }

    void selectTemplate() {
        if (this.hasValidTemplate()) {
            this.callback.accept(this.getSelectedTemplate());
        }

    }

    private boolean hasValidTemplate() {
        return this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount();
    }

    private void onTrailer() {
        if (this.hasValidTemplate()) {
            WorldTemplate var0 = this.getSelectedTemplate();
            if (!"".equals(var0.trailer)) {
                Util.getPlatform().openUri(var0.trailer);
            }
        }

    }

    private void onPublish() {
        if (this.hasValidTemplate()) {
            WorldTemplate var0 = this.getSelectedTemplate();
            if (!"".equals(var0.link)) {
                Util.getPlatform().openUri(var0.link);
            }
        }

    }

    private void fetchTemplatesAsync(final WorldTemplatePaginatedList param0) {
        (new Thread("realms-template-fetcher") {
                @Override
                public void run() {
                    WorldTemplatePaginatedList var0 = param0;
    
                    Either<WorldTemplatePaginatedList, String> var2;
                    for(RealmsClient var1 = RealmsClient.create();
                        var0 != null;
                        var0 = RealmsSelectWorldTemplateScreen.this.minecraft
                            .submit(
                                () -> {
                                    if (var2.right().isPresent()) {
                                        RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", var2.right().get());
                                        if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                                            RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(
                                                I18n.get("mco.template.select.failure")
                                            );
                                        }
                
                                        return null;
                                    } else {
                                        WorldTemplatePaginatedList var0x = var2.left().get();
                
                                        for(WorldTemplate var3x : var0x.templates) {
                                            RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(var3x);
                                        }
                
                                        if (var0x.templates.isEmpty()) {
                                            if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
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

    Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList param0, RealmsClient param1) {
        try {
            return Either.left(param1.fetchWorldTemplates(param0.page + 1, param0.size, this.worldType));
        } catch (RealmsServiceException var4) {
            return Either.right(var4.getMessage());
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.currentLink = null;
        this.hoverWarning = false;
        this.renderBackground(param0);
        this.worldTemplateObjectSelectionList.render(param0, param1, param2, param3);
        if (this.noTemplatesMessage != null) {
            this.renderMultilineMessage(param0, param1, param2, this.noTemplatesMessage);
        }

        param0.drawCenteredString(this.font, this.title, this.width / 2, 13, 16777215);
        if (this.displayWarning) {
            Component[] var0 = this.warning;

            for(int var1 = 0; var1 < var0.length; ++var1) {
                int var2 = this.font.width(var0[var1]);
                int var3 = this.width / 2 - var2 / 2;
                int var4 = row(-1 + var1);
                if (param1 >= var3 && param1 <= var3 + var2 && param2 >= var4 && param2 <= var4 + 9) {
                    this.hoverWarning = true;
                }
            }

            for(int var5 = 0; var5 < var0.length; ++var5) {
                Component var6 = var0[var5];
                int var7 = 10526880;
                if (this.warningURL != null) {
                    if (this.hoverWarning) {
                        var7 = 7107012;
                        var6 = var6.copy().withStyle(ChatFormatting.STRIKETHROUGH);
                    } else {
                        var7 = 3368635;
                    }
                }

                param0.drawCenteredString(this.font, var6, this.width / 2, row(-1 + var5), var7);
            }
        }

        super.render(param0, param1, param2, param3);
        this.renderMousehoverTooltip(param0, this.toolTip, param1, param2);
    }

    private void renderMultilineMessage(GuiGraphics param0, int param1, int param2, List<TextRenderingUtils.Line> param3) {
        for(int var0 = 0; var0 < param3.size(); ++var0) {
            TextRenderingUtils.Line var1 = param3.get(var0);
            int var2 = row(4 + var0);
            int var3 = var1.segments.stream().mapToInt(param0x -> this.font.width(param0x.renderedText())).sum();
            int var4 = this.width / 2 - var3 / 2;

            for(TextRenderingUtils.LineSegment var5 : var1.segments) {
                int var6 = var5.isLink() ? 3368635 : 16777215;
                int var7 = param0.drawString(this.font, var5.renderedText(), var4, var2, var6);
                if (var5.isLink() && param1 > var4 && param1 < var7 && param2 > var2 - 3 && param2 < var2 + 8) {
                    this.toolTip = Component.literal(var5.getLinkUrl());
                    this.currentLink = var5.getLinkUrl();
                }

                var4 = var7;
            }
        }

    }

    protected void renderMousehoverTooltip(GuiGraphics param0, @Nullable Component param1, int param2, int param3) {
        if (param1 != null) {
            int var0 = param2 + 12;
            int var1 = param3 - 12;
            int var2 = this.font.width(param1);
            param0.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            param0.drawString(this.font, param1, var0, var1, 16777215);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
        final WorldTemplate template;

        public Entry(WorldTemplate param0) {
            this.template = param0;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderWorldTemplateItem(param0, this.template, param3, param2, param6, param7);
        }

        private void renderWorldTemplateItem(GuiGraphics param0, WorldTemplate param1, int param2, int param3, int param4, int param5) {
            int var0 = param2 + 45 + 20;
            param0.drawString(RealmsSelectWorldTemplateScreen.this.font, param1.name, var0, param3 + 2, 16777215, false);
            param0.drawString(RealmsSelectWorldTemplateScreen.this.font, param1.author, var0, param3 + 15, 7105644, false);
            param0.drawString(
                RealmsSelectWorldTemplateScreen.this.font,
                param1.version,
                var0 + 227 - RealmsSelectWorldTemplateScreen.this.font.width(param1.version),
                param3 + 1,
                7105644,
                false
            );
            if (!"".equals(param1.link) || !"".equals(param1.trailer) || !"".equals(param1.recommendedPlayers)) {
                this.drawIcons(param0, var0 - 1, param3 + 25, param4, param5, param1.link, param1.trailer, param1.recommendedPlayers);
            }

            this.drawImage(param0, param2, param3 + 1, param4, param5, param1);
        }

        private void drawImage(GuiGraphics param0, int param1, int param2, int param3, int param4, WorldTemplate param5) {
            param0.blit(RealmsTextureManager.worldTemplate(param5.id, param5.image), param1 + 1, param2 + 1, 0.0F, 0.0F, 38, 38, 38, 38);
            param0.blit(RealmsSelectWorldTemplateScreen.SLOT_FRAME_LOCATION, param1, param2, 0.0F, 0.0F, 40, 40, 40, 40);
        }

        private void drawIcons(GuiGraphics param0, int param1, int param2, int param3, int param4, String param5, String param6, String param7) {
            if (!"".equals(param7)) {
                param0.drawString(RealmsSelectWorldTemplateScreen.this.font, param7, param1, param2 + 4, 5000268, false);
            }

            int var0 = "".equals(param7) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(param7) + 2;
            boolean var1 = false;
            boolean var2 = false;
            boolean var3 = "".equals(param5);
            if (param3 >= param1 + var0
                && param3 <= param1 + var0 + 32
                && param4 >= param2
                && param4 <= param2 + 15
                && param4 < RealmsSelectWorldTemplateScreen.this.height - 15
                && param4 > 32) {
                if (param3 <= param1 + 15 + var0 && param3 > var0) {
                    if (var3) {
                        var2 = true;
                    } else {
                        var1 = true;
                    }
                } else if (!var3) {
                    var2 = true;
                }
            }

            if (!var3) {
                float var4 = var1 ? 15.0F : 0.0F;
                param0.blit(RealmsSelectWorldTemplateScreen.LINK_ICON, param1 + var0, param2, var4, 0.0F, 15, 15, 30, 15);
            }

            if (!"".equals(param6)) {
                int var5 = param1 + var0 + (var3 ? 0 : 17);
                float var6 = var2 ? 15.0F : 0.0F;
                param0.blit(RealmsSelectWorldTemplateScreen.TRAILER_ICON, var5, param2, var6, 0.0F, 15, 15, 30, 15);
            }

            if (var1) {
                RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.PUBLISHER_LINK_TOOLTIP;
                RealmsSelectWorldTemplateScreen.this.currentLink = param5;
            } else if (var2 && !"".equals(param6)) {
                RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.TRAILER_LINK_TOOLTIP;
                RealmsSelectWorldTemplateScreen.this.currentLink = param6;
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
    class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
        public WorldTemplateObjectSelectionList() {
            this(Collections.emptyList());
        }

        public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> param0) {
            super(
                RealmsSelectWorldTemplateScreen.this.width,
                RealmsSelectWorldTemplateScreen.this.height,
                RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsSelectWorldTemplateScreen.row(1) : 32,
                RealmsSelectWorldTemplateScreen.this.height - 40,
                46
            );
            param0.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate param0x) {
            this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(param0x));
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 == 0 && param1 >= (double)this.y0 && param1 <= (double)this.y1) {
                int var0 = this.width / 2 - 150;
                if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
                    Util.getPlatform().openUri(RealmsSelectWorldTemplateScreen.this.currentLink);
                }

                int var1 = (int)Math.floor(param1 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int var2 = var1 / this.itemHeight;
                if (param0 >= (double)var0 && param0 < (double)this.getScrollbarPosition() && var2 >= 0 && var1 >= 0 && var2 < this.getItemCount()) {
                    this.selectItem(var2);
                    this.itemClicked(var1, var2, param0, param1, this.width, param2);
                    if (var2 >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                        return super.mouseClicked(param0, param1, param2);
                    }

                    RealmsSelectWorldTemplateScreen.this.clicks += 7;
                    if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                        RealmsSelectWorldTemplateScreen.this.selectTemplate();
                    }

                    return true;
                }
            }

            return super.mouseClicked(param0, param1, param2);
        }

        public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry param0) {
            super.setSelected(param0);
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(param0);
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

        @Override
        public void renderBackground(GuiGraphics param0) {
            RealmsSelectWorldTemplateScreen.this.renderBackground(param0);
        }

        public boolean isEmpty() {
            return this.getItemCount() == 0;
        }

        public WorldTemplate get(int param0) {
            return this.children().get(param0).template;
        }

        public List<WorldTemplate> getTemplates() {
            return this.children().stream().map(param0 -> param0.template).collect(Collectors.toList());
        }
    }
}
