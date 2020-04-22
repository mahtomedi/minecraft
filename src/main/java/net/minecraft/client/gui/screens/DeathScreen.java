package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeathScreen extends Screen {
    private int delayTicker;
    private final Component causeOfDeath;
    private final boolean hardcore;

    public DeathScreen(@Nullable Component param0, boolean param1) {
        super(new TranslatableComponent(param1 ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.causeOfDeath = param0;
        this.hardcore = param1;
    }

    @Override
    protected void init() {
        this.delayTicker = 0;
        this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 4 + 72,
                200,
                20,
                this.hardcore ? new TranslatableComponent("deathScreen.spectate") : new TranslatableComponent("deathScreen.respawn"),
                param0 -> {
                    this.minecraft.player.respawn();
                    this.minecraft.setScreen(null);
                }
            )
        );
        Button var0 = this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 4 + 96,
                200,
                20,
                new TranslatableComponent("deathScreen.titleScreen"),
                param0 -> {
                    if (this.hardcore) {
                        this.exitToTitleScreen();
                    } else {
                        ConfirmScreen var0x = new ConfirmScreen(
                            this::confirmResult,
                            new TranslatableComponent("deathScreen.quit.confirm"),
                            TextComponent.EMPTY,
                            new TranslatableComponent("deathScreen.titleScreen"),
                            new TranslatableComponent("deathScreen.respawn")
                        );
                        this.minecraft.setScreen(var0x);
                        var0x.setDelay(20);
                    }
                }
            )
        );
        if (!this.hardcore && this.minecraft.getUser() == null) {
            var0.active = false;
        }

        for(AbstractWidget var1 : this.buttons) {
            var1.active = false;
        }

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void confirmResult(boolean param0) {
        if (param0) {
            this.exitToTitleScreen();
        } else {
            this.minecraft.player.respawn();
            this.minecraft.setScreen(null);
        }

    }

    private void exitToTitleScreen() {
        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect();
        }

        this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.fillGradient(param0, 0, 0, this.width, this.height, 1615855616, -1602211792);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(2.0F, 2.0F, 2.0F);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2 / 2, 30, 16777215);
        RenderSystem.popMatrix();
        if (this.causeOfDeath != null) {
            this.drawCenteredString(param0, this.font, this.causeOfDeath, this.width / 2, 85, 16777215);
        }

        this.drawCenteredString(
            param0, this.font, I18n.get("deathScreen.score") + ": " + ChatFormatting.YELLOW + this.minecraft.player.getScore(), this.width / 2, 100, 16777215
        );
        if (this.causeOfDeath != null && param2 > 85 && param2 < 85 + 9) {
            Component var0 = this.getClickedComponentAt(param1);
            this.renderComponentHoverEffect(param0, var0, param1, param2);
        }

        super.render(param0, param1, param2, param3);
    }

    @Nullable
    public Component getClickedComponentAt(int param0) {
        if (this.causeOfDeath == null) {
            return null;
        } else {
            int var0 = this.minecraft.font.width(this.causeOfDeath);
            int var1 = this.width / 2 - var0 / 2;
            int var2 = this.width / 2 + var0 / 2;
            return param0 >= var1 && param0 <= var2 ? this.minecraft.font.getSplitter().componentAtWidth(this.causeOfDeath, param0 - var1) : null;
        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.causeOfDeath != null && param1 > 85.0 && param1 < (double)(85 + 9)) {
            Component var0 = this.getClickedComponentAt((int)param0);
            if (var0 != null && var0.getStyle().getClickEvent() != null && var0.getStyle().getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
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
            for(AbstractWidget var0 : this.buttons) {
                var0.active = true;
            }
        }

    }
}
