package net.minecraft.world.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHeadItem extends StandingAndWallBlockItem {
    public PlayerHeadItem(Block param0, Block param1, Item.Properties param2) {
        super(param0, param1, param2);
    }

    @Override
    public Component getName(ItemStack param0) {
        if (param0.getItem() == Items.PLAYER_HEAD && param0.hasTag()) {
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
                return new TranslatableComponent(this.getDescriptionId() + ".named", var0);
            }
        }

        return super.getName(param0);
    }

    @Override
    public boolean verifyTagAfterLoad(CompoundTag param0) {
        super.verifyTagAfterLoad(param0);
        if (param0.contains("SkullOwner", 8) && !StringUtils.isBlank(param0.getString("SkullOwner"))) {
            GameProfile var0 = new GameProfile(null, param0.getString("SkullOwner"));
            var0 = SkullBlockEntity.updateGameprofile(var0);
            param0.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), var0));
            return true;
        } else {
            return false;
        }
    }
}
