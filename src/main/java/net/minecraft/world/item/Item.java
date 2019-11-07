package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Item implements ItemLike {
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    private static final ItemPropertyFunction PROPERTY_DAMAGED = (param0, param1, param2) -> param0.isDamaged() ? 1.0F : 0.0F;
    private static final ItemPropertyFunction PROPERTY_DAMAGE = (param0, param1, param2) -> Mth.clamp(
            (float)param0.getDamageValue() / (float)param0.getMaxDamage(), 0.0F, 1.0F
        );
    private static final ItemPropertyFunction PROPERTY_LEFTHANDED = (param0, param1, param2) -> param2 != null && param2.getMainArm() != HumanoidArm.RIGHT
            ? 1.0F
            : 0.0F;
    private static final ItemPropertyFunction PROPERTY_COOLDOWN = (param0, param1, param2) -> param2 instanceof Player
            ? ((Player)param2).getCooldowns().getCooldownPercent(param0.getItem(), 0.0F)
            : 0.0F;
    private static final ItemPropertyFunction PROPERTY_CUSTOM_MODEL_DATA = (param0, param1, param2) -> param0.hasTag()
            ? (float)param0.getTag().getInt("CustomModelData")
            : 0.0F;
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    protected static final Random random = new Random();
    private final Map<ResourceLocation, ItemPropertyFunction> properties = Maps.newHashMap();
    protected final CreativeModeTab category;
    private final Rarity rarity;
    private final int maxStackSize;
    private final int maxDamage;
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
        this.addProperty(new ResourceLocation("lefthanded"), PROPERTY_LEFTHANDED);
        this.addProperty(new ResourceLocation("cooldown"), PROPERTY_COOLDOWN);
        this.addProperty(new ResourceLocation("custom_model_data"), PROPERTY_CUSTOM_MODEL_DATA);
        this.category = param0.category;
        this.rarity = param0.rarity;
        this.craftingRemainingItem = param0.craftingRemainingItem;
        this.maxDamage = param0.maxDamage;
        this.maxStackSize = param0.maxStackSize;
        this.foodProperties = param0.foodProperties;
        if (this.maxDamage > 0) {
            this.addProperty(new ResourceLocation("damaged"), PROPERTY_DAMAGED);
            this.addProperty(new ResourceLocation("damage"), PROPERTY_DAMAGE);
        }

    }

    public void onUseTick(Level param0, LivingEntity param1, ItemStack param2, int param3) {
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public ItemPropertyFunction getProperty(ResourceLocation param0) {
        return this.properties.get(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasProperties() {
        return !this.properties.isEmpty();
    }

    public boolean verifyTagAfterLoad(CompoundTag param0) {
        return false;
    }

    public boolean canAttackBlock(BlockState param0, Level param1, BlockPos param2, Player param3) {
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public final void addProperty(ResourceLocation param0, ItemPropertyFunction param1) {
        this.properties.put(param0, param1);
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
                return InteractionResultHolder.success(var0);
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

    public boolean hurtEnemy(ItemStack param0, LivingEntity param1, LivingEntity param2) {
        return false;
    }

    public boolean mineBlock(ItemStack param0, Level param1, BlockState param2, BlockPos param3, LivingEntity param4) {
        return false;
    }

    public boolean canDestroySpecial(BlockState param0) {
        return false;
    }

    public boolean interactEnemy(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDescription() {
        return new TranslatableComponent(this.getDescriptionId());
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

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
    }

    public Component getName(ItemStack param0) {
        return new TranslatableComponent(this.getDescriptionId(param0));
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

    protected static HitResult getPlayerPOVHitResult(Level param0, Player param1, ClipContext.Fluid param2) {
        float var0 = param1.xRot;
        float var1 = param1.yRot;
        Vec3 var2 = param1.getEyePosition(1.0F);
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

    public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        return HashMultimap.create();
    }

    public boolean useOnRelease(ItemStack param0) {
        return param0.getItem() == Items.CROSSBOW;
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public boolean is(Tag<Item> param0) {
        return param0.contains(this);
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

    public static class Properties {
        private int maxStackSize = 64;
        private int maxDamage;
        private Item craftingRemainingItem;
        private CreativeModeTab category;
        private Rarity rarity = Rarity.COMMON;
        private FoodProperties foodProperties;

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
    }
}
