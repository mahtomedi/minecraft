package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.realmsclient.util.task.LongRunningTask;
import com.mojang.realmsclient.util.task.ResettingGeneratedWorldTask;
import com.mojang.realmsclient.util.task.ResettingTemplateWorldTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreen {
    static final Logger LOGGER = LogManager.getLogger();
    private final Screen lastScreen;
    private final RealmsServer serverData;
    private RealmsLabel titleLabel;
    private RealmsLabel subtitleLabel;
    private Component title = new TranslatableComponent("mco.reset.world.title");
    private Component subtitle = new TranslatableComponent("mco.reset.world.warning");
    private Component buttonTitle = CommonComponents.GUI_CANCEL;
    private int subtitleColor = 16711680;
    private static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    private static final ResourceLocation UPLOAD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/upload.png");
    private static final ResourceLocation ADVENTURE_MAP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/adventure.png");
    private static final ResourceLocation SURVIVAL_SPAWN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/survival_spawn.png");
    private static final ResourceLocation NEW_WORLD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/new_world.png");
    private static final ResourceLocation EXPERIENCE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/experience.png");
    private static final ResourceLocation INSPIRATION_LOCATION = new ResourceLocation("realms", "textures/gui/realms/inspiration.png");
    WorldTemplatePaginatedList templates;
    WorldTemplatePaginatedList adventuremaps;
    WorldTemplatePaginatedList experiences;
    WorldTemplatePaginatedList inspirations;
    public int slot = -1;
    private Component resetTitle = new TranslatableComponent("mco.reset.world.resetting.screen.title");
    private final Runnable resetWorldRunnable;
    private final Runnable callback;

    public RealmsResetWorldScreen(Screen param0, RealmsServer param1, Runnable param2, Runnable param3) {
        this.lastScreen = param0;
        this.serverData = param1;
        this.resetWorldRunnable = param2;
        this.callback = param3;
    }

    public RealmsResetWorldScreen(
        Screen param0, RealmsServer param1, Component param2, Component param3, int param4, Component param5, Runnable param6, Runnable param7
    ) {
        this(param0, param1, param6, param7);
        this.title = param2;
        this.subtitle = param3;
        this.subtitleColor = param4;
        this.buttonTitle = param5;
    }

    public void setSlot(int param0) {
        this.slot = param0;
    }

    public void setResetTitle(Component param0) {
        this.resetTitle = param0;
    }

    @Override
    public void init() {
        this.addButton(new Button(this.width / 2 - 40, row(14) - 10, 80, 20, this.buttonTitle, param0 -> this.minecraft.setScreen(this.lastScreen)));
        (new Thread("Realms-reset-world-fetcher") {
            @Override
            public void run() {
                RealmsClient var0 = RealmsClient.create();

                try {
                    WorldTemplatePaginatedList var1 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
                    WorldTemplatePaginatedList var2 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
                    WorldTemplatePaginatedList var3 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
                    WorldTemplatePaginatedList var4 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
                    RealmsResetWorldScreen.this.minecraft.execute(() -> {
                        RealmsResetWorldScreen.this.templates = var1;
                        RealmsResetWorldScreen.this.adventuremaps = var2;
                        RealmsResetWorldScreen.this.experiences = var3;
                        RealmsResetWorldScreen.this.inspirations = var4;
                    });
                } catch (RealmsServiceException var6) {
                    RealmsResetWorldScreen.LOGGER.error("Couldn't fetch templates in reset world", (Throwable)var6);
                }

            }
        }).start();
        this.titleLabel = this.addWidget(new RealmsLabel(this.title, this.width / 2, 7, 16777215));
        this.subtitleLabel = this.addWidget(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
        this.addButton(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(1),
                row(0) + 10,
                new TranslatableComponent("mco.reset.world.generate"),
                NEW_WORLD_LOCATION,
                param0 -> this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this::generationSelectionCallback, this.title))
            )
        );
        this.addButton(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(2),
                row(0) + 10,
                new TranslatableComponent("mco.reset.world.upload"),
                UPLOAD_LOCATION,
                param0 -> {
                    Screen var0 = new RealmsSelectFileToUploadScreen(
                        this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.callback
                    );
                    this.minecraft.setScreen(var0);
                }
            )
        );
        this.addButton(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(3),
                row(0) + 10,
                new TranslatableComponent("mco.reset.world.template"),
                SURVIVAL_SPAWN_LOCATION,
                param0 -> {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        this::templateSelectionCallback, RealmsServer.WorldType.NORMAL, this.templates
                    );
                    var0.setTitle(new TranslatableComponent("mco.reset.world.template"));
                    this.minecraft.setScreen(var0);
                }
            )
        );
        this.addButton(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(1),
                row(6) + 20,
                new TranslatableComponent("mco.reset.world.adventure"),
                ADVENTURE_MAP_LOCATION,
                param0 -> {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        this::templateSelectionCallback, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps
                    );
                    var0.setTitle(new TranslatableComponent("mco.reset.world.adventure"));
                    this.minecraft.setScreen(var0);
                }
            )
        );
        this.addButton(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(2),
                row(6) + 20,
                new TranslatableComponent("mco.reset.world.experience"),
                EXPERIENCE_LOCATION,
                param0 -> {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        this::templateSelectionCallback, RealmsServer.WorldType.EXPERIENCE, this.experiences
                    );
                    var0.setTitle(new TranslatableComponent("mco.reset.world.experience"));
                    this.minecraft.setScreen(var0);
                }
            )
        );
        this.addButton(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(3),
                row(6) + 20,
                new TranslatableComponent("mco.reset.world.inspiration"),
                INSPIRATION_LOCATION,
                param0 -> {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        this::templateSelectionCallback, RealmsServer.WorldType.INSPIRATION, this.inspirations
                    );
                    var0.setTitle(new TranslatableComponent("mco.reset.world.inspiration"));
                    this.minecraft.setScreen(var0);
                }
            )
        );
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private int frame(int param0) {
        return this.width / 2 - 130 + (param0 - 1) * 100;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.titleLabel.render(this, param0);
        this.subtitleLabel.render(this, param0);
        super.render(param0, param1, param2, param3);
    }

    void drawFrame(PoseStack param0, int param1, int param2, Component param3, ResourceLocation param4, boolean param5, boolean param6) {
        RenderSystem.setShaderTexture(0, param4);
        if (param5) {
            RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GuiComponent.blit(param0, param1 + 2, param2 + 14, 0.0F, 0.0F, 56, 56, 56, 56);
        RenderSystem.setShaderTexture(0, SLOT_FRAME_LOCATION);
        if (param5) {
            RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GuiComponent.blit(param0, param1, param2 + 12, 0.0F, 0.0F, 60, 60, 60, 60);
        int var0 = param5 ? 10526880 : 16777215;
        drawCenteredString(param0, this.font, param3, param1 + 30, param2, var0);
    }

    private void startTask(LongRunningTask param0) {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, param0));
    }

    public void switchSlot(Runnable param0) {
        this.startTask(new SwitchSlotTask(this.serverData.id, this.slot, param0));
    }

    private void templateSelectionCallback(@Nullable WorldTemplate param0) {
        this.minecraft.setScreen(this);
        if (param0 != null) {
            this.resetWorld(() -> this.startTask(new ResettingTemplateWorldTask(param0, this.serverData.id, this.resetTitle, this.resetWorldRunnable)));
        }

    }

    private void generationSelectionCallback(@Nullable WorldGenerationInfo param0) {
        this.minecraft.setScreen(this);
        if (param0 != null) {
            this.resetWorld(() -> this.startTask(new ResettingGeneratedWorldTask(param0, this.serverData.id, this.resetTitle, this.resetWorldRunnable)));
        }

    }

    private void resetWorld(Runnable param0) {
        if (this.slot == -1) {
            param0.run();
        } else {
            this.switchSlot(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    class FrameButton extends Button {
        private final ResourceLocation image;

        public FrameButton(int param0, int param1, Component param2, ResourceLocation param3, Button.OnPress param4) {
            super(param0, param1, 60, 72, param2, param4);
            this.image = param3;
        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            RealmsResetWorldScreen.this.drawFrame(
                param0, this.x, this.y, this.getMessage(), this.image, this.isHovered(), this.isMouseOver((double)param1, (double)param2)
            );
        }
    }
}
