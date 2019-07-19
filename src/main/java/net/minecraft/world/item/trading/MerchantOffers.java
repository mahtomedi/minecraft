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
        param0.writeByte((byte)(this.size() & 0xFF));

        for(int var0 = 0; var0 < this.size(); ++var0) {
            MerchantOffer var1 = this.get(var0);
            param0.writeItem(var1.getBaseCostA());
            param0.writeItem(var1.getResult());
            ItemStack var2 = var1.getCostB();
            param0.writeBoolean(!var2.isEmpty());
            if (!var2.isEmpty()) {
                param0.writeItem(var2);
            }

            param0.writeBoolean(var1.isOutOfStock());
            param0.writeInt(var1.getUses());
            param0.writeInt(var1.getMaxUses());
            param0.writeInt(var1.getXp());
            param0.writeInt(var1.getSpecialPriceDiff());
            param0.writeFloat(var1.getPriceMultiplier());
            param0.writeInt(var1.getDemand());
        }

    }

    public static MerchantOffers createFromStream(FriendlyByteBuf param0) {
        MerchantOffers var0 = new MerchantOffers();
        int var1 = param0.readByte() & 255;

        for(int var2 = 0; var2 < var1; ++var2) {
            ItemStack var3 = param0.readItem();
            ItemStack var4 = param0.readItem();
            ItemStack var5 = ItemStack.EMPTY;
            if (param0.readBoolean()) {
                var5 = param0.readItem();
            }

            boolean var6 = param0.readBoolean();
            int var7 = param0.readInt();
            int var8 = param0.readInt();
            int var9 = param0.readInt();
            int var10 = param0.readInt();
            float var11 = param0.readFloat();
            int var12 = param0.readInt();
            MerchantOffer var13 = new MerchantOffer(var3, var5, var4, var7, var8, var9, var11, var12);
            if (var6) {
                var13.setToOutOfStock();
            }

            var13.setSpecialPriceDiff(var10);
            var0.add(var13);
        }

        return var0;
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
