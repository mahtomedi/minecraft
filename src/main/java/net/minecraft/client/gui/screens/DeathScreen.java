package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
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
        String var0;
        String var1;
        if (this.hardcore) {
            var0 = I18n.get("deathScreen.spectate");
            var1 = I18n.get("deathScreen." + (this.minecraft.isLocalServer() ? "deleteWorld" : "leaveServer"));
        } else {
            var0 = I18n.get("deathScreen.respawn");
            var1 = I18n.get("deathScreen.titleScreen");
        }

        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72, 200, 20, var0, param0 -> {
            this.minecraft.player.respawn();
            this.minecraft.setScreen(null);
        }));
        Button var4 = this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 4 + 96,
                200,
                20,
                var1,
                param0 -> {
                    if (this.hardcore) {
                        this.minecraft.setScreen(new TitleScreen());
                    } else {
                        ConfirmScreen var0x = new ConfirmScreen(
                            this::confirmResult,
                            new TranslatableComponent("deathScreen.quit.confirm"),
                            new TextComponent(""),
                            I18n.get("deathScreen.titleScreen"),
                            I18n.get("deathScreen.respawn")
                        );
                        this.minecraft.setScreen(var0x);
                        var0x.setDelay(20);
                    }
                }
            )
        );
        if (!this.hardcore && this.minecraft.getUser() == null) {
            var4.active = false;
        }

        for(AbstractWidget var5 : this.buttons) {
            var5.active = false;
        }

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void confirmResult(boolean param0) {
        if (param0) {
            if (this.minecraft.level != null) {
                this.minecraft.level.disconnect();
            }

            this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
            this.minecraft.setScreen(new TitleScreen());
        } else {
            this.minecraft.player.respawn();
            this.minecraft.setScreen(null);
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(2.0F, 2.0F, 2.0F);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();
        if (this.causeOfDeath != null) {
            this.drawCenteredString(this.font, this.causeOfDeath.getColoredString(), this.width / 2, 85, 16777215);
        }

        this.drawCenteredString(
            this.font, I18n.get("deathScreen.score") + ": " + ChatFormatting.YELLOW + this.minecraft.player.getScore(), this.width / 2, 100, 16777215
        );
        if (this.causeOfDeath != null && param1 > 85 && param1 < 85 + 9) {
            Component var0 = this.getClickedComponentAt(param0);
            if (var0 != null && var0.getStyle().getHoverEvent() != null) {
                this.renderComponentHoverEffect(var0, param0, param1);
            }
        }

        super.render(param0, param1, param2);
    }

    @Nullable
    public Component getClickedComponentAt(int param0) {
        if (this.causeOfDeath == null) {
            return null;
        } else {
            int var0 = this.minecraft.font.width(this.causeOfDeath.getColoredString());
            int var1 = this.width / 2 - var0 / 2;
            int var2 = this.width / 2 + var0 / 2;
            int var3 = var1;
            if (param0 >= var1 && param0 <= var2) {
                for(Component var4 : this.causeOfDeath) {
                    var3 += this.minecraft.font.width(ComponentRenderUtils.stripColor(var4.getContents(), false));
                    if (var3 > param0) {
                        return var4;
                    }
                }

                return null;
            } else {
                return null;
            }
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
