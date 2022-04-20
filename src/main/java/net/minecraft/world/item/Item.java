package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item implements ItemLike {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final int MAX_STACK_SIZE = 64;
    public static final int EAT_DURATION = 32;
    public static final int MAX_BAR_WIDTH = 13;
    private final Holder.Reference<Item> builtInRegistryHolder = Registry.ITEM.createIntrusiveHolder(this);
    @Nullable
    protected final CreativeModeTab category;
    private final Rarity rarity;
    private final int maxStackSize;
    private final int maxDamage;
    private final boolean isFireResistant;
    @Nullable
    private final Item craftingRemainingItem;
    @Nullable
    private String descriptionId;
    @Nullable
    private final FoodProperties foodProperties;

    public static int getId(Item param0) {
        return param0 == null ? 0 : Registry.ITEM.getId(param0);
    }

    public static Item byId(int param0) {
        return Registry.ITEM.byId(param0);
    }

    @Deprecated
    public static Item byBlock(Block param0) {
        return BY_BLOCK.getOrDefault(param0, Items.AIR);
    }

    public Item(Item.Properties param0) {
        this.category = param0.category;
        this.rarity = param0.rarity;
        this.craftingRemainingItem = param0.craftingRemainingItem;
        this.maxDamage = param0.maxDamage;
        this.maxStackSize = param0.maxStackSize;
        this.foodProperties = param0.foodProperties;
        this.isFireResistant = param0.isFireResistant;
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            String var0 = this.getClass().getSimpleName();
            if (!var0.endsWith("Item")) {
                LOGGER.error("Item classes should end with Item and {} doesn't.", var0);
            }
        }

    }

    @Deprecated
    public Holder.Reference<Item> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public void onUseTick(Level param0, LivingEntity param1, ItemStack param2, int param3) {
    }

    public void onDestroyed(ItemEntity param0) {
    }

    public void verifyTagAfterLoad(CompoundTag param0) {
    }

    public boolean canAttackBlock(BlockState param0, Level param1, BlockPos param2, Player param3) {
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public InteractionResult useOn(UseOnContext param0) {
        return InteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack param0, BlockState param1) {
        return 1.0F;
    }

    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        if (this.isEdible()) {
            ItemStack var0 = param1.getItemInHand(param2);
            if (param1.canEat(this.getFoodProperties().canAlwaysEat())) {
                param1.startUsingItem(param2);
                return InteractionResultHolder.consume(var0);
            } else {
                return InteractionResultHolder.fail(var0);
            }
        } else {
            return InteractionResultHolder.pass(param1.getItemInHand(param2));
        }
    }

    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        return this.isEdible() ? param2.eat(param1, param0) : param0;
    }

    public final int getMaxStackSize() {
        return this.maxStackSize;
    }

    public final int getMaxDamage() {
        return this.maxDamage;
    }

    public boolean canBeDepleted() {
        return this.maxDamage > 0;
    }

    public boolean isBarVisible(ItemStack param0) {
        return param0.isDamaged();
    }

    public int getBarWidth(ItemStack param0) {
        return Math.round(13.0F - (float)param0.getDamageValue() * 13.0F / (float)this.maxDamage);
    }

    public int getBarColor(ItemStack param0) {
        float var0 = Math.max(0.0F, ((float)this.maxDamage - (float)param0.getDamageValue()) / (float)this.maxDamage);
        return Mth.hsvToRgb(var0 / 3.0F, 1.0F, 1.0F);
    }

    public boolean overrideStackedOnOther(ItemStack param0, Slot param1, ClickAction param2, Player param3) {
        return false;
    }

    public boolean overrideOtherStackedOnMe(ItemStack param0, ItemStack param1, Slot param2, ClickAction param3, Player param4, SlotAccess param5) {
        return false;
    }

    public boolean hurtEnemy(ItemStack param0, LivingEntity param1, LivingEntity param2) {
        return false;
    }

    public boolean mineBlock(ItemStack param0, Level param1, BlockState param2, BlockPos param3, LivingEntity param4) {
        return false;
    }

    public boolean isCorrectToolForDrops(BlockState param0) {
        return false;
    }

    public InteractionResult interactLivingEntity(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        return InteractionResult.PASS;
    }

    public Component getDescription() {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public String toString() {
        return Registry.ITEM.getKey(this).getPath();
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("item", Registry.ITEM.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public String getDescriptionId(ItemStack param0) {
        return this.getDescriptionId();
    }

    public boolean shouldOverrideMultiplayerNbt() {
        return true;
    }

    @Nullable
    public final Item getCraftingRemainingItem() {
        return this.craftingRemainingItem;
    }

    public boolean hasCraftingRemainingItem() {
        return this.craftingRemainingItem != null;
    }

    public void inventoryTick(ItemStack param0, Level param1, Entity param2, int param3, boolean param4) {
    }

    public void onCraftedBy(ItemStack param0, Level param1, Player param2) {
    }

    public boolean isComplex() {
        return false;
    }

    public UseAnim getUseAnimation(ItemStack param0) {
        return param0.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
    }

    public int getUseDuration(ItemStack param0) {
        if (param0.getItem().isEdible()) {
            return this.getFoodProperties().isFastFood() ? 16 : 32;
        } else {
            return 0;
        }
    }

    public void releaseUsing(ItemStack param0, Level param1, LivingEntity param2, int param3) {
    }

    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack param0) {
        return Optional.empty();
    }

    public Component getName(ItemStack param0) {
        return Component.translatable(this.getDescriptionId(param0));
    }

    public boolean isFoil(ItemStack param0) {
        return param0.isEnchanted();
    }

    public Rarity getRarity(ItemStack param0) {
        if (!param0.isEnchanted()) {
            return this.rarity;
        } else {
            switch(this.rarity) {
                case COMMON:
                case UNCOMMON:
                    return Rarity.RARE;
                case RARE:
                    return Rarity.EPIC;
                case EPIC:
                default:
                    return this.rarity;
            }
        }
    }

    public boolean isEnchantable(ItemStack param0) {
        return this.getMaxStackSize() == 1 && this.canBeDepleted();
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level param0, Player param1, ClipContext.Fluid param2) {
        float var0 = param1.getXRot();
        float var1 = param1.getYRot();
        Vec3 var2 = param1.getEyePosition();
        float var3 = Mth.cos(-var1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var4 = Mth.sin(-var1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var5 = -Mth.cos(-var0 * (float) (Math.PI / 180.0));
        float var6 = Mth.sin(-var0 * (float) (Math.PI / 180.0));
        float var7 = var4 * var5;
        float var9 = var3 * var5;
        double var10 = 5.0;
        Vec3 var11 = var2.add((double)var7 * 5.0, (double)var6 * 5.0, (double)var9 * 5.0);
        return param0.clip(new ClipContext(var2, var11, ClipContext.Block.OUTLINE, param2, param1));
    }

    public int getEnchantmentValue() {
        return 0;
    }

    public void fillItemCategory(CreativeModeTab param0, NonNullList<ItemStack> param1) {
        if (this.allowdedIn(param0)) {
            param1.add(new ItemStack(this));
        }

    }

    protected boolean allowdedIn(CreativeModeTab param0) {
        CreativeModeTab var0 = this.getItemCategory();
        return var0 != null && (param0 == CreativeModeTab.TAB_SEARCH || param0 == var0);
    }

    @Nullable
    public final CreativeModeTab getItemCategory() {
        return this.category;
    }

    public boolean isValidRepairItem(ItemStack param0, ItemStack param1) {
        return false;
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        return ImmutableMultimap.of();
    }

    public boolean useOnRelease(ItemStack param0) {
        return false;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public boolean isEdible() {
        return this.foodProperties != null;
    }

    @Nullable
    public FoodProperties getFoodProperties() {
        return this.foodProperties;
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_EAT;
    }

    public boolean isFireResistant() {
        return this.isFireResistant;
    }

    public boolean canBeHurtBy(DamageSource param0) {
        return !this.isFireResistant || !param0.isFire();
    }

    @Nullable
    public SoundEvent getEquipSound() {
        return null;
    }

    public boolean canFitInsideContainerItems() {
        return true;
    }

    public static class Properties {
        int maxStackSize = 64;
        int maxDamage;
        @Nullable
        Item craftingRemainingItem;
        @Nullable
        CreativeModeTab category;
        Rarity rarity = Rarity.COMMON;
        @Nullable
        FoodProperties foodProperties;
        boolean isFireResistant;

        public Item.Properties food(FoodProperties param0) {
            this.foodProperties = param0;
            return this;
        }

        public Item.Properties stacksTo(int param0) {
            if (this.maxDamage > 0) {
                throw new RuntimeException("Unable to have damage AND stack.");
            } else {
                this.maxStackSize = param0;
                return this;
            }
        }

        public Item.Properties defaultDurability(int param0) {
            return this.maxDamage == 0 ? this.durability(param0) : this;
        }

        public Item.Properties durability(int param0) {
            this.maxDamage = param0;
            this.maxStackSize = 1;
            return this;
        }

        public Item.Properties craftRemainder(Item param0) {
            this.craftingRemainingItem = param0;
            return this;
        }

        public Item.Properties tab(CreativeModeTab param0) {
            this.category = param0;
            return this;
        }

        public Item.Properties rarity(Rarity param0) {
            this.rarity = param0;
            return this;
        }

        public Item.Properties fireResistant() {
            this.isFireResistant = true;
            return this;
        }
    }
}
