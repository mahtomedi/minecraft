package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class Screen extends AbstractContainerEventHandler implements TickableWidget, Widget {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");
    protected final Component title;
    protected final List<GuiEventListener> children = Lists.newArrayList();
    @Nullable
    protected Minecraft minecraft;
    protected ItemRenderer itemRenderer;
    public int width;
    public int height;
    protected final List<AbstractWidget> buttons = Lists.newArrayList();
    public boolean passEvents;
    protected Font font;
    private URI clickedLink;

    protected Screen(Component param0) {
        this.title = param0;
    }

    public Component getTitle() {
        return this.title;
    }

    public String getNarrationMessage() {
        return this.getTitle().getString();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        for(int var0 = 0; var0 < this.buttons.size(); ++var0) {
            this.buttons.get(var0).render(param0, param1, param2, param3);
        }

    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        } else if (param0 == 258) {
            boolean var0 = !hasShiftDown();
            if (!this.changeFocus(var0)) {
                this.changeFocus(var0);
            }

            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    protected <T extends AbstractWidget> T addButton(T param0) {
        this.buttons.add(param0);
        return this.addWidget(param0);
    }

    protected <T extends GuiEventListener> T addWidget(T param0) {
        this.children.add(param0);
        return param0;
    }

    protected void renderTooltip(PoseStack param0, ItemStack param1, int param2, int param3) {
        this.renderTooltip(param0, this.getTooltipFromItem(param1), param2, param3);
    }

    public List<Component> getTooltipFromItem(ItemStack param0) {
        return param0.getTooltipLines(
            this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
        );
    }

    public void renderTooltip(PoseStack param0, Component param1, int param2, int param3) {
        this.renderTooltip(param0, Arrays.asList(param1), param2, param3);
    }

    public void renderTooltip(PoseStack param0, List<Component> param1, int param2, int param3) {
        if (!param1.isEmpty()) {
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableDepthTest();
            int var0 = 0;

            for(Component var1 : param1) {
                int var2 = this.font.width(var1);
                if (var2 > var0) {
                    var0 = var2;
                }
            }

            int var3 = param2 + 12;
            int var4 = param3 - 12;
            int var6 = 8;
            if (param1.size() > 1) {
                var6 += 2 + (param1.size() - 1) * 10;
            }

            if (var3 + var0 > this.width) {
                var3 -= 28 + var0;
            }

            if (var4 + var6 + 6 > this.height) {
                var4 = this.height - var6 - 6;
            }

            this.setBlitOffset(300);
            this.itemRenderer.blitOffset = 300.0F;
            int var7 = -267386864;
            this.fillGradient(param0, var3 - 3, var4 - 4, var3 + var0 + 3, var4 - 3, -267386864, -267386864);
            this.fillGradient(param0, var3 - 3, var4 + var6 + 3, var3 + var0 + 3, var4 + var6 + 4, -267386864, -267386864);
            this.fillGradient(param0, var3 - 3, var4 - 3, var3 + var0 + 3, var4 + var6 + 3, -267386864, -267386864);
            this.fillGradient(param0, var3 - 4, var4 - 3, var3 - 3, var4 + var6 + 3, -267386864, -267386864);
            this.fillGradient(param0, var3 + var0 + 3, var4 - 3, var3 + var0 + 4, var4 + var6 + 3, -267386864, -267386864);
            int var8 = 1347420415;
            int var9 = 1344798847;
            this.fillGradient(param0, var3 - 3, var4 - 3 + 1, var3 - 3 + 1, var4 + var6 + 3 - 1, 1347420415, 1344798847);
            this.fillGradient(param0, var3 + var0 + 2, var4 - 3 + 1, var3 + var0 + 3, var4 + var6 + 3 - 1, 1347420415, 1344798847);
            this.fillGradient(param0, var3 - 3, var4 - 3, var3 + var0 + 3, var4 - 3 + 1, 1347420415, 1347420415);
            this.fillGradient(param0, var3 - 3, var4 + var6 + 2, var3 + var0 + 3, var4 + var6 + 3, 1344798847, 1344798847);
            MultiBufferSource.BufferSource var10 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            param0.translate(0.0, 0.0, (double)this.itemRenderer.blitOffset);
            Matrix4f var11 = param0.last().pose();

            for(int var12 = 0; var12 < param1.size(); ++var12) {
                Component var13 = param1.get(var12);
                if (var13 != null) {
                    this.font.drawInBatch(var13, (float)var3, (float)var4, -1, true, var11, var10, false, 0, 15728880);
                }

                if (var12 == 0) {
                    var4 += 2;
                }

                var4 += 10;
            }

            var10.endBatch();
            this.setBlitOffset(0);
            this.itemRenderer.blitOffset = 0.0F;
            RenderSystem.enableDepthTest();
            RenderSystem.enableRescaleNormal();
        }
    }

    protected void renderComponentHoverEffect(PoseStack param0, @Nullable Component param1, int param2, int param3) {
        if (param1 != null && param1.getStyle().getHoverEvent() != null) {
            HoverEvent var0 = param1.getStyle().getHoverEvent();
            HoverEvent.ItemStackInfo var1 = var0.getValue(HoverEvent.Action.SHOW_ITEM);
            if (var1 != null) {
                this.renderTooltip(param0, var1.getItemStack(), param2, param3);
            } else {
                HoverEvent.EntityTooltipInfo var2 = var0.getValue(HoverEvent.Action.SHOW_ENTITY);
                if (var2 != null) {
                    if (this.minecraft.options.advancedItemTooltips) {
                        this.renderTooltip(param0, var2.getTooltipLines(), param2, param3);
                    }
                } else {
                    Component var3 = var0.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (var3 != null) {
                        this.renderTooltip(param0, this.minecraft.font.split(var3, Math.max(this.width / 2, 200)), param2, param3);
                    }
                }
            }

        }
    }

    protected void insertText(String param0, boolean param1) {
    }

    public boolean handleComponentClicked(Component param0) {
        if (param0 == null) {
            return false;
        } else {
            ClickEvent var0 = param0.getStyle().getClickEvent();
            if (hasShiftDown()) {
                if (param0.getStyle().getInsertion() != null) {
                    this.insertText(param0.getStyle().getInsertion(), false);
                }
            } else if (var0 != null) {
                if (var0.getAction() == ClickEvent.Action.OPEN_URL) {
                    if (!this.minecraft.options.chatLinks) {
                        return false;
                    }

                    try {
                        URI var1 = new URI(var0.getValue());
                        String var2 = var1.getScheme();
                        if (var2 == null) {
                            throw new URISyntaxException(var0.getValue(), "Missing protocol");
                        }

                        if (!ALLOWED_PROTOCOLS.contains(var2.toLowerCase(Locale.ROOT))) {
                            throw new URISyntaxException(var0.getValue(), "Unsupported protocol: " + var2.toLowerCase(Locale.ROOT));
                        }

                        if (this.minecraft.options.chatLinksPrompt) {
                            this.clickedLink = var1;
                            this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, var0.getValue(), false));
                        } else {
                            this.openLink(var1);
                        }
                    } catch (URISyntaxException var5) {
                        LOGGER.error("Can't open url for {}", var0, var5);
                    }
                } else if (var0.getAction() == ClickEvent.Action.OPEN_FILE) {
                    URI var4 = new File(var0.getValue()).toURI();
                    this.openLink(var4);
                } else if (var0.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
                    this.insertText(var0.getValue(), true);
                } else if (var0.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    this.sendMessage(var0.getValue(), false);
                } else if (var0.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
                    this.minecraft.keyboardHandler.setClipboard(var0.getValue());
                } else {
                    LOGGER.error("Don't know how to handle {}", var0);
                }

                return true;
            }

            return false;
        }
    }

    public void sendMessage(String param0) {
        this.sendMessage(param0, true);
    }

    public void sendMessage(String param0, boolean param1) {
        if (param1) {
            this.minecraft.gui.getChat().addRecentChat(param0);
        }

        this.minecraft.player.chat(param0);
    }

    public void init(Minecraft param0, int param1, int param2) {
        this.minecraft = param0;
        this.itemRenderer = param0.getItemRenderer();
        this.font = param0.font;
        this.width = param1;
        this.height = param2;
        this.buttons.clear();
        this.children.clear();
        this.setFocused(null);
        this.init();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    protected void init() {
    }

    @Override
    public void tick() {
    }

    public void removed() {
    }

    public void renderBackground(PoseStack param0) {
        this.renderBackground(param0, 0);
    }

    public void renderBackground(PoseStack param0, int param1) {
        if (this.minecraft.level != null) {
            this.fillGradient(param0, 0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderDirtBackground(param1);
        }

    }

    public void renderDirtBackground(int param0) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        this.minecraft.getTextureManager().bind(BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var2 = 32.0F;
        var1.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var1.vertex(0.0, (double)this.height, 0.0).uv(0.0F, (float)this.height / 32.0F + (float)param0).color(64, 64, 64, 255).endVertex();
        var1.vertex((double)this.width, (double)this.height, 0.0)
            .uv((float)this.width / 32.0F, (float)this.height / 32.0F + (float)param0)
            .color(64, 64, 64, 255)
            .endVertex();
        var1.vertex((double)this.width, 0.0, 0.0).uv((float)this.width / 32.0F, (float)param0).color(64, 64, 64, 255).endVertex();
        var1.vertex(0.0, 0.0, 0.0).uv(0.0F, (float)param0).color(64, 64, 64, 255).endVertex();
        var0.end();
    }

    public boolean isPauseScreen() {
        return true;
    }

    private void confirmLink(boolean param0x) {
        if (param0x) {
            this.openLink(this.clickedLink);
        }

        this.clickedLink = null;
        this.minecraft.setScreen(this);
    }

    private void openLink(URI param0) {
        Util.getPlatform().openUri(param0);
    }

    public static boolean hasControlDown() {
        if (Minecraft.ON_OSX) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
        } else {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
        }
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
            || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342)
            || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
    }

    public static boolean isCut(int param0) {
        return param0 == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public static boolean isPaste(int param0) {
        return param0 == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public static boolean isCopy(int param0) {
        return param0 == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public static boolean isSelectAll(int param0) {
        return param0 == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public void resize(Minecraft param0, int param1, int param2) {
        this.init(param0, param1, param2);
    }

    public static void wrapScreenError(Runnable param0, String param1, String param2) {
        try {
            param0.run();
        } catch (Throwable var6) {
            CrashReport var1 = CrashReport.forThrowable(var6, param1);
            CrashReportCategory var2 = var1.addCategory("Affected screen");
            var2.setDetail("Screen name", () -> param2);
            throw new ReportedException(var1);
        }
    }

    protected boolean isValidCharacterForName(String param0, char param1, int param2) {
        int var0 = param0.indexOf(58);
        int var1 = param0.indexOf(47);
        if (param1 == ':') {
            return (var1 == -1 || param2 <= var1) && var0 == -1;
        } else if (param1 == '/') {
            return param2 > var0;
        } else {
            return param1 == '_' || param1 == '-' || param1 >= 'a' && param1 <= 'z' || param1 >= '0' && param1 <= '9' || param1 == '.';
        }
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return true;
    }
}
