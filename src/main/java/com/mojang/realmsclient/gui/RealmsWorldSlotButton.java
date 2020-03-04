package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldSlotButton extends Button implements TickableWidget {
    public static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("realms", "textures/gui/realms/empty_frame.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
    private final Supplier<RealmsServer> serverDataProvider;
    private final Consumer<String> toolTipSetter;
    private final int slotIndex;
    private int animTick;
    @Nullable
    private RealmsWorldSlotButton.State state;

    public RealmsWorldSlotButton(
        int param0, int param1, int param2, int param3, Supplier<RealmsServer> param4, Consumer<String> param5, int param6, Button.OnPress param7
    ) {
        super(param0, param1, param2, param3, "", param7);
        this.serverDataProvider = param4;
        this.slotIndex = param6;
        this.toolTipSetter = param5;
    }

    @Nullable
    public RealmsWorldSlotButton.State getState() {
        return this.state;
    }

    @Override
    public void tick() {
        ++this.animTick;
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

            RealmsWorldSlotButton.Action var13 = RealmsWorldSlotButton.Action.NOTHING;
            String var14 = null;
            if (var3) {
                if (!var0.expired && var0.state != RealmsServer.State.UNINITIALIZED) {
                    var13 = RealmsWorldSlotButton.Action.JOIN;
                    var14 = I18n.get("mco.configure.world.slot.tooltip.active");
                }
            } else if (var2) {
                if (!var0.expired) {
                    var13 = RealmsWorldSlotButton.Action.SWITCH_SLOT;
                    var14 = I18n.get("mco.configure.world.slot.tooltip.minigame");
                }
            } else {
                var13 = RealmsWorldSlotButton.Action.SWITCH_SLOT;
                var14 = I18n.get("mco.configure.world.slot.tooltip");
            }

            this.state = new RealmsWorldSlotButton.State(var3, var4, var5, var6, var7, var2, var13, var14);
            this.handleNarration(var0, this.state.slotName, this.state.empty, this.state.minigame, this.state.action, this.state.actionPrompt);
        }
    }

    public void handleNarration(RealmsServer param0, String param1, boolean param2, boolean param3, RealmsWorldSlotButton.Action param4, String param5) {
        String var0;
        if (param4 == RealmsWorldSlotButton.Action.NOTHING) {
            var0 = param1;
        } else if (param3) {
            if (param2) {
                var0 = param5;
            } else {
                var0 = param5 + " " + param1 + " " + param0.minigameName;
            }
        } else {
            var0 = param5 + " " + param1;
        }

        this.setMessage(var0);
    }

    @Override
    public void renderButton(int param0, int param1, float param2) {
        if (this.state != null) {
            this.drawSlotFrame(
                this.x,
                this.y,
                param0,
                param1,
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
        int param0,
        int param1,
        int param2,
        int param3,
        boolean param4,
        String param5,
        int param6,
        long param7,
        @Nullable String param8,
        boolean param9,
        boolean param10,
        RealmsWorldSlotButton.Action param11,
        @Nullable String param12
    ) {
        boolean var0 = this.isHovered();
        if (this.isMouseOver((double)param2, (double)param3) && param12 != null) {
            this.toolTipSetter.accept(param12);
        }

        Minecraft var1 = Minecraft.getInstance();
        TextureManager var2 = var1.getTextureManager();
        if (param10) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param7), param8);
        } else if (param9) {
            var2.bind(EMPTY_SLOT_LOCATION);
        } else if (param8 != null && param7 != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param7), param8);
        } else if (param6 == 1) {
            var2.bind(DEFAULT_WORLD_SLOT_1);
        } else if (param6 == 2) {
            var2.bind(DEFAULT_WORLD_SLOT_2);
        } else if (param6 == 3) {
            var2.bind(DEFAULT_WORLD_SLOT_3);
        }

        if (param4) {
            float var3 = 0.85F + 0.15F * Mth.cos((float)this.animTick * 0.2F);
            RenderSystem.color4f(var3, var3, var3, 1.0F);
        } else {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        }

        blit(param0 + 3, param1 + 3, 0.0F, 0.0F, 74, 74, 74, 74);
        var2.bind(SLOT_FRAME_LOCATION);
        boolean var4 = var0 && param11 != RealmsWorldSlotButton.Action.NOTHING;
        if (var4) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else if (param4) {
            RenderSystem.color4f(0.8F, 0.8F, 0.8F, 1.0F);
        } else {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        }

        blit(param0, param1, 0.0F, 0.0F, 80, 80, 80, 80);
        this.drawCenteredString(var1.font, param5, param0 + 40, param1 + 66, 16777215);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT,
        JOIN;
    }

    @OnlyIn(Dist.CLIENT)
    public static class State {
        private final boolean isCurrentlyActiveSlot;
        private final String slotName;
        private final long imageId;
        private final String image;
        public final boolean empty;
        public final boolean minigame;
        public final RealmsWorldSlotButton.Action action;
        private final String actionPrompt;

        State(
            boolean param0,
            String param1,
            long param2,
            @Nullable String param3,
            boolean param4,
            boolean param5,
            RealmsWorldSlotButton.Action param6,
            @Nullable String param7
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
