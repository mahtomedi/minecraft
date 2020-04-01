package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BookBoxBlock extends Block {
    private static final char[] CHARACTERS = new char[]{
        ' ', ',', '.', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BookBoxBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        Direction var0 = param0.getValue(FACING);
        int var1 = param2.getY();
        int var2;
        int var3;
        switch(var0) {
            case NORTH:
                var2 = 15 - param2.getX() & 15;
                var3 = 0;
                break;
            case SOUTH:
                var2 = param2.getX() & 15;
                var3 = 2;
                break;
            case EAST:
                var2 = 15 - param2.getZ() & 15;
                var3 = 1;
                break;
            case WEST:
            default:
                var2 = param2.getZ() & 15;
                var3 = 3;
        }

        if (var2 > 0 && var2 < 15) {
            ChunkPos var10 = new ChunkPos(param2);
            String var11 = var10.x + "/" + var10.z + "/" + var3 + "/" + var2 + "/" + var1;
            Random var12 = new Random((long)var10.x);
            Random var13 = new Random((long)var10.z);
            Random var14 = new Random((long)((var2 << 8) + (var1 << 4) + var3));
            ItemStack var15 = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag var16 = var15.getOrCreateTag();
            ListTag var17 = new ListTag();

            for(int var18 = 0; var18 < 16; ++var18) {
                StringBuilder var19 = new StringBuilder();

                for(int var20 = 0; var20 < 128; ++var20) {
                    int var21 = var12.nextInt() + var13.nextInt() + -var14.nextInt();
                    var19.append(CHARACTERS[Math.floorMod(var21, CHARACTERS.length)]);
                }

                var17.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(var19.toString()))));
            }

            var16.put("pages", var17);
            var16.putString("author", ChatFormatting.OBFUSCATED + "Universe itself");
            var16.putString("title", var11);
            popResource(param1, param2.relative(param5.getDirection()), var15);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Direction var0 = param0.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, var0);
    }
}
