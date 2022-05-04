package net.minecraft.world.item.trading;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MerchantOffers extends ArrayList<MerchantOffer> {
    public MerchantOffers() {
    }

    private MerchantOffers(int param0) {
        super(param0);
    }

    public MerchantOffers(CompoundTag param0) {
        ListTag var0 = param0.getList("Recipes", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            this.add(new MerchantOffer(var0.getCompound(var1)));
        }

    }

    @Nullable
    public MerchantOffer getRecipeFor(ItemStack param0, ItemStack param1, int param2) {
        if (param2 > 0 && param2 < this.size()) {
            MerchantOffer var0 = this.get(param2);
            return var0.satisfiedBy(param0, param1) ? var0 : null;
        } else {
            for(int var1 = 0; var1 < this.size(); ++var1) {
                MerchantOffer var2 = this.get(var1);
                if (var2.satisfiedBy(param0, param1)) {
                    return var2;
                }
            }

            return null;
        }
    }

    public void writeToStream(FriendlyByteBuf param0) {
        param0.writeCollection(this, (param0x, param1) -> {
            param0x.writeItem(param1.getBaseCostA());
            param0x.writeItem(param1.getResult());
            param0x.writeItem(param1.getCostB());
            param0x.writeBoolean(param1.isOutOfStock());
            param0x.writeInt(param1.getUses());
            param0x.writeInt(param1.getMaxUses());
            param0x.writeInt(param1.getXp());
            param0x.writeInt(param1.getSpecialPriceDiff());
            param0x.writeFloat(param1.getPriceMultiplier());
            param0x.writeInt(param1.getDemand());
        });
    }

    public static MerchantOffers createFromStream(FriendlyByteBuf param0) {
        return param0.readCollection(MerchantOffers::new, param0x -> {
            ItemStack var0x = param0x.readItem();
            ItemStack var1 = param0x.readItem();
            ItemStack var2 = param0x.readItem();
            boolean var3 = param0x.readBoolean();
            int var4 = param0x.readInt();
            int var5 = param0x.readInt();
            int var6 = param0x.readInt();
            int var7 = param0x.readInt();
            float var8 = param0x.readFloat();
            int var9 = param0x.readInt();
            MerchantOffer var10 = new MerchantOffer(var0x, var2, var1, var4, var5, var6, var8, var9);
            if (var3) {
                var10.setToOutOfStock();
            }

            var10.setSpecialPriceDiff(var7);
            return var10;
        });
    }

    public CompoundTag createTag() {
        CompoundTag var0 = new CompoundTag();
        ListTag var1 = new ListTag();

        for(int var2 = 0; var2 < this.size(); ++var2) {
            MerchantOffer var3 = this.get(var2);
            var1.add(var3.createTag());
        }

        var0.put("Recipes", var1);
        return var0;
    }
}
