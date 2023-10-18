package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldSlotButton extends Button {
    private static final ResourceLocation SLOT_FRAME_SPRITE = new ResourceLocation("widget/slot_frame");
    private static final ResourceLocation CHECKMARK_SPRITE = new ResourceLocation("icon/checkmark");
    public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("textures/gui/realms/empty_frame.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
    private static final Component SLOT_ACTIVE_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.active");
    private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
    private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
    static final Component MINIGAME = Component.translatable("mco.worldSlot.minigame");
    private final int slotIndex;
    @Nullable
    private RealmsWorldSlotButton.State state;
    @Nullable
    private Tooltip tooltip;

    public RealmsWorldSlotButton(int param0, int param1, int param2, int param3, int param4, Button.OnPress param5) {
        super(param0, param1, param2, param3, CommonComponents.EMPTY, param5, DEFAULT_NARRATION);
        this.slotIndex = param4;
    }

    @Nullable
    public RealmsWorldSlotButton.State getState() {
        return this.state;
    }

    public void setServerData(RealmsServer param0) {
        this.state = new RealmsWorldSlotButton.State(param0, this.slotIndex);
        this.setTooltipAndNarration(this.state, param0.minigameName);
    }

    private void setTooltipAndNarration(RealmsWorldSlotButton.State param0, String param1) {
        Component var0 = switch(param0.action) {
            case JOIN -> SLOT_ACTIVE_TOOLTIP;
            case SWITCH_SLOT -> param0.minigame ? SWITCH_TO_MINIGAME_SLOT_TOOLTIP : SWITCH_TO_WORLD_SLOT_TOOLTIP;
            default -> null;
        };
        if (var0 == null) {
            this.setMessage(Component.literal(param0.slotName));
        } else {
            this.tooltip = Tooltip.create(var0);
            if (param0.empty) {
                this.setMessage(var0);
            } else {
                MutableComponent var1 = var0.copy().append(CommonComponents.space()).append(Component.literal(param0.slotName));
                if (param0.minigame) {
                    var1 = var1.append(CommonComponents.SPACE).append(param1);
                }

                this.setMessage(var1);
            }
        }
    }

    static RealmsWorldSlotButton.Action getAction(RealmsServer param0, boolean param1, boolean param2) {
        if (param1 && !param0.expired && param0.state != RealmsServer.State.UNINITIALIZED) {
            return RealmsWorldSlotButton.Action.JOIN;
        } else {
            return param1 || param2 && param0.expired ? RealmsWorldSlotButton.Action.NOTHING : RealmsWorldSlotButton.Action.SWITCH_SLOT;
        }
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.state != null) {
            int var0 = this.getX();
            int var1 = this.getY();
            boolean var2 = this.isHoveredOrFocused();
            if (this.tooltip != null) {
                this.tooltip.refreshTooltipForNextRenderPass(this.isHovered(), this.isFocused(), this.getRectangle());
            }

            ResourceLocation var3;
            if (this.state.minigame) {
                var3 = RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image);
            } else if (this.state.empty) {
                var3 = EMPTY_SLOT_LOCATION;
            } else if (this.state.image != null && this.state.imageId != -1L) {
                var3 = RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image);
            } else if (this.slotIndex == 1) {
                var3 = DEFAULT_WORLD_SLOT_1;
            } else if (this.slotIndex == 2) {
                var3 = DEFAULT_WORLD_SLOT_2;
            } else if (this.slotIndex == 3) {
                var3 = DEFAULT_WORLD_SLOT_3;
            } else {
                var3 = EMPTY_SLOT_LOCATION;
            }

            if (this.state.isCurrentlyActiveSlot) {
                param0.setColor(0.56F, 0.56F, 0.56F, 1.0F);
            }

            param0.blit(var3, var0 + 3, var1 + 3, 0.0F, 0.0F, 74, 74, 74, 74);
            boolean var10 = var2 && this.state.action != RealmsWorldSlotButton.Action.NOTHING;
            if (var10) {
                param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else if (this.state.isCurrentlyActiveSlot) {
                param0.setColor(0.8F, 0.8F, 0.8F, 1.0F);
            } else {
                param0.setColor(0.56F, 0.56F, 0.56F, 1.0F);
            }

            param0.blitSprite(SLOT_FRAME_SPRITE, var0, var1, 80, 80);
            param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.state.isCurrentlyActiveSlot) {
                RenderSystem.enableBlend();
                param0.blitSprite(CHECKMARK_SPRITE, var0 + 67, var1 + 4, 9, 8);
                RenderSystem.disableBlend();
            }

            Font var11 = Minecraft.getInstance().font;
            param0.drawCenteredString(var11, this.state.slotName, var0 + 40, var1 + 66, -1);
            param0.drawCenteredString(
                var11, RealmsMainScreen.getVersionComponent(this.state.slotVersion, this.state.compatibility.isCompatible()), var0 + 40, var1 + 80 + 2, -1
            );
        }
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
        final String slotVersion;
        final RealmsServer.Compatibility compatibility;
        final long imageId;
        @Nullable
        final String image;
        public final boolean empty;
        public final boolean minigame;
        public final RealmsWorldSlotButton.Action action;

        public State(RealmsServer param0, int param1) {
            this.minigame = param1 == 4;
            if (this.minigame) {
                this.isCurrentlyActiveSlot = param0.worldType == RealmsServer.WorldType.MINIGAME;
                this.slotName = RealmsWorldSlotButton.MINIGAME.getString();
                this.imageId = (long)param0.minigameId;
                this.image = param0.minigameImage;
                this.empty = param0.minigameId == -1;
                this.slotVersion = "";
                this.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
            } else {
                RealmsWorldOptions var0 = param0.slots.get(param1);
                this.isCurrentlyActiveSlot = param0.activeSlot == param1 && param0.worldType != RealmsServer.WorldType.MINIGAME;
                this.slotName = var0.getSlotName(param1);
                this.imageId = var0.templateId;
                this.image = var0.templateImage;
                this.empty = var0.empty;
                this.slotVersion = var0.version;
                this.compatibility = var0.compatibility;
            }

            this.action = RealmsWorldSlotButton.getAction(param0, this.isCurrentlyActiveSlot, this.minigame);
        }
    }
}
