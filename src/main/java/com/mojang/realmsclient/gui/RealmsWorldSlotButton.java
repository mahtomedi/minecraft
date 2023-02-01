package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldSlotButton extends Button {
    public static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("realms", "textures/gui/realms/empty_frame.png");
    public static final ResourceLocation CHECK_MARK_LOCATION = new ResourceLocation("minecraft", "textures/gui/checkmark.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
    private static final Component SLOT_ACTIVE_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.active");
    private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
    private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
    private final Supplier<RealmsServer> serverDataProvider;
    private final Consumer<Component> toolTipSetter;
    private final int slotIndex;
    @Nullable
    private RealmsWorldSlotButton.State state;

    public RealmsWorldSlotButton(
        int param0, int param1, int param2, int param3, Supplier<RealmsServer> param4, Consumer<Component> param5, int param6, Button.OnPress param7
    ) {
        super(param0, param1, param2, param3, CommonComponents.EMPTY, param7, DEFAULT_NARRATION);
        this.serverDataProvider = param4;
        this.slotIndex = param6;
        this.toolTipSetter = param5;
    }

    @Nullable
    public RealmsWorldSlotButton.State getState() {
        return this.state;
    }

    public void tick() {
        RealmsServer var0 = this.serverDataProvider.get();
        if (var0 != null) {
            RealmsWorldOptions var1 = var0.slots.get(this.slotIndex);
            boolean var2 = this.slotIndex == 4;
            boolean var3;
            String var4;
            long var5;
            String var6;
            boolean var7;
            if (var2) {
                var3 = var0.worldType == RealmsServer.WorldType.MINIGAME;
                var4 = "Minigame";
                var5 = (long)var0.minigameId;
                var6 = var0.minigameImage;
                var7 = var0.minigameId == -1;
            } else {
                var3 = var0.activeSlot == this.slotIndex && var0.worldType != RealmsServer.WorldType.MINIGAME;
                var4 = var1.getSlotName(this.slotIndex);
                var5 = var1.templateId;
                var6 = var1.templateImage;
                var7 = var1.empty;
            }

            RealmsWorldSlotButton.Action var13 = getAction(var0, var3, var2);
            Pair<Component, Component> var14 = this.getTooltipAndNarration(var0, var4, var7, var2, var13);
            this.state = new RealmsWorldSlotButton.State(var3, var4, var5, var6, var7, var2, var13, var14.getFirst());
            this.setMessage(var14.getSecond());
        }
    }

    private static RealmsWorldSlotButton.Action getAction(RealmsServer param0, boolean param1, boolean param2) {
        if (param1) {
            if (!param0.expired && param0.state != RealmsServer.State.UNINITIALIZED) {
                return RealmsWorldSlotButton.Action.JOIN;
            }
        } else {
            if (!param2) {
                return RealmsWorldSlotButton.Action.SWITCH_SLOT;
            }

            if (!param0.expired) {
                return RealmsWorldSlotButton.Action.SWITCH_SLOT;
            }
        }

        return RealmsWorldSlotButton.Action.NOTHING;
    }

    private Pair<Component, Component> getTooltipAndNarration(
        RealmsServer param0, String param1, boolean param2, boolean param3, RealmsWorldSlotButton.Action param4
    ) {
        if (param4 == RealmsWorldSlotButton.Action.NOTHING) {
            return Pair.of(null, Component.literal(param1));
        } else {
            Component var0;
            if (param3) {
                if (param2) {
                    var0 = CommonComponents.EMPTY;
                } else {
                    var0 = CommonComponents.space().append(param1).append(CommonComponents.SPACE).append(param0.minigameName);
                }
            } else {
                var0 = CommonComponents.space().append(param1);
            }

            Component var3;
            if (param4 == RealmsWorldSlotButton.Action.JOIN) {
                var3 = SLOT_ACTIVE_TOOLTIP;
            } else {
                var3 = param3 ? SWITCH_TO_MINIGAME_SLOT_TOOLTIP : SWITCH_TO_WORLD_SLOT_TOOLTIP;
            }

            Component var5 = var3.copy().append(var0);
            return Pair.of(var3, var5);
        }
    }

    @Override
    public void renderWidget(PoseStack param0, int param1, int param2, float param3) {
        if (this.state != null) {
            this.drawSlotFrame(
                param0,
                this.getX(),
                this.getY(),
                param1,
                param2,
                this.state.isCurrentlyActiveSlot,
                this.state.slotName,
                this.slotIndex,
                this.state.imageId,
                this.state.image,
                this.state.empty,
                this.state.minigame,
                this.state.action,
                this.state.actionPrompt
            );
        }
    }

    private void drawSlotFrame(
        PoseStack param0,
        int param1,
        int param2,
        int param3,
        int param4,
        boolean param5,
        String param6,
        int param7,
        long param8,
        @Nullable String param9,
        boolean param10,
        boolean param11,
        RealmsWorldSlotButton.Action param12,
        @Nullable Component param13
    ) {
        boolean var0 = this.isHoveredOrFocused();
        if (this.isMouseOver((double)param3, (double)param4) && param13 != null) {
            this.toolTipSetter.accept(param13);
        }

        Minecraft var1 = Minecraft.getInstance();
        if (param11) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param8), param9);
        } else if (param10) {
            RenderSystem.setShaderTexture(0, EMPTY_SLOT_LOCATION);
        } else if (param9 != null && param8 != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param8), param9);
        } else if (param7 == 1) {
            RenderSystem.setShaderTexture(0, DEFAULT_WORLD_SLOT_1);
        } else if (param7 == 2) {
            RenderSystem.setShaderTexture(0, DEFAULT_WORLD_SLOT_2);
        } else if (param7 == 3) {
            RenderSystem.setShaderTexture(0, DEFAULT_WORLD_SLOT_3);
        }

        if (param5) {
            RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
        }

        blit(param0, param1 + 3, param2 + 3, 0.0F, 0.0F, 74, 74, 74, 74);
        RenderSystem.setShaderTexture(0, SLOT_FRAME_LOCATION);
        boolean var2 = var0 && param12 != RealmsWorldSlotButton.Action.NOTHING;
        if (var2) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else if (param5) {
            RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 1.0F);
        } else {
            RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
        }

        blit(param0, param1, param2, 0.0F, 0.0F, 80, 80, 80, 80);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (param5) {
            this.renderCheckMark(param0, param1, param2);
        }

        drawCenteredString(param0, var1.font, param6, param1 + 40, param2 + 66, 16777215);
    }

    private void renderCheckMark(PoseStack param0, int param1, int param2) {
        RenderSystem.setShaderTexture(0, CHECK_MARK_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        blit(param0, param1 + 67, param2 + 4, 0.0F, 0.0F, 9, 8, 9, 8);
        RenderSystem.disableBlend();
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT,
        JOIN;
    }

    @OnlyIn(Dist.CLIENT)
    public static class State {
        final boolean isCurrentlyActiveSlot;
        final String slotName;
        final long imageId;
        @Nullable
        final String image;
        public final boolean empty;
        public final boolean minigame;
        public final RealmsWorldSlotButton.Action action;
        @Nullable
        final Component actionPrompt;

        State(
            boolean param0,
            String param1,
            long param2,
            @Nullable String param3,
            boolean param4,
            boolean param5,
            RealmsWorldSlotButton.Action param6,
            @Nullable Component param7
        ) {
            this.isCurrentlyActiveSlot = param0;
            this.slotName = param1;
            this.imageId = param2;
            this.image = param3;
            this.empty = param4;
            this.minigame = param5;
            this.action = param6;
            this.actionPrompt = param7;
        }
    }
}
