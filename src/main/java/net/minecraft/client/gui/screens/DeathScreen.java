package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeathScreen extends Screen {
    private static final ResourceLocation DRAFT_REPORT_SPRITE = new ResourceLocation("icon/draft_report");
    private int delayTicker;
    private final Component causeOfDeath;
    private final boolean hardcore;
    private Component deathScore;
    private final List<Button> exitButtons = Lists.newArrayList();
    @Nullable
    private Button exitToTitleButton;

    public DeathScreen(@Nullable Component param0, boolean param1) {
        super(Component.translatable(param1 ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.causeOfDeath = param0;
        this.hardcore = param1;
    }

    @Override
    protected void init() {
        this.delayTicker = 0;
        this.exitButtons.clear();
        Component var0 = this.hardcore ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
        this.exitButtons.add(this.addRenderableWidget(Button.builder(var0, param0 -> {
            this.minecraft.player.respawn();
            param0.active = false;
        }).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
        this.exitToTitleButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("deathScreen.titleScreen"),
                    param0 -> this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true)
                )
                .bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20)
                .build()
        );
        this.exitButtons.add(this.exitToTitleButton);
        this.setButtonsActive(false);
        this.deathScore = Component.translatable(
            "deathScreen.score.value", Component.literal(Integer.toString(this.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW)
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void handleExitToTitleScreen() {
        if (this.hardcore) {
            this.exitToTitleScreen();
        } else {
            ConfirmScreen var0 = new DeathScreen.TitleConfirmScreen(
                param0 -> {
                    if (param0) {
                        this.exitToTitleScreen();
                    } else {
                        this.minecraft.player.respawn();
                        this.minecraft.setScreen(null);
                    }
    
                },
                Component.translatable("deathScreen.quit.confirm"),
                CommonComponents.EMPTY,
                Component.translatable("deathScreen.titleScreen"),
                Component.translatable("deathScreen.respawn")
            );
            this.minecraft.setScreen(var0);
            var0.setDelay(20);
        }
    }

    private void exitToTitleScreen() {
        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect();
        }

        this.minecraft.disconnect(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.pose().pushPose();
        param0.pose().scale(2.0F, 2.0F, 2.0F);
        param0.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, 16777215);
        param0.pose().popPose();
        if (this.causeOfDeath != null) {
            param0.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, 85, 16777215);
        }

        param0.drawCenteredString(this.font, this.deathScore, this.width / 2, 100, 16777215);
        if (this.causeOfDeath != null && param2 > 85 && param2 < 85 + 9) {
            Style var0 = this.getClickedComponentStyleAt(param1);
            param0.renderComponentHoverEffect(this.font, var0, param1, param2);
        }

        if (this.exitToTitleButton != null && this.minecraft.getReportingContext().hasDraftReport()) {
            param0.blitSprite(
                DRAFT_REPORT_SPRITE, this.exitToTitleButton.getX() + this.exitToTitleButton.getWidth() - 17, this.exitToTitleButton.getY() + 3, 15, 15
            );
        }

    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        param0.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
    }

    @Nullable
    private Style getClickedComponentStyleAt(int param0) {
        if (this.causeOfDeath == null) {
            return null;
        } else {
            int var0 = this.minecraft.font.width(this.causeOfDeath);
            int var1 = this.width / 2 - var0 / 2;
            int var2 = this.width / 2 + var0 / 2;
            return param0 >= var1 && param0 <= var2 ? this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, param0 - var1) : null;
        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.causeOfDeath != null && param1 > 85.0 && param1 < (double)(85 + 9)) {
            Style var0 = this.getClickedComponentStyleAt((int)param0);
            if (var0 != null && var0.getClickEvent() != null && var0.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                this.handleComponentClicked(var0);
                return false;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.delayTicker;
        if (this.delayTicker == 20) {
            this.setButtonsActive(true);
        }

    }

    private void setButtonsActive(boolean param0) {
        for(Button var0 : this.exitButtons) {
            var0.active = param0;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class TitleConfirmScreen extends ConfirmScreen {
        public TitleConfirmScreen(BooleanConsumer param0, Component param1, Component param2, Component param3, Component param4) {
            super(param0, param1, param2, param3, param4);
        }
    }
}
