package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsButtonProxy;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldSlotButton extends RealmsButton {
    private final Supplier<RealmsServer> serverDataProvider;
    private final Consumer<String> toolTipSetter;
    private final RealmsWorldSlotButton.Listener listener;
    private final int slotIndex;
    private int animTick;
    private RealmsWorldSlotButton.State state;

    public RealmsWorldSlotButton(
        int param0,
        int param1,
        int param2,
        int param3,
        Supplier<RealmsServer> param4,
        Consumer<String> param5,
        int param6,
        int param7,
        RealmsWorldSlotButton.Listener param8
    ) {
        super(param6, param0, param1, param2, param3, "");
        this.serverDataProvider = param4;
        this.slotIndex = param7;
        this.toolTipSetter = param5;
        this.listener = param8;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        super.render(param0, param1, param2);
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
                var3 = var0.worldType.equals(RealmsServer.WorldType.MINIGAME);
                var4 = "Minigame";
                var5 = (long)var0.minigameId;
                var6 = var0.minigameImage;
                var7 = var0.minigameId == -1;
            } else {
                var3 = var0.activeSlot == this.slotIndex && !var0.worldType.equals(RealmsServer.WorldType.MINIGAME);
                var4 = var1.getSlotName(this.slotIndex);
                var5 = var1.templateId;
                var6 = var1.templateImage;
                var7 = var1.empty;
            }

            String var13 = null;
            RealmsWorldSlotButton.Action var16;
            if (var3) {
                boolean var14 = var0.state == RealmsServer.State.OPEN || var0.state == RealmsServer.State.CLOSED;
                if (!var0.expired && var14) {
                    var16 = RealmsWorldSlotButton.Action.JOIN;
                    var13 = Realms.getLocalizedString("mco.configure.world.slot.tooltip.active");
                } else {
                    var16 = RealmsWorldSlotButton.Action.NOTHING;
                }
            } else if (var2) {
                if (var0.expired) {
                    var16 = RealmsWorldSlotButton.Action.NOTHING;
                } else {
                    var16 = RealmsWorldSlotButton.Action.SWITCH_SLOT;
                    var13 = Realms.getLocalizedString("mco.configure.world.slot.tooltip.minigame");
                }
            } else {
                var16 = RealmsWorldSlotButton.Action.SWITCH_SLOT;
                var13 = Realms.getLocalizedString("mco.configure.world.slot.tooltip");
            }

            this.state = new RealmsWorldSlotButton.State(var3, var4, var5, var6, var7, var2, var16, var13);
            String var20;
            if (var16 == RealmsWorldSlotButton.Action.NOTHING) {
                var20 = var4;
            } else if (var2) {
                if (var7) {
                    var20 = var13;
                } else {
                    var20 = var13 + " " + var4 + " " + var0.minigameName;
                }
            } else {
                var20 = var13 + " " + var4;
            }

            this.setMessage(var20);
        }
    }

    @Override
    public void renderButton(int param0, int param1, float param2) {
        if (this.state != null) {
            RealmsButtonProxy var0 = this.getProxy();
            this.drawSlotFrame(
                var0.x,
                var0.y,
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
        boolean var0 = this.getProxy().isHovered();
        if (this.getProxy().isMouseOver((double)param2, (double)param3) && param12 != null) {
            this.toolTipSetter.accept(param12);
        }

        if (param10) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param7), param8);
        } else if (param9) {
            Realms.bind("realms:textures/gui/realms/empty_frame.png");
        } else if (param8 != null && param7 != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param7), param8);
        } else if (param6 == 1) {
            Realms.bind("textures/gui/title/background/panorama_0.png");
        } else if (param6 == 2) {
            Realms.bind("textures/gui/title/background/panorama_2.png");
        } else if (param6 == 3) {
            Realms.bind("textures/gui/title/background/panorama_3.png");
        }

        if (param4) {
            float var1 = 0.85F + 0.15F * RealmsMth.cos((float)this.animTick * 0.2F);
            GlStateManager.color4f(var1, var1, var1, 1.0F);
        } else {
            GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        }

        RealmsScreen.blit(param0 + 3, param1 + 3, 0.0F, 0.0F, 74, 74, 74, 74);
        Realms.bind("realms:textures/gui/realms/slot_frame.png");
        boolean var2 = var0 && param11 != RealmsWorldSlotButton.Action.NOTHING;
        if (var2) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else if (param4) {
            GlStateManager.color4f(0.8F, 0.8F, 0.8F, 1.0F);
        } else {
            GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        }

        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 80, 80, 80, 80);
        this.drawCenteredString(param5, param0 + 40, param1 + 66, 16777215);
    }

    @Override
    public void onPress() {
        this.listener.onSlotClick(this.slotIndex, this.state.action, this.state.minigame, this.state.empty);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT,
        JOIN;
    }

    @OnlyIn(Dist.CLIENT)
    public interface Listener {
        void onSlotClick(int var1, @Nonnull RealmsWorldSlotButton.Action var2, boolean var3, boolean var4);
    }

    @OnlyIn(Dist.CLIENT)
    public static class State {
        final boolean isCurrentlyActiveSlot;
        final String slotName;
        final long imageId;
        public final String image;
        public final boolean empty;
        final boolean minigame;
        public final RealmsWorldSlotButton.Action action;
        final String actionPrompt;

        State(
            boolean param0,
            String param1,
            long param2,
            @Nullable String param3,
            boolean param4,
            boolean param5,
            @Nonnull RealmsWorldSlotButton.Action param6,
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
