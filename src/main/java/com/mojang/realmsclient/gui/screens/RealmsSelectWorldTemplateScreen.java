package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
    private static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
    private static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    private final RealmsScreenWithCallback lastScreen;
    private RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
    private int selectedTemplate = -1;
    private Component title;
    private Button selectButton;
    private Button trailerButton;
    private Button publisherButton;
    private String toolTip;
    private String currentLink;
    private final RealmsServer.WorldType worldType;
    private int clicks;
    @Nullable
    private Component[] warning;
    private String warningURL;
    private boolean displayWarning;
    private boolean hoverWarning;
    private List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(RealmsScreenWithCallback param0, RealmsServer.WorldType param1) {
        this(param0, param1, null);
    }

    public RealmsSelectWorldTemplateScreen(RealmsScreenWithCallback param0, RealmsServer.WorldType param1, @Nullable WorldTemplatePaginatedList param2) {
        this.lastScreen = param0;
        this.worldType = param1;
        if (param2 == null) {
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
            this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
        } else {
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(param2.templates));
            this.fetchTemplatesAsync(param2);
        }

        this.title = new TranslatableComponent("mco.template.title");
    }

    public void setTitle(Component param0) {
        this.title = param0;
    }

    public void setWarning(Component... param0) {
        this.warning = param0;
        this.displayWarning = true;
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.hoverWarning && this.warningURL != null) {
            Util.getPlatform().openUri("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(
            this.worldTemplateObjectSelectionList.getTemplates()
        );
        this.trailerButton = this.addButton(
            new Button(this.width / 2 - 206, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.trailer"), param0 -> this.onTrailer())
        );
        this.selectButton = this.addButton(
            new Button(
                this.width / 2 - 100, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.select"), param0 -> this.selectTemplate()
            )
        );
        Component var0 = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
        Button var1 = new Button(this.width / 2 + 6, this.height - 32, 100, 20, var0, param0 -> this.backButtonClicked());
        this.addButton(var1);
        this.publisherButton = this.addButton(
            new Button(this.width / 2 + 112, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.publisher"), param0 -> this.onPublish())
        );
        this.selectButton.active = false;
        this.trailerButton.visible = false;
        this.publisherButton.visible = false;
        this.addWidget(this.worldTemplateObjectSelectionList);
        this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
        Stream<Component> var2 = Stream.of(this.title);
        if (this.warning != null) {
            var2 = Stream.concat(Stream.of(this.warning), var2);
        }

        NarrationHelper.now(var2.filter(Objects::nonNull).map(Component::getString).collect(Collectors.toList()));
    }

    private void updateButtonStates() {
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
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.backButtonClicked();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void backButtonClicked() {
        this.lastScreen.callback(null);
        this.minecraft.setScreen(this.lastScreen);
    }

    private void selectTemplate() {
        if (this.hasValidTemplate()) {
            this.lastScreen.callback(this.getSelectedTemplate());
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
                                                    I18n.get("mco.template.select.none.linkTitle"), "https://minecraft.net/realms/content-creator/"
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

    private Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList param0, RealmsClient param1) {
        try {
            return Either.left(param1.fetchWorldTemplates(param0.page + 1, param0.size, this.worldType));
        } catch (RealmsServiceException var4) {
            return Either.right(var4.getMessage());
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.currentLink = null;
        this.hoverWarning = false;
        this.renderBackground(param0);
        this.worldTemplateObjectSelectionList.render(param0, param1, param2, param3);
        if (this.noTemplatesMessage != null) {
            this.renderMultilineMessage(param0, param1, param2, this.noTemplatesMessage);
        }

        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 13, 16777215);
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
                        var6 = var6.mutableCopy().withStyle(ChatFormatting.STRIKETHROUGH);
                    } else {
                        var7 = 3368635;
                    }
                }

                this.drawCenteredString(param0, this.font, var6, this.width / 2, row(-1 + var5), var7);
            }
        }

        super.render(param0, param1, param2, param3);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(param0, this.toolTip, param1, param2);
        }

    }

    private void renderMultilineMessage(PoseStack param0, int param1, int param2, List<TextRenderingUtils.Line> param3) {
        for(int var0 = 0; var0 < param3.size(); ++var0) {
            TextRenderingUtils.Line var1 = param3.get(var0);
            int var2 = row(4 + var0);
            int var3 = var1.segments.stream().mapToInt(param0x -> this.font.width(param0x.renderedText())).sum();
            int var4 = this.width / 2 - var3 / 2;

            for(TextRenderingUtils.LineSegment var5 : var1.segments) {
                int var6 = var5.isLink() ? 3368635 : 16777215;
                int var7 = this.font.drawShadow(param0, var5.renderedText(), (float)var4, (float)var2, var6);
                if (var5.isLink() && param1 > var4 && param1 < var7 && param2 > var2 - 3 && param2 < var2 + 8) {
                    this.toolTip = var5.getLinkUrl();
                    this.currentLink = var5.getLinkUrl();
                }

                var4 = var7;
            }
        }

    }

    protected void renderMousehoverTooltip(PoseStack param0, String param1, int param2, int param3) {
        if (param1 != null) {
            int var0 = param2 + 12;
            int var1 = param3 - 12;
            int var2 = this.font.width(param1);
            this.fillGradient(param0, var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(param0, param1, (float)var0, (float)var1, 16777215);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
        private final WorldTemplate template;

        public Entry(WorldTemplate param0) {
            this.template = param0;
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderWorldTemplateItem(param0, this.template, param3, param2, param6, param7);
        }

        private void renderWorldTemplateItem(PoseStack param0, WorldTemplate param1, int param2, int param3, int param4, int param5) {
            int var0 = param2 + 45 + 20;
            RealmsSelectWorldTemplateScreen.this.font.draw(param0, param1.name, (float)var0, (float)(param3 + 2), 16777215);
            RealmsSelectWorldTemplateScreen.this.font.draw(param0, param1.author, (float)var0, (float)(param3 + 15), 7105644);
            RealmsSelectWorldTemplateScreen.this.font
                .draw(
                    param0, param1.version, (float)(var0 + 227 - RealmsSelectWorldTemplateScreen.this.font.width(param1.version)), (float)(param3 + 1), 7105644
                );
            if (!"".equals(param1.link) || !"".equals(param1.trailer) || !"".equals(param1.recommendedPlayers)) {
                this.drawIcons(param0, var0 - 1, param3 + 25, param4, param5, param1.link, param1.trailer, param1.recommendedPlayers);
            }

            this.drawImage(param0, param2, param3 + 1, param4, param5, param1);
        }

        private void drawImage(PoseStack param0, int param1, int param2, int param3, int param4, WorldTemplate param5) {
            RealmsTextureManager.bindWorldTemplate(param5.id, param5.image);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param0, param1 + 1, param2 + 1, 0.0F, 0.0F, 38, 38, 38, 38);
            RealmsSelectWorldTemplateScreen.this.minecraft.getTextureManager().bind(RealmsSelectWorldTemplateScreen.SLOT_FRAME_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 40, 40, 40, 40);
        }

        private void drawIcons(PoseStack param0, int param1, int param2, int param3, int param4, String param5, String param6, String param7) {
            if (!"".equals(param7)) {
                RealmsSelectWorldTemplateScreen.this.font.draw(param0, param7, (float)param1, (float)(param2 + 4), 5000268);
            }

            int var0 = "".equals(param7) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(param7) + 2;
            boolean var1 = false;
            boolean var2 = false;
            if (param3 >= param1 + var0
                && param3 <= param1 + var0 + 32
                && param4 >= param2
                && param4 <= param2 + 15
                && param4 < RealmsSelectWorldTemplateScreen.this.height - 15
                && param4 > 32) {
                if (param3 <= param1 + 15 + var0 && param3 > var0) {
                    if ("".equals(param5)) {
                        var2 = true;
                    } else {
                        var1 = true;
                    }
                } else if (!"".equals(param5)) {
                    var2 = true;
                }
            }

            if (!"".equals(param5)) {
                RealmsSelectWorldTemplateScreen.this.minecraft.getTextureManager().bind(RealmsSelectWorldTemplateScreen.LINK_ICON);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.pushMatrix();
                RenderSystem.scalef(1.0F, 1.0F, 1.0F);
                float var3 = var1 ? 15.0F : 0.0F;
                GuiComponent.blit(param0, param1 + var0, param2, var3, 0.0F, 15, 15, 30, 15);
                RenderSystem.popMatrix();
            }

            if (!"".equals(param6)) {
                RealmsSelectWorldTemplateScreen.this.minecraft.getTextureManager().bind(RealmsSelectWorldTemplateScreen.TRAILER_ICON);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.pushMatrix();
                RenderSystem.scalef(1.0F, 1.0F, 1.0F);
                int var4 = param1 + var0 + ("".equals(param5) ? 0 : 17);
                float var5 = var2 ? 15.0F : 0.0F;
                GuiComponent.blit(param0, var4, param2, var5, 0.0F, 15, 15, 30, 15);
                RenderSystem.popMatrix();
            }

            if (var1 && !"".equals(param5)) {
                RealmsSelectWorldTemplateScreen.this.toolTip = I18n.get("mco.template.info.tooltip");
                RealmsSelectWorldTemplateScreen.this.currentLink = param5;
            } else if (var2 && !"".equals(param6)) {
                RealmsSelectWorldTemplateScreen.this.toolTip = I18n.get("mco.template.trailer.tooltip");
                RealmsSelectWorldTemplateScreen.this.currentLink = param6;
            }

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
                    this.itemClicked(var1, var2, param0, param1, this.width);
                    if (var2 >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                        return super.mouseClicked(param0, param1, param2);
                    }

                    RealmsSelectWorldTemplateScreen.this.clicks = RealmsSelectWorldTemplateScreen.this.clicks + 7;
                    if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                        RealmsSelectWorldTemplateScreen.this.selectTemplate();
                    }

                    return true;
                }
            }

            return super.mouseClicked(param0, param1, param2);
        }

        @Override
        public void selectItem(int param0) {
            this.setSelectedItem(param0);
            if (param0 != -1) {
                WorldTemplate var0 = RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.get(param0);
                String var1 = I18n.get(
                    "narrator.select.list.position", param0 + 1, RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()
                );
                String var2 = I18n.get("mco.template.select.narrate.version", var0.version);
                String var3 = I18n.get("mco.template.select.narrate.authors", var0.author);
                String var4 = NarrationHelper.join(Arrays.asList(var0.name, var3, var0.recommendedPlayers, var2, var1));
                NarrationHelper.now(I18n.get("narrator.select", var4));
            }

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
        public void renderBackground(PoseStack param0) {
            RealmsSelectWorldTemplateScreen.this.renderBackground(param0);
        }

        @Override
        public boolean isFocused() {
            return RealmsSelectWorldTemplateScreen.this.getFocused() == this;
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
