package net.minecraft.world.item.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class MerchantOffer {
    private final CarryableTrade cost;
    private final CarryableTrade result;
    private int uses;
    private final int maxUses;
    private boolean rewardExp = true;
    private int demand;
    private float priceMultiplier;
    private int xp = 1;

    public MerchantOffer(CompoundTag param0) {
        this.cost = CarryableTrade.CODEC.parse(NbtOps.INSTANCE, param0.getCompound("buy")).result().orElse(CarryableTrade.block(Blocks.AIR));
        this.result = CarryableTrade.CODEC.parse(NbtOps.INSTANCE, param0.getCompound("sell")).result().orElse(CarryableTrade.block(Blocks.AIR));
        this.uses = param0.getInt("uses");
        if (param0.contains("maxUses", 99)) {
            this.maxUses = param0.getInt("maxUses");
        } else {
            this.maxUses = 4;
        }

        if (param0.contains("rewardExp", 1)) {
            this.rewardExp = param0.getBoolean("rewardExp");
        }

        if (param0.contains("xp", 3)) {
            this.xp = param0.getInt("xp");
        }

        if (param0.contains("priceMultiplier", 5)) {
            this.priceMultiplier = param0.getFloat("priceMultiplier");
        }

        this.demand = param0.getInt("demand");
    }

    public MerchantOffer(CarryableTrade param0, CarryableTrade param1, int param2, int param3, float param4) {
        this(param0, param1, 0, param2, param3, param4);
    }

    public MerchantOffer(CarryableTrade param0, CarryableTrade param1, int param2, int param3, int param4, float param5) {
        this(param0, param1, param2, param3, param4, param5, 0);
    }

    public MerchantOffer(CarryableTrade param0, CarryableTrade param1, int param2, int param3, int param4, float param5, int param6) {
        this.cost = param0;
        this.result = param1;
        this.uses = param2;
        this.maxUses = param3;
        this.xp = param4;
        this.priceMultiplier = param5;
        this.demand = param6;
    }

    public CarryableTrade getCost() {
        return this.cost;
    }

    public CarryableTrade getResult() {
        return this.result;
    }

    public void updateDemand() {
        this.demand = this.demand + this.uses - (this.maxUses - this.uses);
    }

    public ItemStack getResultItemStack() {
        return this.result.asItemStack();
    }

    public int getUses() {
        return this.uses;
    }

    public void resetUses() {
        this.uses = 0;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public void increaseUses() {
        ++this.uses;
    }

    public int getDemand() {
        return this.demand;
    }

    public float getPriceMultiplier() {
        return this.priceMultiplier;
    }

    public int getXp() {
        return this.xp;
    }

    public boolean isOutOfStock() {
        return this.uses >= this.maxUses;
    }

    public void setToOutOfStock() {
        this.uses = this.maxUses;
    }

    public boolean needsRestock() {
        return this.uses > 0;
    }

    public boolean shouldRewardExp() {
        return this.rewardExp;
    }

    public CompoundTag createTag() {
        CompoundTag var0 = new CompoundTag();
        CarryableTrade.CODEC.encodeStart(NbtOps.INSTANCE, this.cost).result().ifPresent(param1 -> var0.put("buy", param1));
        CarryableTrade.CODEC.encodeStart(NbtOps.INSTANCE, this.result).result().ifPresent(param1 -> var0.put("sell", param1));
        var0.putInt("uses", this.uses);
        var0.putInt("maxUses", this.maxUses);
        var0.putBoolean("rewardExp", this.rewardExp);
        var0.putInt("xp", this.xp);
        var0.putFloat("priceMultiplier", this.priceMultiplier);
        var0.putInt("demand", this.demand);
        return var0;
    }

    public boolean accepts(CarryableTrade param0) {
        return this.cost.matches(param0);
    }
}
