package net.minecraft.server.level;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class DemoMode extends ServerPlayerGameMode {
    private boolean displayedIntro;
    private boolean demoHasEnded;
    private int demoEndedReminder;
    private int gameModeTicks;

    public DemoMode(ServerPlayer param0) {
        super(param0);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.gameModeTicks;
        long var0 = this.level.getGameTime();
        long var1 = var0 / 24000L + 1L;
        if (!this.displayedIntro && this.gameModeTicks > 20) {
            this.displayedIntro = true;
            this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 0.0F));
        }

        this.demoHasEnded = var0 > 120500L;
        if (this.demoHasEnded) {
            ++this.demoEndedReminder;
        }

        if (var0 % 24000L == 500L) {
            if (var1 <= 6L) {
                if (var1 == 6L) {
                    this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 104.0F));
                } else {
                    this.player.sendMessage(new TranslatableComponent("demo.day." + var1), Util.NIL_UUID);
                }
            }
        } else if (var1 == 1L) {
            if (var0 == 100L) {
                this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 101.0F));
            } else if (var0 == 175L) {
                this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 102.0F));
            } else if (var0 == 250L) {
                this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 103.0F));
            }
        } else if (var1 == 5L && var0 % 24000L == 22000L) {
            this.player.sendMessage(new TranslatableComponent("demo.day.warning"), Util.NIL_UUID);
        }

    }

    private void outputDemoReminder() {
        if (this.demoEndedReminder > 100) {
            this.player.sendMessage(new TranslatableComponent("demo.reminder"), Util.NIL_UUID);
            this.demoEndedReminder = 0;
        }

    }

    @Override
    public void handleBlockBreakAction(BlockPos param0, ServerboundPlayerActionPacket.Action param1, Direction param2, int param3) {
        if (this.demoHasEnded) {
            this.outputDemoReminder();
        } else {
            super.handleBlockBreakAction(param0, param1, param2, param3);
        }
    }

    @Override
    public InteractionResult useItem(ServerPlayer param0, Level param1, ItemStack param2, InteractionHand param3) {
        if (this.demoHasEnded) {
            this.outputDemoReminder();
            return InteractionResult.PASS;
        } else {
            return super.useItem(param0, param1, param2, param3);
        }
    }

    @Override
    public InteractionResult useItemOn(ServerPlayer param0, Level param1, ItemStack param2, InteractionHand param3, BlockHitResult param4) {
        if (this.demoHasEnded) {
            this.outputDemoReminder();
            return InteractionResult.PASS;
        } else {
            return super.useItemOn(param0, param1, param2, param3, param4);
        }
    }
}
