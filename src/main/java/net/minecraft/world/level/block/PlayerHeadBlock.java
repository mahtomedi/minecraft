package net.minecraft.world.level.block;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerHeadBlock extends SkullBlock {
    protected PlayerHeadBlock(BlockBehaviour.Properties param0) {
        super(SkullBlock.Types.PLAYER, param0);
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        super.setPlacedBy(param0, param1, param2, param3, param4);
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof SkullBlockEntity var1) {
            GameProfile var2 = null;
            if (param4.hasTag()) {
                CompoundTag var3 = param4.getTag();
                if (var3.contains("SkullOwner", 10)) {
                    var2 = NbtUtils.readGameProfile(var3.getCompound("SkullOwner"));
                } else if (var3.contains("SkullOwner", 8) && !Util.isBlank(var3.getString("SkullOwner"))) {
                    var2 = new GameProfile(Util.NIL_UUID, var3.getString("SkullOwner"));
                }
            }

            var1.setOwner(var2);
        }

    }
}
