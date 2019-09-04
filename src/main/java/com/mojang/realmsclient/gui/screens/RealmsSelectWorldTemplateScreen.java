package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreenWithCallback<WorldTemplate> lastScreen;
    private RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
    private int selectedTemplate = -1;
    private String title;
    private RealmsButton selectButton;
    private RealmsButton trailerButton;
    private RealmsButton publisherButton;
    private String toolTip;
    private String currentLink;
    private final RealmsServer.WorldType worldType;
    private int clicks;
    private String warning;
    private String warningURL;
    private boolean displayWarning;
    private boolean hoverWarning;
    private List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(RealmsScreenWithCallback<WorldTemplate> param0, RealmsServer.WorldType param1) {
        this(param0, param1, null);
    }

    public RealmsSelectWorldTemplateScreen(
        RealmsScreenWithCallback<WorldTemplate> param0, RealmsServer.WorldType param1, @Nullable WorldTemplatePaginatedList param2
    ) {
        this.lastScreen = param0;
        this.worldType = param1;
        if (param2 == null) {
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
            this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
        } else {
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(param2.templates));
            this.fetchTemplatesAsync(param2);
        }

        this.title = getLocalizedString("mco.template.title");
    }

    public void setTitle(String param0) {
        this.title = param0;
    }

    public void setWarning(String param0) {
        this.warning = param0;
        this.displayWarning = true;
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.hoverWarning && this.warningURL != null) {
            RealmsUtil.browseTo("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(
            this.worldTemplateObjectSelectionList.getTemplates()
        );
        this.buttonsAdd(
            this.trailerButton = new RealmsButton(2, this.width() / 2 - 206, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.trailer")) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen.this.onTrailer();
                }
            }
        );
        this.buttonsAdd(
            this.selectButton = new RealmsButton(1, this.width() / 2 - 100, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.select")) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen.this.selectTemplate();
                }
            }
        );
        this.buttonsAdd(
            new RealmsButton(
                0,
                this.width() / 2 + 6,
                this.height() - 32,
                100,
                20,
                getLocalizedString(this.worldType == RealmsServer.WorldType.MINIGAME ? "gui.cancel" : "gui.back")
            ) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen.this.backButtonClicked();
                }
            }
        );
        this.publisherButton = new RealmsButton(3, this.width() / 2 + 112, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.publisher")) {
            @Override
            public void onPress() {
                RealmsSelectWorldTemplateScreen.this.onPublish();
            }
        };
        this.buttonsAdd(this.publisherButton);
        this.selectButton.active(false);
        this.trailerButton.setVisible(false);
        this.publisherButton.setVisible(false);
        this.addWidget(this.worldTemplateObjectSelectionList);
        this.focusOn(this.worldTemplateObjectSelectionList);
        Realms.narrateNow(Stream.of(this.title, this.warning).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    private void updateButtonStates() {
        this.publisherButton.setVisible(this.shouldPublisherBeVisible());
        this.trailerButton.setVisible(this.shouldTrailerBeVisible());
        this.selectButton.active(this.shouldSelectButtonBeActive());
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
        switch(param0) {
            case 256:
                this.backButtonClicked();
                return true;
            default:
                return super.keyPressed(param0, param1, param2);
        }
    }

    private void backButtonClicked() {
        this.lastScreen.callback(null);
        Realms.setScreen(this.lastScreen);
    }

    private void selectTemplate() {
        if (this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount()) {
            WorldTemplate var0 = this.getSelectedTemplate();
            this.lastScreen.callback(var0);
        }

    }

    private void onTrailer() {
        if (this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount()) {
            WorldTemplate var0 = this.getSelectedTemplate();
            if (!"".equals(var0.trailer)) {
                RealmsUtil.browseTo(var0.trailer);
            }
        }

    }

    private void onPublish() {
        if (this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount()) {
            WorldTemplate var0 = this.getSelectedTemplate();
            if (!"".equals(var0.link)) {
                RealmsUtil.browseTo(var0.link);
            }
        }

    }

    private void fetchTemplatesAsync(final WorldTemplatePaginatedList param0) {
        (new Thread("realms-template-fetcher") {
                @Override
                public void run() {
                    WorldTemplatePaginatedList var0 = param0;
    
                    Either<WorldTemplatePaginatedList, String> var2;
                    for(RealmsClient var1 = RealmsClient.createRealmsClient();
                        var0 != null;
                        var0 = Realms.execute(
                                () -> {
                                    if (var2.right().isPresent()) {
                                        RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", var2.right().get());
                                        if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                                            RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(
                                                RealmsScreen.getLocalizedString("mco.template.select.failure")
                                            );
                                        }
                
                                        return null;
                                    } else {
                                        assert var2.left().isPresent();
                
                                        WorldTemplatePaginatedList var0x = var2.left().get();
                
                                        for(WorldTemplate var3x : var0x.templates) {
                                            RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(var3x);
                                        }
                
                                        if (var0x.templates.isEmpty()) {
                                            if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                                                String var2x = RealmsScreen.getLocalizedString("mco.template.select.none", "%link");
                                                TextRenderingUtils.LineSegment var3 = TextRenderingUtils.LineSegment.link(
                                                    RealmsScreen.getLocalizedString("mco.template.select.none.linkTitle"),
                                                    "https://minecraft.net/realms/content-creator/"
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
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.currentLink = null;
        this.hoverWarning = false;
        this.renderBackground();
        this.worldTemplateObjectSelectionList.render(param0, param1, param2);
        if (this.noTemplatesMessage != null) {
            this.renderMultilineMessage(param0, param1, this.noTemplatesMessage);
        }

        this.drawCenteredString(this.title, this.width() / 2, 13, 16777215);
        if (this.displayWarning) {
            String[] var0 = this.warning.split("\\\\n");

            for(int var1 = 0; var1 < var0.length; ++var1) {
                int var2 = this.fontWidth(var0[var1]);
                int var3 = this.width() / 2 - var2 / 2;
                int var4 = RealmsConstants.row(-1 + var1);
                if (param0 >= var3 && param0 <= var3 + var2 && param1 >= var4 && param1 <= var4 + this.fontLineHeight()) {
                    this.hoverWarning = true;
                }
            }

            for(int var5 = 0; var5 < var0.length; ++var5) {
                String var6 = var0[var5];
                int var7 = 10526880;
                if (this.warningURL != null) {
                    if (this.hoverWarning) {
                        var7 = 7107012;
                        var6 = "\u00a7n" + var6;
                    } else {
                        var7 = 3368635;
                    }
                }

                this.drawCenteredString(var6, this.width() / 2, RealmsConstants.row(-1 + var5), var7);
            }
        }

        super.render(param0, param1, param2);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, param0, param1);
        }

    }

    private void renderMultilineMessage(int param0, int param1, List<TextRenderingUtils.Line> param2) {
        for(int var0 = 0; var0 < param2.size(); ++var0) {
            TextRenderingUtils.Line var1 = param2.get(var0);
            int var2 = RealmsConstants.row(4 + var0);
            int var3 = var1.segments.stream().mapToInt(param0x -> this.fontWidth(param0x.renderedText())).sum();
            int var4 = this.width() / 2 - var3 / 2;

            for(TextRenderingUtils.LineSegment var5 : var1.segments) {
                int var6 = var5.isLink() ? 3368635 : 16777215;
                int var7 = this.draw(var5.renderedText(), var4, var2, var6, true);
                if (var5.isLink() && param0 > var4 && param0 < var7 && param1 > var2 - 3 && param1 < var2 + 8) {
                    this.toolTip = var5.getLinkUrl();
                    this.currentLink = var5.getLinkUrl();
                }

                var4 = var7;
            }
        }

    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = param1 + 12;
            int var1 = param2 - 12;
            int var2 = this.fontWidth(param0);
            this.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.fontDrawShadow(param0, var0, var1, 16777215);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionListEntry> {
        public WorldTemplateObjectSelectionList() {
            this(Collections.emptyList());
        }

        public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> param0) {
            super(
                RealmsSelectWorldTemplateScreen.this.width(),
                RealmsSelectWorldTemplateScreen.this.height(),
                RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsConstants.row(1) : 32,
                RealmsSelectWorldTemplateScreen.this.height() - 40,
                46
            );
            param0.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate param0x) {
            this.addEntry(RealmsSelectWorldTemplateScreen.this.new WorldTemplateObjectSelectionListEntry(param0x));
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 == 0 && param1 >= (double)this.y0() && param1 <= (double)this.y1()) {
                int var0 = this.width() / 2 - 150;
                if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
                    RealmsUtil.browseTo(RealmsSelectWorldTemplateScreen.this.currentLink);
                }

                int var1 = (int)Math.floor(param1 - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
                int var2 = var1 / this.itemHeight();
                if (param0 >= (double)var0 && param0 < (double)this.getScrollbarPosition() && var2 >= 0 && var1 >= 0 && var2 < this.getItemCount()) {
                    this.selectItem(var2);
                    this.itemClicked(var1, var2, param0, param1, this.width());
                    if (var2 >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                        return super.mouseClicked(param0, param1, param2);
                    }

                    RealmsSelectWorldTemplateScreen.this.selectedTemplate = var2;
                    RealmsSelectWorldTemplateScreen.this.updateButtonStates();
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
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = param0;
            this.setSelected(param0);
            if (param0 != -1) {
                WorldTemplate var0 = RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.get(param0);
                String var1 = RealmsScreen.getLocalizedString(
                    "narrator.select.list.position", param0 + 1, RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()
                );
                String var2 = RealmsScreen.getLocalizedString("mco.template.select.narrate.version", var0.version);
                String var3 = RealmsScreen.getLocalizedString("mco.template.select.narrate.authors", var0.author);
                String var4 = Realms.joinNarrations(Arrays.asList(var0.name, var3, var0.recommendedPlayers, var2, var1));
                Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", var4));
            }

            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
        }

        @Override
        public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
            if (param1 < RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                ;
            }
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
        public void renderBackground() {
            RealmsSelectWorldTemplateScreen.this.renderBackground();
        }

        @Override
        public boolean isFocused() {
            return RealmsSelectWorldTemplateScreen.this.isFocused(this);
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

    @OnlyIn(Dist.CLIENT)
    class WorldTemplateObjectSelectionListEntry extends RealmListEntry {
        final WorldTemplate template;

        public WorldTemplateObjectSelectionListEntry(WorldTemplate param0) {
            this.template = param0;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderWorldTemplateItem(this.template, param2, param1, param5, param6);
        }

        private void renderWorldTemplateItem(WorldTemplate param0, int param1, int param2, int param3, int param4) {
            int var0 = param1 + 45 + 20;
            RealmsSelectWorldTemplateScreen.this.drawString(param0.name, var0, param2 + 2, 16777215);
            RealmsSelectWorldTemplateScreen.this.drawString(param0.author, var0, param2 + 15, 7105644);
            RealmsSelectWorldTemplateScreen.this.drawString(
                param0.version, var0 + 227 - RealmsSelectWorldTemplateScreen.this.fontWidth(param0.version), param2 + 1, 7105644
            );
            if (!"".equals(param0.link) || !"".equals(param0.trailer) || !"".equals(param0.recommendedPlayers)) {
                this.drawIcons(var0 - 1, param2 + 25, param3, param4, param0.link, param0.trailer, param0.recommendedPlayers);
            }

            this.drawImage(param1, param2 + 1, param3, param4, param0);
        }

        private void drawImage(int param0, int param1, int param2, int param3, WorldTemplate param4) {
            RealmsTextureManager.bindWorldTemplate(param4.id, param4.image);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(param0 + 1, param1 + 1, 0.0F, 0.0F, 38, 38, 38, 38);
            RealmsScreen.bind("realms:textures/gui/realms/slot_frame.png");
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 40, 40, 40, 40);
        }

        private void drawIcons(int param0, int param1, int param2, int param3, String param4, String param5, String param6) {
            if (!"".equals(param6)) {
                RealmsSelectWorldTemplateScreen.this.drawString(param6, param0, param1 + 4, 5000268);
            }

            int var0 = "".equals(param6) ? 0 : RealmsSelectWorldTemplateScreen.this.fontWidth(param6) + 2;
            boolean var1 = false;
            boolean var2 = false;
            if (param2 >= param0 + var0
                && param2 <= param0 + var0 + 32
                && param3 >= param1
                && param3 <= param1 + 15
                && param3 < RealmsSelectWorldTemplateScreen.this.height() - 15
                && param3 > 32) {
                if (param2 <= param0 + 15 + var0 && param2 > var0) {
                    if ("".equals(param4)) {
                        var2 = true;
                    } else {
                        var1 = true;
                    }
                } else if (!"".equals(param4)) {
                    var2 = true;
                }
            }

            if (!"".equals(param4)) {
                RealmsScreen.bind("realms:textures/gui/realms/link_icons.png");
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.pushMatrix();
                RenderSystem.scalef(1.0F, 1.0F, 1.0F);
                RealmsScreen.blit(param0 + var0, param1, var1 ? 15.0F : 0.0F, 0.0F, 15, 15, 30, 15);
                RenderSystem.popMatrix();
            }

            if (!"".equals(param5)) {
                RealmsScreen.bind("realms:textures/gui/realms/trailer_icons.png");
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.pushMatrix();
                RenderSystem.scalef(1.0F, 1.0F, 1.0F);
                RealmsScreen.blit(param0 + var0 + ("".equals(param4) ? 0 : 17), param1, var2 ? 15.0F : 0.0F, 0.0F, 15, 15, 30, 15);
                RenderSystem.popMatrix();
            }

            if (var1 && !"".equals(param4)) {
                RealmsSelectWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.info.tooltip");
                RealmsSelectWorldTemplateScreen.this.currentLink = param4;
            } else if (var2 && !"".equals(param5)) {
                RealmsSelectWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.trailer.tooltip");
                RealmsSelectWorldTemplateScreen.this.currentLink = param5;
            }

        }
    }
}
