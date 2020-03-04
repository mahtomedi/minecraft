package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ScaffoldingBlockItem extends BlockItem {
    public ScaffoldingBlockItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext param0) {
        BlockPos var0 = param0.getClickedPos();
        Level var1 = param0.getLevel();
        BlockState var2 = var1.getBlockState(var0);
        Block var3 = this.getBlock();
        if (var2.getBlock() != var3) {
            return ScaffoldingBlock.getDistance(var1, var0) == 7 ? null : param0;
        } else {
            Direction var4;
            if (param0.isSecondaryUseActive()) {
                var4 = param0.isInside() ? param0.getClickedFace().getOpposite() : param0.getClickedFace();
            } else {
                var4 = param0.getClickedFace() == Direction.UP ? param0.getHorizontalDirection() : Direction.UP;
            }

            int var6 = 0;
            BlockPos.MutableBlockPos var7 = var0.mutable().move(var4);

            while(var6 < 7) {
                if (!var1.isClientSide && !Level.isInWorldBounds(var7)) {
                    Player var8 = param0.getPlayer();
                    int var9 = var1.getMaxBuildHeight();
                    if (var8 instanceof ServerPlayer && var7.getY() >= var9) {
                        ClientboundChatPacket var10 = new ClientboundChatPacket(
                            new TranslatableComponent("build.tooHigh", var9).withStyle(ChatFormatting.RED), ChatType.GAME_INFO
                        );
                        ((ServerPlayer)var8).connection.send(var10);
                    }
                    break;
                }

                var2 = var1.getBlockState(var7);
                if (var2.getBlock() != this.getBlock()) {
                    if (var2.canBeReplaced(param0)) {
                        return BlockPlaceContext.at(param0, var7, var4);
                    }
                    break;
                }

                var7.move(var4);
                if (var4.getAxis().isHorizontal()) {
                    ++var6;
                }
            }

            return null;
        }
    }

    @Override
    protected boolean mustSurvive() {
        return false;
    }
}
