package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreenWithCallback<WorldTemplate> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final RealmsServer serverData;
    private final RealmsScreen returnScreen;
    private RealmsLabel titleLabel;
    private RealmsLabel subtitleLabel;
    private String title = getLocalizedString("mco.reset.world.title");
    private String subtitle = getLocalizedString("mco.reset.world.warning");
    private String buttonTitle = getLocalizedString("gui.cancel");
    private int subtitleColor = 16711680;
    private final int BUTTON_CANCEL_ID = 0;
    private final int BUTTON_FRAME_START = 100;
    private WorldTemplatePaginatedList templates;
    private WorldTemplatePaginatedList adventuremaps;
    private WorldTemplatePaginatedList experiences;
    private WorldTemplatePaginatedList inspirations;
    public int slot = -1;
    private RealmsResetWorldScreen.ResetType typeToReset = RealmsResetWorldScreen.ResetType.NONE;
    private RealmsResetWorldScreen.ResetWorldInfo worldInfoToReset;
    private WorldTemplate worldTemplateToReset;
    private String resetTitle;
    private int confirmationId = -1;

    public RealmsResetWorldScreen(RealmsScreen param0, RealmsServer param1, RealmsScreen param2) {
        this.lastScreen = param0;
        this.serverData = param1;
        this.returnScreen = param2;
    }

    public RealmsResetWorldScreen(RealmsScreen param0, RealmsServer param1, RealmsScreen param2, String param3, String param4, int param5, String param6) {
        this(param0, param1, param2);
        this.title = param3;
        this.subtitle = param4;
        this.subtitleColor = param5;
        this.buttonTitle = param6;
    }

    public void setConfirmationId(int param0) {
        this.confirmationId = param0;
    }

    public void setSlot(int param0) {
        this.slot = param0;
    }

    public void setResetTitle(String param0) {
        this.resetTitle = param0;
    }

    @Override
    public void init() {
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 40, RealmsConstants.row(14) - 10, 80, 20, this.buttonTitle) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsResetWorldScreen.this.lastScreen);
            }
        });
        (new Thread("Realms-reset-world-fetcher") {
            @Override
            public void run() {
                RealmsClient var0 = RealmsClient.createRealmsClient();

                try {
                    WorldTemplatePaginatedList var1 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
                    WorldTemplatePaginatedList var2 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
                    WorldTemplatePaginatedList var3 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
                    WorldTemplatePaginatedList var4 = var0.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
                    Realms.execute(() -> {
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
        this.addWidget(this.titleLabel = new RealmsLabel(this.title, this.width() / 2, 7, 16777215));
        this.addWidget(this.subtitleLabel = new RealmsLabel(this.subtitle, this.width() / 2, 22, this.subtitleColor));
        this.buttonsAdd(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(1),
                RealmsConstants.row(0) + 10,
                getLocalizedString("mco.reset.world.generate"),
                -1L,
                "realms:textures/gui/realms/new_world.png",
                RealmsResetWorldScreen.ResetType.GENERATE
            ) {
                @Override
                public void onPress() {
                    Realms.setScreen(new RealmsResetNormalWorldScreen(RealmsResetWorldScreen.this, RealmsResetWorldScreen.this.title));
                }
            }
        );
        this.buttonsAdd(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(2),
                RealmsConstants.row(0) + 10,
                getLocalizedString("mco.reset.world.upload"),
                -1L,
                "realms:textures/gui/realms/upload.png",
                RealmsResetWorldScreen.ResetType.UPLOAD
            ) {
                @Override
                public void onPress() {
                    Realms.setScreen(
                        new RealmsSelectFileToUploadScreen(
                            RealmsResetWorldScreen.this.serverData.id,
                            RealmsResetWorldScreen.this.slot != -1 ? RealmsResetWorldScreen.this.slot : RealmsResetWorldScreen.this.serverData.activeSlot,
                            RealmsResetWorldScreen.this
                        )
                    );
                }
            }
        );
        this.buttonsAdd(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(3),
                RealmsConstants.row(0) + 10,
                getLocalizedString("mco.reset.world.template"),
                -1L,
                "realms:textures/gui/realms/survival_spawn.png",
                RealmsResetWorldScreen.ResetType.SURVIVAL_SPAWN
            ) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        RealmsResetWorldScreen.this, RealmsServer.WorldType.NORMAL, RealmsResetWorldScreen.this.templates
                    );
                    var0.setTitle(RealmsScreen.getLocalizedString("mco.reset.world.template"));
                    Realms.setScreen(var0);
                }
            }
        );
        this.buttonsAdd(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(1),
                RealmsConstants.row(6) + 20,
                getLocalizedString("mco.reset.world.adventure"),
                -1L,
                "realms:textures/gui/realms/adventure.png",
                RealmsResetWorldScreen.ResetType.ADVENTURE
            ) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        RealmsResetWorldScreen.this, RealmsServer.WorldType.ADVENTUREMAP, RealmsResetWorldScreen.this.adventuremaps
                    );
                    var0.setTitle(RealmsScreen.getLocalizedString("mco.reset.world.adventure"));
                    Realms.setScreen(var0);
                }
            }
        );
        this.buttonsAdd(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(2),
                RealmsConstants.row(6) + 20,
                getLocalizedString("mco.reset.world.experience"),
                -1L,
                "realms:textures/gui/realms/experience.png",
                RealmsResetWorldScreen.ResetType.EXPERIENCE
            ) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        RealmsResetWorldScreen.this, RealmsServer.WorldType.EXPERIENCE, RealmsResetWorldScreen.this.experiences
                    );
                    var0.setTitle(RealmsScreen.getLocalizedString("mco.reset.world.experience"));
                    Realms.setScreen(var0);
                }
            }
        );
        this.buttonsAdd(
            new RealmsResetWorldScreen.FrameButton(
                this.frame(3),
                RealmsConstants.row(6) + 20,
                getLocalizedString("mco.reset.world.inspiration"),
                -1L,
                "realms:textures/gui/realms/inspiration.png",
                RealmsResetWorldScreen.ResetType.INSPIRATION
            ) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
                        RealmsResetWorldScreen.this, RealmsServer.WorldType.INSPIRATION, RealmsResetWorldScreen.this.inspirations
                    );
                    var0.setTitle(RealmsScreen.getLocalizedString("mco.reset.world.inspiration"));
                    Realms.setScreen(var0);
                }
            }
        );
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return super.mouseClicked(param0, param1, param2);
    }

    private int frame(int param0) {
        return this.width() / 2 - 130 + (param0 - 1) * 100;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.titleLabel.render(this);
        this.subtitleLabel.render(this);
        super.render(param0, param1, param2);
    }

    private void drawFrame(
        int param0, int param1, String param2, long param3, String param4, RealmsResetWorldScreen.ResetType param5, boolean param6, boolean param7
    ) {
        if (param3 == -1L) {
            bind(param4);
        } else {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param3), param4);
        }

        if (param6) {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        } else {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        RealmsScreen.blit(param0 + 2, param1 + 14, 0.0F, 0.0F, 56, 56, 56, 56);
        bind("realms:textures/gui/realms/slot_frame.png");
        if (param6) {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        } else {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        RealmsScreen.blit(param0, param1 + 12, 0.0F, 0.0F, 60, 60, 60, 60);
        this.drawCenteredString(param2, param0 + 30, param1, param6 ? 10526880 : 16777215);
    }

    void callback(WorldTemplate param0) {
        if (param0 != null) {
            if (this.slot == -1) {
                this.resetWorldWithTemplate(param0);
            } else {
                switch(param0.type) {
                    case WORLD_TEMPLATE:
                        this.typeToReset = RealmsResetWorldScreen.ResetType.SURVIVAL_SPAWN;
                        break;
                    case ADVENTUREMAP:
                        this.typeToReset = RealmsResetWorldScreen.ResetType.ADVENTURE;
                        break;
                    case EXPERIENCE:
                        this.typeToReset = RealmsResetWorldScreen.ResetType.EXPERIENCE;
                        break;
                    case INSPIRATION:
                        this.typeToReset = RealmsResetWorldScreen.ResetType.INSPIRATION;
                }

                this.worldTemplateToReset = param0;
                this.switchSlot();
            }
        }

    }

    private void switchSlot() {
        this.switchSlot(this);
    }

    public void switchSlot(RealmsScreen param0) {
        RealmsTasks.SwitchSlotTask var0 = new RealmsTasks.SwitchSlotTask(this.serverData.id, this.slot, param0, 100);
        RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(this.lastScreen, var0);
        var1.start();
        Realms.setScreen(var1);
    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        if (param1 == 100 && param0) {
            switch(this.typeToReset) {
                case ADVENTURE:
                case SURVIVAL_SPAWN:
                case EXPERIENCE:
                case INSPIRATION:
                    if (this.worldTemplateToReset != null) {
                        this.resetWorldWithTemplate(this.worldTemplateToReset);
                    }
                    break;
                case GENERATE:
                    if (this.worldInfoToReset != null) {
                        this.triggerResetWorld(this.worldInfoToReset);
                    }
                    break;
                default:
                    return;
            }

        } else {
            if (param0) {
                Realms.setScreen(this.returnScreen);
                if (this.confirmationId != -1) {
                    this.returnScreen.confirmResult(true, this.confirmationId);
                }
            }

        }
    }

    public void resetWorldWithTemplate(WorldTemplate param0) {
        RealmsTasks.ResettingWorldTask var0 = new RealmsTasks.ResettingWorldTask(this.serverData.id, this.returnScreen, param0);
        if (this.resetTitle != null) {
            var0.setResetTitle(this.resetTitle);
        }

        if (this.confirmationId != -1) {
            var0.setConfirmationId(this.confirmationId);
        }

        RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(this.lastScreen, var0);
        var1.start();
        Realms.setScreen(var1);
    }

    public void resetWorld(RealmsResetWorldScreen.ResetWorldInfo param0) {
        if (this.slot == -1) {
            this.triggerResetWorld(param0);
        } else {
            this.typeToReset = RealmsResetWorldScreen.ResetType.GENERATE;
            this.worldInfoToReset = param0;
            this.switchSlot();
        }

    }

    private void triggerResetWorld(RealmsResetWorldScreen.ResetWorldInfo param0) {
        RealmsTasks.ResettingWorldTask var0 = new RealmsTasks.ResettingWorldTask(
            this.serverData.id, this.returnScreen, param0.seed, param0.levelType, param0.generateStructures
        );
        if (this.resetTitle != null) {
            var0.setResetTitle(this.resetTitle);
        }

        if (this.confirmationId != -1) {
            var0.setConfirmationId(this.confirmationId);
        }

        RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(this.lastScreen, var0);
        var1.start();
        Realms.setScreen(var1);
    }

    @OnlyIn(Dist.CLIENT)
    abstract class FrameButton extends RealmsButton {
        private final long imageId;
        private final String image;
        private final RealmsResetWorldScreen.ResetType resetType;

        public FrameButton(int param0, int param1, String param2, long param3, String param4, RealmsResetWorldScreen.ResetType param5) {
            super(100 + param5.ordinal(), param0, param1, 60, 72, param2);
            this.imageId = param3;
            this.image = param4;
            this.resetType = param5;
        }

        @Override
        public void tick() {
            super.tick();
        }

        @Override
        public void render(int param0, int param1, float param2) {
            super.render(param0, param1, param2);
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsResetWorldScreen.this.drawFrame(
                this.x(),
                this.y(),
                this.getProxy().getMessage(),
                this.imageId,
                this.image,
                this.resetType,
                this.getProxy().isHovered(),
                this.getProxy().isMouseOver((double)param0, (double)param1)
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum ResetType {
        NONE,
        GENERATE,
        UPLOAD,
        ADVENTURE,
        SURVIVAL_SPAWN,
        EXPERIENCE,
        INSPIRATION;
    }

    @OnlyIn(Dist.CLIENT)
    public static class ResetWorldInfo {
        String seed;
        int levelType;
        boolean generateStructures;

        public ResetWorldInfo(String param0, int param1, boolean param2) {
            this.seed = param0;
            this.levelType = param1;
            this.generateStructures = param2;
        }
    }
}
