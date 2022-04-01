package net.minecraft.world.item.trading;

import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class MerchantOffers extends ArrayList<MerchantOffer> {
    public MerchantOffers() {
    }

    public MerchantOffers(CompoundTag param0) {
        ListTag var0 = param0.getList("Recipes", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            this.add(new MerchantOffer(var0.getCompound(var1)));
        }

    }

    public void writeToStream(FriendlyByteBuf param0) {
        param0.writeByte((byte)(this.size() & 0xFF));

        for(int var0 = 0; var0 < this.size(); ++var0) {
            MerchantOffer var1 = this.get(var0);
            param0.writeWithCodec(CarryableTrade.CODEC, var1.getCost());
            param0.writeWithCodec(CarryableTrade.CODEC, var1.getResult());
            param0.writeBoolean(var1.isOutOfStock());
            param0.writeInt(var1.getUses());
            param0.writeInt(var1.getMaxUses());
            param0.writeInt(var1.getXp());
            param0.writeFloat(var1.getPriceMultiplier());
            param0.writeInt(var1.getDemand());
        }

    }

    public static MerchantOffers createFromStream(FriendlyByteBuf param0) {
        MerchantOffers var0 = new MerchantOffers();
        int var1 = param0.readByte() & 255;

        for(int var2 = 0; var2 < var1; ++var2) {
            CarryableTrade var3 = param0.readWithCodec(CarryableTrade.CODEC);
            CarryableTrade var4 = param0.readWithCodec(CarryableTrade.CODEC);
            boolean var5 = param0.readBoolean();
            int var6 = param0.readInt();
            int var7 = param0.readInt();
            int var8 = param0.readInt();
            float var9 = param0.readFloat();
            int var10 = param0.readInt();
            MerchantOffer var11 = new MerchantOffer(var3, var4, var6, var7, var8, var9, var10);
            if (var5) {
                var11.setToOutOfStock();
            }

            var0.add(var11);
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
