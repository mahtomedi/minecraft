package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
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
import com.mojang.realmsclient.util.task.WorldCreationTask;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation SLOT_FRAME_SPRITE = new ResourceLocation("widget/slot_frame");
    private static final Component CREATE_REALM_TITLE = Component.translatable("mco.selectServer.create");
    private static final Component CREATE_REALM_SUBTITLE = Component.translatable("mco.selectServer.create.subtitle");
    private static final Component CREATE_WORLD_TITLE = Component.translatable("mco.configure.world.switch.slot");
    private static final Component CREATE_WORLD_SUBTITLE = Component.translatable("mco.configure.world.switch.slot.subtitle");
    private static final Component RESET_WORLD_TITLE = Component.translatable("mco.reset.world.title");
    private static final Component RESET_WORLD_SUBTITLE = Component.translatable("mco.reset.world.warning");
    public static final Component CREATE_WORLD_RESET_TASK_TITLE = Component.translatable("mco.create.world.reset.title");
    private static final Component RESET_WORLD_RESET_TASK_TITLE = Component.translatable("mco.reset.world.resetting.screen.title");
    private static final Component WORLD_TEMPLATES_TITLE = Component.translatable("mco.reset.world.template");
    private static final Component ADVENTURES_TITLE = Component.translatable("mco.reset.world.adventure");
    private static final Component EXPERIENCES_TITLE = Component.translatable("mco.reset.world.experience");
    private static final Component INSPIRATION_TITLE = Component.translatable("mco.reset.world.inspiration");
    private final Screen lastScreen;
    private final RealmsServer serverData;
    private final Component subtitle;
    private final int subtitleColor;
    private final Component resetTaskTitle;
    private static final ResourceLocation UPLOAD_LOCATION = new ResourceLocation("textures/gui/realms/upload.png");
    private static final ResourceLocation ADVENTURE_MAP_LOCATION = new ResourceLocation("textures/gui/realms/adventure.png");
    private static final ResourceLocation SURVIVAL_SPAWN_LOCATION = new ResourceLocation("textures/gui/realms/survival_spawn.png");
    private static final ResourceLocation NEW_WORLD_LOCATION = new ResourceLocation("textures/gui/realms/new_world.png");
    private static final ResourceLocation EXPERIENCE_LOCATION = new ResourceLocation("textures/gui/realms/experience.png");
    private static final ResourceLocation INSPIRATION_LOCATION = new ResourceLocation("textures/gui/realms/inspiration.png");
    WorldTemplatePaginatedList templates;
    WorldTemplatePaginatedList adventuremaps;
    WorldTemplatePaginatedList experiences;
    WorldTemplatePaginatedList inspirations;
    public final int slot;
    @Nullable
    private final WorldCreationTask worldCreationTask;
    private final Runnable resetWorldRunnable;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private RealmsResetWorldScreen(
        Screen param0, RealmsServer param1, int param2, Component param3, Component param4, int param5, Component param6, Runnable param7
    ) {
        this(param0, param1, param2, param3, param4, param5, param6, null, param7);
    }

    public RealmsResetWorldScreen(
        Screen param0,
        RealmsServer param1,
        int param2,
        Component param3,
        Component param4,
        int param5,
        Component param6,
        @Nullable WorldCreationTask param7,
        Runnable param8
    ) {
        super(param3);
        this.lastScreen = param0;
        this.serverData = param1;
        this.slot = param2;
        this.subtitle = param4;
        this.subtitleColor = param5;
        this.resetTaskTitle = param6;
        this.worldCreationTask = param7;
        this.resetWorldRunnable = param8;
    }

    public static RealmsResetWorldScreen forNewRealm(Screen param0, RealmsServer param1, WorldCreationTask param2, Runnable param3) {
        return new RealmsResetWorldScreen(
            param0, param1, param1.activeSlot, CREATE_REALM_TITLE, CREATE_REALM_SUBTITLE, -6250336, CREATE_WORLD_RESET_TASK_TITLE, param2, param3
        );
    }

    public static RealmsResetWorldScreen forEmptySlot(Screen param0, int param1, RealmsServer param2, Runnable param3) {
        return new RealmsResetWorldScreen(param0, param2, param1, CREATE_WORLD_TITLE, CREATE_WORLD_SUBTITLE, -6250336, CREATE_WORLD_RESET_TASK_TITLE, param3);
    }

    public static RealmsResetWorldScreen forResetSlot(Screen param0, RealmsServer param1, Runnable param2) {
        return new RealmsResetWorldScreen(
            param0, param1, param1.activeSlot, RESET_WORLD_TITLE, RESET_WORLD_SUBTITLE, -65536, RESET_WORLD_RESET_TASK_TITLE, param2
        );
    }

    @Override
    public void init() {
        LinearLayout var0 = LinearLayout.vertical();
        var0.addChild(new StringWidget(this.title, this.font), LayoutSettings::alignHorizontallyCenter);
        var0.addChild(SpacerElement.height(3));
        var0.addChild(new StringWidget(this.subtitle, this.font).setColor(this.subtitleColor), LayoutSettings::alignHorizontallyCenter);
        this.layout.addToHeader(var0);
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
        this.addRenderableWidget(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(1),
                row(0) + 10,
                RealmsResetNormalWorldScreen.TITLE,
                NEW_WORLD_LOCATION,
                param0 -> this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this::generationSelectionCallback, this.title))
            )
        );
        this.addRenderableWidget(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(2),
                row(0) + 10,
                RealmsSelectFileToUploadScreen.TITLE,
                UPLOAD_LOCATION,
                param0 -> this.minecraft.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot, this))
            )
        );
        this.addRenderableWidget(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(3),
                row(0) + 10,
                WORLD_TEMPLATES_TITLE,
                SURVIVAL_SPAWN_LOCATION,
                param0 -> this.minecraft
                        .setScreen(
                            new RealmsSelectWorldTemplateScreen(
                                WORLD_TEMPLATES_TITLE, this::templateSelectionCallback, RealmsServer.WorldType.NORMAL, this.templates
                            )
                        )
            )
        );
        this.addRenderableWidget(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(1),
                row(6) + 20,
                ADVENTURES_TITLE,
                ADVENTURE_MAP_LOCATION,
                param0 -> this.minecraft
                        .setScreen(
                            new RealmsSelectWorldTemplateScreen(
                                ADVENTURES_TITLE, this::templateSelectionCallback, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps
                            )
                        )
            )
        );
        this.addRenderableWidget(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(2),
                row(6) + 20,
                EXPERIENCES_TITLE,
                EXPERIENCE_LOCATION,
                param0 -> this.minecraft
                        .setScreen(
                            new RealmsSelectWorldTemplateScreen(
                                EXPERIENCES_TITLE, this::templateSelectionCallback, RealmsServer.WorldType.EXPERIENCE, this.experiences
                            )
                        )
            )
        );
        this.addRenderableWidget(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(3),
                row(6) + 20,
                INSPIRATION_TITLE,
                INSPIRATION_LOCATION,
                param0 -> this.minecraft
                        .setScreen(
                            new RealmsSelectWorldTemplateScreen(
                                INSPIRATION_TITLE, this::templateSelectionCallback, RealmsServer.WorldType.INSPIRATION, this.inspirations
                            )
                        )
            )
        );
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.layout.arrangeElements();
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.subtitle);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private int frame(int param0) {
        return this.width / 2 - 130 + (param0 - 1) * 100;
    }

    private void templateSelectionCallback(@Nullable WorldTemplate param0) {
        this.minecraft.setScreen(this);
        if (param0 != null) {
            this.runResetTasks(new ResettingTemplateWorldTask(param0, this.serverData.id, this.resetTaskTitle, this.resetWorldRunnable));
        }

    }

    private void generationSelectionCallback(@Nullable WorldGenerationInfo param0) {
        this.minecraft.setScreen(this);
        if (param0 != null) {
            this.runResetTasks(new ResettingGeneratedWorldTask(param0, this.serverData.id, this.resetTaskTitle, this.resetWorldRunnable));
        }

    }

    private void runResetTasks(LongRunningTask param0) {
        List<LongRunningTask> var0 = new ArrayList<>();
        if (this.worldCreationTask != null) {
            var0.add(this.worldCreationTask);
        }

        if (this.slot != this.serverData.activeSlot) {
            var0.add(new SwitchSlotTask(this.serverData.id, this.slot, () -> {
            }));
        }

        var0.add(param0);
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, var0.toArray(new LongRunningTask[0])));
    }

    public void switchSlot(Runnable param0) {
        this.minecraft
            .setScreen(
                new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(this.serverData.id, this.slot, () -> this.minecraft.execute(param0)))
            );
    }

    @OnlyIn(Dist.CLIENT)
    class FrameButton extends Button {
        private static final int WIDTH = 60;
        private static final int HEIGHT = 72;
        private static final int IMAGE_SIZE = 56;
        private final ResourceLocation image;

        FrameButton(int param0, int param1, Component param2, ResourceLocation param3, Button.OnPress param4) {
            super(param0, param1, 60, 72, param2, param4, DEFAULT_NARRATION);
            this.image = param3;
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            boolean var0 = this.isHoveredOrFocused();
            if (var0) {
                param0.setColor(0.56F, 0.56F, 0.56F, 1.0F);
            }

            int var1 = this.getX();
            int var2 = this.getY();
            param0.blit(this.image, var1 + 2, var2 + 14, 0.0F, 0.0F, 56, 56, 56, 56);
            param0.blitSprite(RealmsResetWorldScreen.SLOT_FRAME_SPRITE, var1, var2 + 12, 60, 60);
            param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            int var3 = var0 ? -6250336 : -1;
            param0.drawCenteredString(RealmsResetWorldScreen.this.font, this.getMessage(), var1 + 30, var2, var3);
        }
    }
}
