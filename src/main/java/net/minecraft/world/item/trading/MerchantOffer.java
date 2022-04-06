package net.minecraft.world.item.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class MerchantOffer {
    private final ItemStack baseCostA;
    private final ItemStack costB;
    private final ItemStack result;
    private int uses;
    private final int maxUses;
    private boolean rewardExp = true;
    private int specialPriceDiff;
    private int demand;
    private float priceMultiplier;
    private int xp = 1;

    public MerchantOffer(CompoundTag param0) {
        this.baseCostA = ItemStack.of(param0.getCompound("buy"));
        this.costB = ItemStack.of(param0.getCompound("buyB"));
        this.result = ItemStack.of(param0.getCompound("sell"));
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

        this.specialPriceDiff = param0.getInt("specialPrice");
        this.demand = param0.getInt("demand");
    }

    public MerchantOffer(ItemStack param0, ItemStack param1, int param2, int param3, float param4) {
        this(param0, ItemStack.EMPTY, param1, param2, param3, param4);
    }

    public MerchantOffer(ItemStack param0, ItemStack param1, ItemStack param2, int param3, int param4, float param5) {
        this(param0, param1, param2, 0, param3, param4, param5);
    }

    public MerchantOffer(ItemStack param0, ItemStack param1, ItemStack param2, int param3, int param4, int param5, float param6) {
        this(param0, param1, param2, param3, param4, param5, param6, 0);
    }

    public MerchantOffer(ItemStack param0, ItemStack param1, ItemStack param2, int param3, int param4, int param5, float param6, int param7) {
        this.baseCostA = param0;
        this.costB = param1;
        this.result = param2;
        this.uses = param3;
        this.maxUses = param4;
        this.xp = param5;
        this.priceMultiplier = param6;
        this.demand = param7;
    }

    public ItemStack getBaseCostA() {
        return this.baseCostA;
    }

    public ItemStack getCostA() {
        int var0 = this.baseCostA.getCount();
        ItemStack var1 = this.baseCostA.copy();
        int var2 = Math.max(0, Mth.floor((float)(var0 * this.demand) * this.priceMultiplier));
        var1.setCount(Mth.clamp(var0 + var2 + this.specialPriceDiff, 1, this.baseCostA.getItem().getMaxStackSize()));
        return var1;
    }

    public ItemStack getCostB() {
        return this.costB;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public void updateDemand() {
        this.demand = this.demand + this.uses - (this.maxUses - this.uses);
    }

    public ItemStack assemble() {
        return this.result.copy();
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

    public void addToSpecialPriceDiff(int param0) {
        this.specialPriceDiff += param0;
    }

    public void resetSpecialPriceDiff() {
        this.specialPriceDiff = 0;
    }

    public int getSpecialPriceDiff() {
        return this.specialPriceDiff;
    }

    public void setSpecialPriceDiff(int param0) {
        this.specialPriceDiff = param0;
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
        var0.put("buy", this.baseCostA.save(new CompoundTag()));
        var0.put("sell", this.result.save(new CompoundTag()));
        var0.put("buyB", this.costB.save(new CompoundTag()));
        var0.putInt("uses", this.uses);
        var0.putInt("maxUses", this.maxUses);
        var0.putBoolean("rewardExp", this.rewardExp);
        var0.putInt("xp", this.xp);
        var0.putFloat("priceMultiplier", this.priceMultiplier);
        var0.putInt("specialPrice", this.specialPriceDiff);
        var0.putInt("demand", this.demand);
        return var0;
    }

    public boolean satisfiedBy(ItemStack param0, ItemStack param1) {
        return this.isRequiredItem(param0, this.getCostA())
            && param0.getCount() >= this.getCostA().getCount()
            && this.isRequiredItem(param1, this.costB)
            && param1.getCount() >= this.costB.getCount();
    }

    private boolean isRequiredItem(ItemStack param0, ItemStack param1) {
        if (param1.isEmpty() && param0.isEmpty()) {
            return true;
        } else {
            ItemStack var0 = param0.copy();
            if (var0.getItem().canBeDepleted()) {
                var0.setDamageValue(var0.getDamageValue());
            }

            return ItemStack.isSame(var0, param1) && (!param1.hasTag() || var0.hasTag() && NbtUtils.compareNbt(param1.getTag(), var0.getTag(), false));
        }
    }

    public boolean take(ItemStack param0, ItemStack param1) {
        if (!this.satisfiedBy(param0, param1)) {
            return false;
        } else {
            param0.shrink(this.getCostA().getCount());
            if (!this.getCostB().isEmpty()) {
                param1.shrink(this.getCostB().getCount());
            }

            return true;
        }
    }
}
