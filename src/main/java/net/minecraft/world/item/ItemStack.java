package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ItemStack {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Item)null);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = getAttributeDecimalFormat();
    private int count;
    private int popTime;
    @Deprecated
    private final Item item;
    private CompoundTag tag;
    private boolean emptyCacheFlag;
    private ItemFrame frame;
    private BlockInWorld cachedBreakBlock;
    private boolean cachedBreakBlockResult;
    private BlockInWorld cachedPlaceBlock;
    private boolean cachedPlaceBlockResult;

    private static DecimalFormat getAttributeDecimalFormat() {
        DecimalFormat var0 = new DecimalFormat("#.##");
        var0.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        return var0;
    }

    public ItemStack(ItemLike param0) {
        this(param0, 1);
    }

    public ItemStack(ItemLike param0, int param1) {
        this.item = param0 == null ? null : param0.asItem();
        this.count = param1;
        this.updateEmptyCacheFlag();
    }

    private void updateEmptyCacheFlag() {
        this.emptyCacheFlag = false;
        this.emptyCacheFlag = this.isEmpty();
    }

    private ItemStack(CompoundTag param0) {
        this.item = Registry.ITEM.get(new ResourceLocation(param0.getString("id")));
        this.count = param0.getByte("Count");
        if (param0.contains("tag", 10)) {
            this.tag = param0.getCompound("tag");
            this.getItem().verifyTagAfterLoad(param0);
        }

        if (this.getItem().canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }

        this.updateEmptyCacheFlag();
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
        if (this == EMPTY) {
            return true;
        } else if (this.getItem() == null || this.getItem() == Items.AIR) {
            return true;
        } else {
            return this.count <= 0;
        }
    }

    public ItemStack split(int param0) {
        int var0 = Math.min(param0, this.count);
        ItemStack var1 = this.copy();
        var1.setCount(var0);
        this.shrink(var0);
        return var1;
    }

    public Item getItem() {
        return this.emptyCacheFlag ? Items.AIR : this.item;
    }

    public InteractionResult useOn(UseOnContext param0) {
        Player var0 = param0.getPlayer();
        BlockPos var1 = param0.getClickedPos();
        BlockInWorld var2 = new BlockInWorld(param0.getLevel(), var1, false);
        if (var0 != null && !var0.abilities.mayBuild && !this.hasAdventureModePlaceTagForBlock(param0.getLevel().getTagManager(), var2)) {
            return InteractionResult.PASS;
        } else {
            Item var3 = this.getItem();
            InteractionResult var4 = var3.useOn(param0);
            if (var0 != null && var4 == InteractionResult.SUCCESS) {
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
        ResourceLocation var0 = Registry.ITEM.getKey(this.getItem());
        param0.putString("id", var0 == null ? "minecraft:air" : var0.toString());
        param0.putByte("Count", (byte)this.count);
        if (this.tag != null) {
            param0.put("tag", this.tag);
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
        if (!this.emptyCacheFlag && this.getItem().getMaxDamage() > 0) {
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

    public boolean hurt(int param0, Random param1, @Nullable ServerPlayer param2) {
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
        if (!param1.level.isClientSide && (!(param1 instanceof Player) || !((Player)param1).abilities.instabuild)) {
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

    public boolean canDestroySpecial(BlockState param0) {
        return this.getItem().canDestroySpecial(param0);
    }

    public boolean interactEnemy(Player param0, LivingEntity param1, InteractionHand param2) {
        return this.getItem().interactEnemy(this, param0, param1, param2);
    }

    public ItemStack copy() {
        ItemStack var0 = new ItemStack(this.getItem(), this.count);
        var0.setPopTime(this.getPopTime());
        if (this.tag != null) {
            var0.tag = this.tag.copy();
        }

        return var0;
    }

    public static boolean tagMatches(ItemStack param0, ItemStack param1) {
        if (param0.isEmpty() && param1.isEmpty()) {
            return true;
        } else if (param0.isEmpty() || param1.isEmpty()) {
            return false;
        } else if (param0.tag == null && param1.tag != null) {
            return false;
        } else {
            return param0.tag == null || param0.tag.equals(param1.tag);
        }
    }

    public static boolean matches(ItemStack param0, ItemStack param1) {
        if (param0.isEmpty() && param1.isEmpty()) {
            return true;
        } else {
            return !param0.isEmpty() && !param1.isEmpty() ? param0.matches(param1) : false;
        }
    }

    private boolean matches(ItemStack param0) {
        if (this.count != param0.count) {
            return false;
        } else if (this.getItem() != param0.getItem()) {
            return false;
        } else if (this.tag == null && param0.tag != null) {
            return false;
        } else {
            return this.tag == null || this.tag.equals(param0.tag);
        }
    }

    public static boolean isSame(ItemStack param0, ItemStack param1) {
        if (param0 == param1) {
            return true;
        } else {
            return !param0.isEmpty() && !param1.isEmpty() ? param0.sameItem(param1) : false;
        }
    }

    public static boolean isSameIgnoreDurability(ItemStack param0, ItemStack param1) {
        if (param0 == param1) {
            return true;
        } else {
            return !param0.isEmpty() && !param1.isEmpty() ? param0.sameItemStackIgnoreDurability(param1) : false;
        }
    }

    public boolean sameItem(ItemStack param0) {
        return !param0.isEmpty() && this.getItem() == param0.getItem();
    }

    public boolean sameItemStackIgnoreDurability(ItemStack param0) {
        if (!this.isDamageableItem()) {
            return this.sameItem(param0);
        } else {
            return !param0.isEmpty() && this.getItem() == param0.getItem();
        }
    }

    public String getDescriptionId() {
        return this.getItem().getDescriptionId(this);
    }

    @Override
    public String toString() {
        return this.count + " " + this.getItem();
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
        return !this.emptyCacheFlag && this.tag != null && !this.tag.isEmpty();
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

    public void setTag(@Nullable CompoundTag param0) {
        this.tag = param0;
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
            } catch (JsonParseException var3) {
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

    @OnlyIn(Dist.CLIENT)
    public List<Component> getTooltipLines(@Nullable Player param0, TooltipFlag param1) {
        List<Component> var0 = Lists.newArrayList();
        Component var1 = new TextComponent("").append(this.getHoverName()).withStyle(this.getRarity().color);
        if (this.hasCustomHoverName()) {
            var1.withStyle(ChatFormatting.ITALIC);
        }

        var0.add(var1);
        if (!param1.isAdvanced() && !this.hasCustomHoverName() && this.getItem() == Items.FILLED_MAP) {
            var0.add(new TextComponent("#" + MapItem.getMapId(this)).withStyle(ChatFormatting.GRAY));
        }

        int var2 = 0;
        if (this.hasTag() && this.tag.contains("HideFlags", 99)) {
            var2 = this.tag.getInt("HideFlags");
        }

        if ((var2 & 32) == 0) {
            this.getItem().appendHoverText(this, param0 == null ? null : param0.level, var0, param1);
        }

        if (this.hasTag()) {
            if ((var2 & 1) == 0) {
                appendEnchantmentNames(var0, this.getEnchantmentTags());
            }

            if (this.tag.contains("display", 10)) {
                CompoundTag var3 = this.tag.getCompound("display");
                if (var3.contains("color", 3)) {
                    if (param1.isAdvanced()) {
                        var0.add(new TranslatableComponent("item.color", String.format("#%06X", var3.getInt("color"))).withStyle(ChatFormatting.GRAY));
                    } else {
                        var0.add(new TranslatableComponent("item.dyed").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                    }
                }

                if (var3.getTagType("Lore") == 9) {
                    ListTag var4 = var3.getList("Lore", 8);

                    for(int var5 = 0; var5 < var4.size(); ++var5) {
                        String var6 = var4.getString(var5);

                        try {
                            Component var7 = Component.Serializer.fromJson(var6);
                            if (var7 != null) {
                                var0.add(ComponentUtils.mergeStyles(var7, new Style().setColor(ChatFormatting.DARK_PURPLE).setItalic(true)));
                            }
                        } catch (JsonParseException var191) {
                            var3.remove("Lore");
                        }
                    }
                }
            }
        }

        for(EquipmentSlot var9 : EquipmentSlot.values()) {
            Multimap<String, AttributeModifier> var10 = this.getAttributeModifiers(var9);
            if (!var10.isEmpty() && (var2 & 2) == 0) {
                var0.add(new TextComponent(""));
                var0.add(new TranslatableComponent("item.modifiers." + var9.getName()).withStyle(ChatFormatting.GRAY));

                for(Entry<String, AttributeModifier> var11 : var10.entries()) {
                    AttributeModifier var12 = var11.getValue();
                    double var13 = var12.getAmount();
                    boolean var14 = false;
                    if (param0 != null) {
                        if (var12.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
                            var13 += param0.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                            var13 += (double)EnchantmentHelper.getDamageBonus(this, MobType.UNDEFINED);
                            var14 = true;
                        } else if (var12.getId() == Item.BASE_ATTACK_SPEED_UUID) {
                            var13 += param0.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                            var14 = true;
                        }
                    }

                    double var16;
                    if (var12.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && var12.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL
                        )
                     {
                        var16 = var13;
                    } else {
                        var16 = var13 * 100.0;
                    }

                    if (var14) {
                        var0.add(
                            new TextComponent(" ")
                                .append(
                                    new TranslatableComponent(
                                        "attribute.modifier.equals." + var12.getOperation().toValue(),
                                        ATTRIBUTE_MODIFIER_FORMAT.format(var16),
                                        new TranslatableComponent("attribute.name." + (String)var11.getKey())
                                    )
                                )
                                .withStyle(ChatFormatting.DARK_GREEN)
                        );
                    } else if (var13 > 0.0) {
                        var0.add(
                            new TranslatableComponent(
                                    "attribute.modifier.plus." + var12.getOperation().toValue(),
                                    ATTRIBUTE_MODIFIER_FORMAT.format(var16),
                                    new TranslatableComponent("attribute.name." + (String)var11.getKey())
                                )
                                .withStyle(ChatFormatting.BLUE)
                        );
                    } else if (var13 < 0.0) {
                        var16 *= -1.0;
                        var0.add(
                            new TranslatableComponent(
                                    "attribute.modifier.take." + var12.getOperation().toValue(),
                                    ATTRIBUTE_MODIFIER_FORMAT.format(var16),
                                    new TranslatableComponent("attribute.name." + (String)var11.getKey())
                                )
                                .withStyle(ChatFormatting.RED)
                        );
                    }
                }
            }
        }

        if (this.hasTag() && this.getTag().getBoolean("Unbreakable") && (var2 & 4) == 0) {
            var0.add(new TranslatableComponent("item.unbreakable").withStyle(ChatFormatting.BLUE));
        }

        if (this.hasTag() && this.tag.contains("CanDestroy", 9) && (var2 & 8) == 0) {
            ListTag var17 = this.tag.getList("CanDestroy", 8);
            if (!var17.isEmpty()) {
                var0.add(new TextComponent(""));
                var0.add(new TranslatableComponent("item.canBreak").withStyle(ChatFormatting.GRAY));

                for(int var18 = 0; var18 < var17.size(); ++var18) {
                    var0.addAll(expandBlockState(var17.getString(var18)));
                }
            }
        }

        if (this.hasTag() && this.tag.contains("CanPlaceOn", 9) && (var2 & 16) == 0) {
            ListTag var19 = this.tag.getList("CanPlaceOn", 8);
            if (!var19.isEmpty()) {
                var0.add(new TextComponent(""));
                var0.add(new TranslatableComponent("item.canPlace").withStyle(ChatFormatting.GRAY));

                for(int var20 = 0; var20 < var19.size(); ++var20) {
                    var0.addAll(expandBlockState(var19.getString(var20)));
                }
            }
        }

        if (param1.isAdvanced()) {
            if (this.isDamaged()) {
                var0.add(new TranslatableComponent("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
            }

            var0.add(new TextComponent(Registry.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (this.hasTag()) {
                var0.add(new TranslatableComponent("item.nbt_tags", this.getTag().getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void appendEnchantmentNames(List<Component> param0, ListTag param1) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            CompoundTag var1 = param1.getCompound(var0);
            Registry.ENCHANTMENT
                .getOptional(ResourceLocation.tryParse(var1.getString("id")))
                .ifPresent(param2 -> param0.add(param2.getFullname(var1.getInt("lvl"))));
        }

    }

    @OnlyIn(Dist.CLIENT)
    private static Collection<Component> expandBlockState(String param0) {
        try {
            BlockStateParser var0 = new BlockStateParser(new StringReader(param0), true).parse(true);
            BlockState var1 = var0.getState();
            ResourceLocation var2 = var0.getTag();
            boolean var3 = var1 != null;
            boolean var4 = var2 != null;
            if (var3 || var4) {
                if (var3) {
                    return Lists.newArrayList(var1.getBlock().getName().withStyle(ChatFormatting.DARK_GRAY));
                }

                Tag<Block> var5 = BlockTags.getAllTags().getTag(var2);
                if (var5 != null) {
                    Collection<Block> var6 = var5.getValues();
                    if (!var6.isEmpty()) {
                        return var6.stream().map(Block::getName).map(param0x -> param0x.withStyle(ChatFormatting.DARK_GRAY)).collect(Collectors.toList());
                    }
                }
            }
        } catch (CommandSyntaxException var8) {
        }

        return Lists.newArrayList(new TextComponent("missingno").withStyle(ChatFormatting.DARK_GRAY));
    }

    @OnlyIn(Dist.CLIENT)
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
        CompoundTag var1 = new CompoundTag();
        var1.putString("id", String.valueOf(Registry.ENCHANTMENT.getKey(param0)));
        var1.putShort("lvl", (short)((byte)param1));
        var0.add(var1);
    }

    public boolean isEnchanted() {
        if (this.tag != null && this.tag.contains("Enchantments", 9)) {
            return !this.tag.getList("Enchantments", 10).isEmpty();
        } else {
            return false;
        }
    }

    public void addTagElement(String param0, net.minecraft.nbt.Tag param1) {
        this.getOrCreateTag().put(param0, param1);
    }

    public boolean isFramed() {
        return this.frame != null;
    }

    public void setFramed(@Nullable ItemFrame param0) {
        this.frame = param0;
    }

    @Nullable
    public ItemFrame getFrame() {
        return this.emptyCacheFlag ? null : this.frame;
    }

    public int getBaseRepairCost() {
        return this.hasTag() && this.tag.contains("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
    }

    public void setRepairCost(int param0) {
        this.getOrCreateTag().putInt("RepairCost", param0);
    }

    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlot param0) {
        Multimap<String, AttributeModifier> var0;
        if (this.hasTag() && this.tag.contains("AttributeModifiers", 9)) {
            var0 = HashMultimap.create();
            ListTag var1 = this.tag.getList("AttributeModifiers", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                CompoundTag var3 = var1.getCompound(var2);
                AttributeModifier var4 = SharedMonsterAttributes.loadAttributeModifier(var3);
                if (var4 != null
                    && (!var3.contains("Slot", 8) || var3.getString("Slot").equals(param0.getName()))
                    && var4.getId().getLeastSignificantBits() != 0L
                    && var4.getId().getMostSignificantBits() != 0L) {
                    var0.put(var3.getString("AttributeName"), var4);
                }
            }
        } else {
            var0 = this.getItem().getDefaultAttributeModifiers(param0);
        }

        return var0;
    }

    public void addAttributeModifier(String param0, AttributeModifier param1, @Nullable EquipmentSlot param2) {
        this.getOrCreateTag();
        if (!this.tag.contains("AttributeModifiers", 9)) {
            this.tag.put("AttributeModifiers", new ListTag());
        }

        ListTag var0 = this.tag.getList("AttributeModifiers", 10);
        CompoundTag var1 = SharedMonsterAttributes.saveAttributeModifier(param1);
        var1.putString("AttributeName", param0);
        if (param2 != null) {
            var1.putString("Slot", param2.getName());
        }

        var0.add(var1);
    }

    public Component getDisplayName() {
        Component var0 = new TextComponent("").append(this.getHoverName());
        if (this.hasCustomHoverName()) {
            var0.withStyle(ChatFormatting.ITALIC);
        }

        Component var1 = ComponentUtils.wrapInSquareBrackets(var0);
        if (!this.emptyCacheFlag) {
            CompoundTag var2 = this.save(new CompoundTag());
            var1.withStyle(this.getRarity().color)
                .withStyle(param1 -> param1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponent(var2.toString()))));
        }

        return var1;
    }

    private static boolean areSameBlocks(BlockInWorld param0, @Nullable BlockInWorld param1) {
        if (param1 == null || param0.getState() != param1.getState()) {
            return false;
        } else if (param0.getEntity() == null && param1.getEntity() == null) {
            return true;
        } else {
            return param0.getEntity() != null && param1.getEntity() != null
                ? Objects.equals(param0.getEntity().save(new CompoundTag()), param1.getEntity().save(new CompoundTag()))
                : false;
        }
    }

    public boolean hasAdventureModeBreakTagForBlock(TagManager param0, BlockInWorld param1) {
        if (areSameBlocks(param1, this.cachedBreakBlock)) {
            return this.cachedBreakBlockResult;
        } else {
            this.cachedBreakBlock = param1;
            if (this.hasTag() && this.tag.contains("CanDestroy", 9)) {
                ListTag var0 = this.tag.getList("CanDestroy", 8);

                for(int var1 = 0; var1 < var0.size(); ++var1) {
                    String var2 = var0.getString(var1);

                    try {
                        Predicate<BlockInWorld> var3 = BlockPredicateArgument.blockPredicate().parse(new StringReader(var2)).create(param0);
                        if (var3.test(param1)) {
                            this.cachedBreakBlockResult = true;
                            return true;
                        }
                    } catch (CommandSyntaxException var7) {
                    }
                }
            }

            this.cachedBreakBlockResult = false;
            return false;
        }
    }

    public boolean hasAdventureModePlaceTagForBlock(TagManager param0, BlockInWorld param1) {
        if (areSameBlocks(param1, this.cachedPlaceBlock)) {
            return this.cachedPlaceBlockResult;
        } else {
            this.cachedPlaceBlock = param1;
            if (this.hasTag() && this.tag.contains("CanPlaceOn", 9)) {
                ListTag var0 = this.tag.getList("CanPlaceOn", 8);

                for(int var1 = 0; var1 < var0.size(); ++var1) {
                    String var2 = var0.getString(var1);

                    try {
                        Predicate<BlockInWorld> var3 = BlockPredicateArgument.blockPredicate().parse(new StringReader(var2)).create(param0);
                        if (var3.test(param1)) {
                            this.cachedPlaceBlockResult = true;
                            return true;
                        }
                    } catch (CommandSyntaxException var7) {
                    }
                }
            }

            this.cachedPlaceBlockResult = false;
            return false;
        }
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int param0) {
        this.popTime = param0;
    }

    public int getCount() {
        return this.emptyCacheFlag ? 0 : this.count;
    }

    public void setCount(int param0) {
        this.count = param0;
        this.updateEmptyCacheFlag();
    }

    public void grow(int param0) {
        this.setCount(this.count + param0);
    }

    public void shrink(int param0) {
        this.grow(-param0);
    }

    public void onUseTick(Level param0, LivingEntity param1, int param2) {
        this.getItem().onUseTick(param0, param1, this, param2);
    }

    public boolean isEdible() {
        return this.getItem().isEdible();
    }
}
