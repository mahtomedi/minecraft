package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.slf4j.Logger;

public final class ItemStack {
    public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemStack::getItemHolder),
                    Codec.INT.fieldOf("Count").forGetter(ItemStack::getCount),
                    CompoundTag.CODEC.optionalFieldOf("tag").forGetter(param0x -> Optional.ofNullable(param0x.getTag()))
                )
                .apply(param0, ItemStack::new)
    );
    private static final Codec<Item> ITEM_NON_AIR_CODEC = ExtraCodecs.validate(
        BuiltInRegistries.ITEM.byNameCodec(),
        param0 -> param0 == Items.AIR ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success(param0)
    );
    public static final Codec<ItemStack> ADVANCEMENT_ICON_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemStack::getItemHolder),
                    ExtraCodecs.strictOptionalField(TagParser.AS_CODEC, "nbt").forGetter(param0x -> Optional.ofNullable(param0x.getTag()))
                )
                .apply(param0, (param0x, param1) -> new ItemStack(param0x, 1, param1))
    );
    public static final Codec<ItemStack> ITEM_WITH_COUNT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ITEM_NON_AIR_CODEC.fieldOf("item").forGetter(ItemStack::getItem),
                    ExtraCodecs.strictOptionalField(ExtraCodecs.POSITIVE_INT, "count", 1).forGetter(ItemStack::getCount)
                )
                .apply(param0, ItemStack::new)
    );
    public static final Codec<ItemStack> SINGLE_ITEM_CODEC = ITEM_NON_AIR_CODEC.xmap(ItemStack::new, ItemStack::getItem);
    public static final MapCodec<ItemStack> RESULT_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(ItemStack::getItem),
                    Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)
                )
                .apply(param0, ItemStack::new)
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Void)null);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(
        new DecimalFormat("#.##"), param0 -> param0.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );
    public static final String TAG_ENCH = "Enchantments";
    public static final String TAG_DISPLAY = "display";
    public static final String TAG_DISPLAY_NAME = "Name";
    public static final String TAG_LORE = "Lore";
    public static final String TAG_DAMAGE = "Damage";
    public static final String TAG_COLOR = "color";
    private static final String TAG_UNBREAKABLE = "Unbreakable";
    private static final String TAG_REPAIR_COST = "RepairCost";
    private static final String TAG_CAN_DESTROY_BLOCK_LIST = "CanDestroy";
    private static final String TAG_CAN_PLACE_ON_BLOCK_LIST = "CanPlaceOn";
    private static final String TAG_HIDE_FLAGS = "HideFlags";
    private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
    private static final int DONT_HIDE_TOOLTIP = 0;
    private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
    private int count;
    private int popTime;
    @Deprecated
    @Nullable
    private final Item item;
    @Nullable
    private CompoundTag tag;
    @Nullable
    private Entity entityRepresentation;
    @Nullable
    private AdventureModeCheck adventureBreakCheck;
    @Nullable
    private AdventureModeCheck adventurePlaceCheck;

    public Optional<TooltipComponent> getTooltipImage() {
        return this.getItem().getTooltipImage(this);
    }

    public ItemStack(ItemLike param0) {
        this(param0, 1);
    }

    public ItemStack(Holder<Item> param0) {
        this(param0.value(), 1);
    }

    public ItemStack(Holder<Item> param0, int param1, Optional<CompoundTag> param2) {
        this(param0, param1);
        param2.ifPresent(this::setTag);
    }

    public ItemStack(Holder<Item> param0, int param1) {
        this(param0.value(), param1);
    }

    public ItemStack(ItemLike param0, int param1) {
        this.item = param0.asItem();
        this.count = param1;
        if (this.item.canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }

    }

    private ItemStack(@Nullable Void param0) {
        this.item = null;
    }

    private ItemStack(CompoundTag param0) {
        this.item = BuiltInRegistries.ITEM.get(new ResourceLocation(param0.getString("id")));
        this.count = param0.getByte("Count");
        if (param0.contains("tag", 10)) {
            this.tag = param0.getCompound("tag").copy();
            this.getItem().verifyTagAfterLoad(this.tag);
        }

        if (this.getItem().canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }

    }

    public static ItemStack of(CompoundTag param0) {
        try {
            return new ItemStack(param0);
        } catch (RuntimeException var2) {
            LOGGER.debug("Tried to load invalid item: {}", param0, var2);
            return EMPTY;
        }
    }

    public boolean isEmpty() {
        return this == EMPTY || this.item == Items.AIR || this.count <= 0;
    }

    public boolean isItemEnabled(FeatureFlagSet param0) {
        return this.isEmpty() || this.getItem().isEnabled(param0);
    }

    public ItemStack split(int param0) {
        int var0 = Math.min(param0, this.getCount());
        ItemStack var1 = this.copyWithCount(var0);
        this.shrink(var0);
        return var1;
    }

    public ItemStack copyAndClear() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack var0 = this.copy();
            this.setCount(0);
            return var0;
        }
    }

    public Item getItem() {
        return this.isEmpty() ? Items.AIR : this.item;
    }

    public Holder<Item> getItemHolder() {
        return this.getItem().builtInRegistryHolder();
    }

    public boolean is(TagKey<Item> param0) {
        return this.getItem().builtInRegistryHolder().is(param0);
    }

    public boolean is(Item param0) {
        return this.getItem() == param0;
    }

    public boolean is(Predicate<Holder<Item>> param0) {
        return param0.test(this.getItem().builtInRegistryHolder());
    }

    public boolean is(Holder<Item> param0) {
        return this.getItem().builtInRegistryHolder() == param0;
    }

    public boolean is(HolderSet<Item> param0) {
        return param0.contains(this.getItemHolder());
    }

    public Stream<TagKey<Item>> getTags() {
        return this.getItem().builtInRegistryHolder().tags();
    }

    public InteractionResult useOn(UseOnContext param0) {
        Player var0 = param0.getPlayer();
        BlockPos var1 = param0.getClickedPos();
        BlockInWorld var2 = new BlockInWorld(param0.getLevel(), var1, false);
        if (var0 != null
            && !var0.getAbilities().mayBuild
            && !this.hasAdventureModePlaceTagForBlock(param0.getLevel().registryAccess().registryOrThrow(Registries.BLOCK), var2)) {
            return InteractionResult.PASS;
        } else {
            Item var3 = this.getItem();
            InteractionResult var4 = var3.useOn(param0);
            if (var0 != null && var4.shouldAwardStats()) {
                var0.awardStat(Stats.ITEM_USED.get(var3));
            }

            return var4;
        }
    }

    public float getDestroySpeed(BlockState param0) {
        return this.getItem().getDestroySpeed(this, param0);
    }

    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        return this.getItem().use(param0, param1, param2);
    }

    public ItemStack finishUsingItem(Level param0, LivingEntity param1) {
        return this.getItem().finishUsingItem(this, param0, param1);
    }

    public CompoundTag save(CompoundTag param0) {
        ResourceLocation var0 = BuiltInRegistries.ITEM.getKey(this.getItem());
        param0.putString("id", var0 == null ? "minecraft:air" : var0.toString());
        param0.putByte("Count", (byte)this.count);
        if (this.tag != null) {
            param0.put("tag", this.tag.copy());
        }

        return param0;
    }

    public int getMaxStackSize() {
        return this.getItem().getMaxStackSize();
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
    }

    public boolean isDamageableItem() {
        if (!this.isEmpty() && this.getItem().getMaxDamage() > 0) {
            CompoundTag var0 = this.getTag();
            return var0 == null || !var0.getBoolean("Unbreakable");
        } else {
            return false;
        }
    }

    public boolean isDamaged() {
        return this.isDamageableItem() && this.getDamageValue() > 0;
    }

    public int getDamageValue() {
        return this.tag == null ? 0 : this.tag.getInt("Damage");
    }

    public void setDamageValue(int param0) {
        this.getOrCreateTag().putInt("Damage", Math.max(0, param0));
    }

    public int getMaxDamage() {
        return this.getItem().getMaxDamage();
    }

    public boolean hurt(int param0, RandomSource param1, @Nullable ServerPlayer param2) {
        if (!this.isDamageableItem()) {
            return false;
        } else {
            if (param0 > 0) {
                int var0 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
                int var1 = 0;

                for(int var2 = 0; var0 > 0 && var2 < param0; ++var2) {
                    if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(this, var0, param1)) {
                        ++var1;
                    }
                }

                param0 -= var1;
                if (param0 <= 0) {
                    return false;
                }
            }

            if (param2 != null && param0 != 0) {
                CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(param2, this, this.getDamageValue() + param0);
            }

            int var3 = this.getDamageValue() + param0;
            this.setDamageValue(var3);
            return var3 >= this.getMaxDamage();
        }
    }

    public <T extends LivingEntity> void hurtAndBreak(int param0, T param1, Consumer<T> param2) {
        if (!param1.level().isClientSide && (!(param1 instanceof Player) || !((Player)param1).getAbilities().instabuild)) {
            if (this.isDamageableItem()) {
                if (this.hurt(param0, param1.getRandom(), param1 instanceof ServerPlayer ? (ServerPlayer)param1 : null)) {
                    param2.accept(param1);
                    Item var0 = this.getItem();
                    this.shrink(1);
                    if (param1 instanceof Player) {
                        ((Player)param1).awardStat(Stats.ITEM_BROKEN.get(var0));
                    }

                    this.setDamageValue(0);
                }

            }
        }
    }

    public boolean isBarVisible() {
        return this.getItem().isBarVisible(this);
    }

    public int getBarWidth() {
        return this.getItem().getBarWidth(this);
    }

    public int getBarColor() {
        return this.getItem().getBarColor(this);
    }

    public boolean overrideStackedOnOther(Slot param0, ClickAction param1, Player param2) {
        return this.getItem().overrideStackedOnOther(this, param0, param1, param2);
    }

    public boolean overrideOtherStackedOnMe(ItemStack param0, Slot param1, ClickAction param2, Player param3, SlotAccess param4) {
        return this.getItem().overrideOtherStackedOnMe(this, param0, param1, param2, param3, param4);
    }

    public void hurtEnemy(LivingEntity param0, Player param1) {
        Item var0 = this.getItem();
        if (var0.hurtEnemy(this, param0, param1)) {
            param1.awardStat(Stats.ITEM_USED.get(var0));
        }

    }

    public void mineBlock(Level param0, BlockState param1, BlockPos param2, Player param3) {
        Item var0 = this.getItem();
        if (var0.mineBlock(this, param0, param1, param2, param3)) {
            param3.awardStat(Stats.ITEM_USED.get(var0));
        }

    }

    public boolean isCorrectToolForDrops(BlockState param0) {
        return this.getItem().isCorrectToolForDrops(param0);
    }

    public InteractionResult interactLivingEntity(Player param0, LivingEntity param1, InteractionHand param2) {
        return this.getItem().interactLivingEntity(this, param0, param1, param2);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack var0 = new ItemStack(this.getItem(), this.count);
            var0.setPopTime(this.getPopTime());
            if (this.tag != null) {
                var0.tag = this.tag.copy();
            }

            return var0;
        }
    }

    public ItemStack copyWithCount(int param0) {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack var0 = this.copy();
            var0.setCount(param0);
            return var0;
        }
    }

    public static boolean matches(ItemStack param0, ItemStack param1) {
        if (param0 == param1) {
            return true;
        } else {
            return param0.getCount() != param1.getCount() ? false : isSameItemSameTags(param0, param1);
        }
    }

    public static boolean isSameItem(ItemStack param0, ItemStack param1) {
        return param0.is(param1.getItem());
    }

    public static boolean isSameItemSameTags(ItemStack param0, ItemStack param1) {
        if (!param0.is(param1.getItem())) {
            return false;
        } else {
            return param0.isEmpty() && param1.isEmpty() ? true : Objects.equals(param0.tag, param1.tag);
        }
    }

    public String getDescriptionId() {
        return this.getItem().getDescriptionId(this);
    }

    @Override
    public String toString() {
        return this.getCount() + " " + this.getItem();
    }

    public void inventoryTick(Level param0, Entity param1, int param2, boolean param3) {
        if (this.popTime > 0) {
            --this.popTime;
        }

        if (this.getItem() != null) {
            this.getItem().inventoryTick(this, param0, param1, param2, param3);
        }

    }

    public void onCraftedBy(Level param0, Player param1, int param2) {
        param1.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), param2);
        this.getItem().onCraftedBy(this, param0, param1);
    }

    public void onCraftedBySystem(Level param0) {
        this.getItem().onCraftedPostProcess(this, param0);
    }

    public int getUseDuration() {
        return this.getItem().getUseDuration(this);
    }

    public UseAnim getUseAnimation() {
        return this.getItem().getUseAnimation(this);
    }

    public void releaseUsing(Level param0, LivingEntity param1, int param2) {
        this.getItem().releaseUsing(this, param0, param1, param2);
    }

    public boolean useOnRelease() {
        return this.getItem().useOnRelease(this);
    }

    public boolean hasTag() {
        return !this.isEmpty() && this.tag != null && !this.tag.isEmpty();
    }

    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }

    public CompoundTag getOrCreateTag() {
        if (this.tag == null) {
            this.setTag(new CompoundTag());
        }

        return this.tag;
    }

    public CompoundTag getOrCreateTagElement(String param0) {
        if (this.tag != null && this.tag.contains(param0, 10)) {
            return this.tag.getCompound(param0);
        } else {
            CompoundTag var0 = new CompoundTag();
            this.addTagElement(param0, var0);
            return var0;
        }
    }

    @Nullable
    public CompoundTag getTagElement(String param0) {
        return this.tag != null && this.tag.contains(param0, 10) ? this.tag.getCompound(param0) : null;
    }

    public void removeTagKey(String param0) {
        if (this.tag != null && this.tag.contains(param0)) {
            this.tag.remove(param0);
            if (this.tag.isEmpty()) {
                this.tag = null;
            }
        }

    }

    public ListTag getEnchantmentTags() {
        return this.tag != null ? this.tag.getList("Enchantments", 10) : new ListTag();
    }

    public void setTag(@Nullable CompoundTag param0x) {
        this.tag = param0x;
        if (this.getItem().canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }

        if (param0x != null) {
            this.getItem().verifyTagAfterLoad(param0x);
        }

    }

    public Component getHoverName() {
        CompoundTag var0 = this.getTagElement("display");
        if (var0 != null && var0.contains("Name", 8)) {
            try {
                Component var1 = Component.Serializer.fromJson(var0.getString("Name"));
                if (var1 != null) {
                    return var1;
                }

                var0.remove("Name");
            } catch (Exception var3) {
                var0.remove("Name");
            }
        }

        return this.getItem().getName(this);
    }

    public ItemStack setHoverName(@Nullable Component param0) {
        CompoundTag var0 = this.getOrCreateTagElement("display");
        if (param0 != null) {
            var0.putString("Name", Component.Serializer.toJson(param0));
        } else {
            var0.remove("Name");
        }

        return this;
    }

    public void resetHoverName() {
        CompoundTag var0 = this.getTagElement("display");
        if (var0 != null) {
            var0.remove("Name");
            if (var0.isEmpty()) {
                this.removeTagKey("display");
            }
        }

        if (this.tag != null && this.tag.isEmpty()) {
            this.tag = null;
        }

    }

    public boolean hasCustomHoverName() {
        CompoundTag var0 = this.getTagElement("display");
        return var0 != null && var0.contains("Name", 8);
    }

    public List<Component> getTooltipLines(@Nullable Player param0, TooltipFlag param1) {
        List<Component> var0 = Lists.newArrayList();
        MutableComponent var1 = Component.empty().append(this.getHoverName()).withStyle(this.getRarity().color);
        if (this.hasCustomHoverName()) {
            var1.withStyle(ChatFormatting.ITALIC);
        }

        var0.add(var1);
        if (!param1.isAdvanced() && !this.hasCustomHoverName() && this.is(Items.FILLED_MAP)) {
            Integer var2 = MapItem.getMapId(this);
            if (var2 != null) {
                var0.add(MapItem.getTooltipForId(this));
            }
        }

        int var3 = this.getHideFlags();
        if (shouldShowInTooltip(var3, ItemStack.TooltipPart.ADDITIONAL)) {
            this.getItem().appendHoverText(this, param0 == null ? null : param0.level(), var0, param1);
        }

        if (this.hasTag()) {
            if (shouldShowInTooltip(var3, ItemStack.TooltipPart.UPGRADES) && param0 != null) {
                ArmorTrim.appendUpgradeHoverText(this, param0.level().registryAccess(), var0);
            }

            if (shouldShowInTooltip(var3, ItemStack.TooltipPart.ENCHANTMENTS)) {
                appendEnchantmentNames(var0, this.getEnchantmentTags());
            }

            if (this.tag.contains("display", 10)) {
                CompoundTag var4 = this.tag.getCompound("display");
                if (shouldShowInTooltip(var3, ItemStack.TooltipPart.DYE) && var4.contains("color", 99)) {
                    if (param1.isAdvanced()) {
                        var0.add(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", var4.getInt("color"))).withStyle(ChatFormatting.GRAY));
                    } else {
                        var0.add(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    }
                }

                if (var4.getTagType("Lore") == 9) {
                    ListTag var5 = var4.getList("Lore", 8);

                    for(int var6 = 0; var6 < var5.size(); ++var6) {
                        String var7 = var5.getString(var6);

                        try {
                            MutableComponent var8 = Component.Serializer.fromJson(var7);
                            if (var8 != null) {
                                var0.add(ComponentUtils.mergeStyles(var8, LORE_STYLE));
                            }
                        } catch (Exception var19) {
                            var4.remove("Lore");
                        }
                    }
                }
            }
        }

        if (shouldShowInTooltip(var3, ItemStack.TooltipPart.MODIFIERS)) {
            for(EquipmentSlot var10 : EquipmentSlot.values()) {
                Multimap<Attribute, AttributeModifier> var11 = this.getAttributeModifiers(var10);
                if (!var11.isEmpty()) {
                    var0.add(CommonComponents.EMPTY);
                    var0.add(Component.translatable("item.modifiers." + var10.getName()).withStyle(ChatFormatting.GRAY));

                    for(Entry<Attribute, AttributeModifier> var12 : var11.entries()) {
                        AttributeModifier var13 = var12.getValue();
                        double var14 = var13.getAmount();
                        boolean var15 = false;
                        if (param0 != null) {
                            if (var13.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
                                var14 += param0.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                                var14 += (double)EnchantmentHelper.getDamageBonus(this, MobType.UNDEFINED);
                                var15 = true;
                            } else if (var13.getId() == Item.BASE_ATTACK_SPEED_UUID) {
                                var14 += param0.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                                var15 = true;
                            }
                        }

                        double var16;
                        if (var13.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
                            || var13.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                            var16 = var14 * 100.0;
                        } else if (var12.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                            var16 = var14 * 10.0;
                        } else {
                            var16 = var14;
                        }

                        if (var15) {
                            var0.add(
                                CommonComponents.space()
                                    .append(
                                        Component.translatable(
                                            "attribute.modifier.equals." + var13.getOperation().toValue(),
                                            ATTRIBUTE_MODIFIER_FORMAT.format(var16),
                                            Component.translatable(var12.getKey().getDescriptionId())
                                        )
                                    )
                                    .withStyle(ChatFormatting.DARK_GREEN)
                            );
                        } else if (var14 > 0.0) {
                            var0.add(
                                Component.translatable(
                                        "attribute.modifier.plus." + var13.getOperation().toValue(),
                                        ATTRIBUTE_MODIFIER_FORMAT.format(var16),
                                        Component.translatable(var12.getKey().getDescriptionId())
                                    )
                                    .withStyle(ChatFormatting.BLUE)
                            );
                        } else if (var14 < 0.0) {
                            var16 *= -1.0;
                            var0.add(
                                Component.translatable(
                                        "attribute.modifier.take." + var13.getOperation().toValue(),
                                        ATTRIBUTE_MODIFIER_FORMAT.format(var16),
                                        Component.translatable(var12.getKey().getDescriptionId())
                                    )
                                    .withStyle(ChatFormatting.RED)
                            );
                        }
                    }
                }
            }
        }

        if (this.hasTag()) {
            if (shouldShowInTooltip(var3, ItemStack.TooltipPart.UNBREAKABLE) && this.tag.getBoolean("Unbreakable")) {
                var0.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
            }

            if (shouldShowInTooltip(var3, ItemStack.TooltipPart.CAN_DESTROY) && this.tag.contains("CanDestroy", 9)) {
                ListTag var19 = this.tag.getList("CanDestroy", 8);
                if (!var19.isEmpty()) {
                    var0.add(CommonComponents.EMPTY);
                    var0.add(Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY));

                    for(int var20 = 0; var20 < var19.size(); ++var20) {
                        var0.addAll(expandBlockState(var19.getString(var20)));
                    }
                }
            }

            if (shouldShowInTooltip(var3, ItemStack.TooltipPart.CAN_PLACE) && this.tag.contains("CanPlaceOn", 9)) {
                ListTag var21 = this.tag.getList("CanPlaceOn", 8);
                if (!var21.isEmpty()) {
                    var0.add(CommonComponents.EMPTY);
                    var0.add(Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY));

                    for(int var22 = 0; var22 < var21.size(); ++var22) {
                        var0.addAll(expandBlockState(var21.getString(var22)));
                    }
                }
            }
        }

        if (param1.isAdvanced()) {
            if (this.isDamaged()) {
                var0.add(Component.translatable("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
            }

            var0.add(Component.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (this.hasTag()) {
                var0.add(Component.translatable("item.nbt_tags", this.tag.getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (param0 != null && !this.getItem().isEnabled(param0.level().enabledFeatures())) {
            var0.add(DISABLED_ITEM_TOOLTIP);
        }

        return var0;
    }

    private static boolean shouldShowInTooltip(int param0, ItemStack.TooltipPart param1) {
        return (param0 & param1.getMask()) == 0;
    }

    private int getHideFlags() {
        return this.hasTag() && this.tag.contains("HideFlags", 99) ? this.tag.getInt("HideFlags") : 0;
    }

    public void hideTooltipPart(ItemStack.TooltipPart param0) {
        CompoundTag var0 = this.getOrCreateTag();
        var0.putInt("HideFlags", var0.getInt("HideFlags") | param0.getMask());
    }

    public static void appendEnchantmentNames(List<Component> param0, ListTag param1) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            CompoundTag var1 = param1.getCompound(var0);
            BuiltInRegistries.ENCHANTMENT
                .getOptional(EnchantmentHelper.getEnchantmentId(var1))
                .ifPresent(param2 -> param0.add(param2.getFullname(EnchantmentHelper.getEnchantmentLevel(var1))));
        }

    }

    private static Collection<Component> expandBlockState(String param0) {
        try {
            return BlockStateParser.parseForTesting(BuiltInRegistries.BLOCK.asLookup(), param0, true)
                .map(
                    param0x -> Lists.newArrayList(param0x.blockState().getBlock().getName().withStyle(ChatFormatting.DARK_GRAY)),
                    param0x -> param0x.tag()
                            .stream()
                            .map(param0xx -> param0xx.value().getName().withStyle(ChatFormatting.DARK_GRAY))
                            .collect(Collectors.toList())
                );
        } catch (CommandSyntaxException var2) {
            return Lists.newArrayList(Component.literal("missingno").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public boolean hasFoil() {
        return this.getItem().isFoil(this);
    }

    public Rarity getRarity() {
        return this.getItem().getRarity(this);
    }

    public boolean isEnchantable() {
        if (!this.getItem().isEnchantable(this)) {
            return false;
        } else {
            return !this.isEnchanted();
        }
    }

    public void enchant(Enchantment param0, int param1) {
        this.getOrCreateTag();
        if (!this.tag.contains("Enchantments", 9)) {
            this.tag.put("Enchantments", new ListTag());
        }

        ListTag var0 = this.tag.getList("Enchantments", 10);
        var0.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(param0), (byte)param1));
    }

    public boolean isEnchanted() {
        if (this.tag != null && this.tag.contains("Enchantments", 9)) {
            return !this.tag.getList("Enchantments", 10).isEmpty();
        } else {
            return false;
        }
    }

    public void addTagElement(String param0, Tag param1) {
        this.getOrCreateTag().put(param0, param1);
    }

    public boolean isFramed() {
        return this.entityRepresentation instanceof ItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity param0) {
        this.entityRepresentation = param0;
    }

    @Nullable
    public ItemFrame getFrame() {
        return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
    }

    @Nullable
    public Entity getEntityRepresentation() {
        return !this.isEmpty() ? this.entityRepresentation : null;
    }

    public int getBaseRepairCost() {
        return this.hasTag() && this.tag.contains("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
    }

    public void setRepairCost(int param0) {
        if (param0 > 0) {
            this.getOrCreateTag().putInt("RepairCost", param0);
        } else {
            this.removeTagKey("RepairCost");
        }

    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot param0) {
        Multimap<Attribute, AttributeModifier> var0;
        if (this.hasTag() && this.tag.contains("AttributeModifiers", 9)) {
            var0 = HashMultimap.create();
            ListTag var1 = this.tag.getList("AttributeModifiers", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                CompoundTag var3 = var1.getCompound(var2);
                if (!var3.contains("Slot", 8) || var3.getString("Slot").equals(param0.getName())) {
                    Optional<Attribute> var4 = BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.tryParse(var3.getString("AttributeName")));
                    if (!var4.isEmpty()) {
                        AttributeModifier var5 = AttributeModifier.load(var3);
                        if (var5 != null && var5.getId().getLeastSignificantBits() != 0L && var5.getId().getMostSignificantBits() != 0L) {
                            var0.put(var4.get(), var5);
                        }
                    }
                }
            }
        } else {
            var0 = this.getItem().getDefaultAttributeModifiers(param0);
        }

        return var0;
    }

    public void addAttributeModifier(Attribute param0, AttributeModifier param1, @Nullable EquipmentSlot param2) {
        this.getOrCreateTag();
        if (!this.tag.contains("AttributeModifiers", 9)) {
            this.tag.put("AttributeModifiers", new ListTag());
        }

        ListTag var0 = this.tag.getList("AttributeModifiers", 10);
        CompoundTag var1 = param1.save();
        var1.putString("AttributeName", BuiltInRegistries.ATTRIBUTE.getKey(param0).toString());
        if (param2 != null) {
            var1.putString("Slot", param2.getName());
        }

        var0.add(var1);
    }

    public Component getDisplayName() {
        MutableComponent var0 = Component.empty().append(this.getHoverName());
        if (this.hasCustomHoverName()) {
            var0.withStyle(ChatFormatting.ITALIC);
        }

        MutableComponent var1 = ComponentUtils.wrapInSquareBrackets(var0);
        if (!this.isEmpty()) {
            var1.withStyle(this.getRarity().color)
                .withStyle(param0 -> param0.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this))));
        }

        return var1;
    }

    public boolean hasAdventureModePlaceTagForBlock(Registry<Block> param0, BlockInWorld param1) {
        if (this.adventurePlaceCheck == null) {
            this.adventurePlaceCheck = new AdventureModeCheck("CanPlaceOn");
        }

        return this.adventurePlaceCheck.test(this, param0, param1);
    }

    public boolean hasAdventureModeBreakTagForBlock(Registry<Block> param0, BlockInWorld param1) {
        if (this.adventureBreakCheck == null) {
            this.adventureBreakCheck = new AdventureModeCheck("CanDestroy");
        }

        return this.adventureBreakCheck.test(this, param0, param1);
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int param0) {
        this.popTime = param0;
    }

    public int getCount() {
        return this.isEmpty() ? 0 : this.count;
    }

    public void setCount(int param0) {
        this.count = param0;
    }

    public void grow(int param0) {
        this.setCount(this.getCount() + param0);
    }

    public void shrink(int param0) {
        this.grow(-param0);
    }

    public void onUseTick(Level param0, LivingEntity param1, int param2) {
        this.getItem().onUseTick(param0, param1, this, param2);
    }

    public void onDestroyed(ItemEntity param0) {
        this.getItem().onDestroyed(param0);
    }

    public boolean isEdible() {
        return this.getItem().isEdible();
    }

    public SoundEvent getDrinkingSound() {
        return this.getItem().getDrinkingSound();
    }

    public SoundEvent getEatingSound() {
        return this.getItem().getEatingSound();
    }

    public static enum TooltipPart {
        ENCHANTMENTS,
        MODIFIERS,
        UNBREAKABLE,
        CAN_DESTROY,
        CAN_PLACE,
        ADDITIONAL,
        DYE,
        UPGRADES;

        private final int mask = 1 << this.ordinal();

        public int getMask() {
            return this.mask;
        }
    }
}
