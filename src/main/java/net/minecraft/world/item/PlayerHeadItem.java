package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class PlayerHeadItem extends StandingAndWallBlockItem {
    public static final String TAG_SKULL_OWNER = "SkullOwner";

    public PlayerHeadItem(Block param0, Block param1, Item.Properties param2) {
        super(param0, param1, param2, Direction.DOWN);
    }

    @Override
    public Component getName(ItemStack param0) {
        if (param0.is(Items.PLAYER_HEAD) && param0.hasTag()) {
            String var0 = null;
            CompoundTag var1 = param0.getTag();
            if (var1.contains("SkullOwner", 8)) {
                var0 = var1.getString("SkullOwner");
            } else if (var1.contains("SkullOwner", 10)) {
                CompoundTag var2 = var1.getCompound("SkullOwner");
                if (var2.contains("Name", 8)) {
                    var0 = var2.getString("Name");
                }
            }

            if (var0 != null) {
                return Component.translatable(this.getDescriptionId() + ".named", var0);
            }
        }

        return super.getName(param0);
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag param0) {
        super.verifyTagAfterLoad(param0);
        SkullBlockEntity.resolveGameProfile(param0);
    }
}
