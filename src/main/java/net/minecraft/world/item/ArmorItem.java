package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class ArmorItem extends Item implements Equipable {
    private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        param0.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        param0.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        param0.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
    });
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        protected ItemStack execute(BlockSource param0, ItemStack param1) {
            return ArmorItem.dispenseArmor(param0, param1) ? param1 : super.execute(param0, param1);
        }
    };
    protected final ArmorItem.Type type;
    private final int defense;
    private final float toughness;
    protected final float knockbackResistance;
    protected final ArmorMaterial material;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public static boolean dispenseArmor(BlockSource param0, ItemStack param1) {
        BlockPos var0 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
        List<LivingEntity> var1 = param0.getLevel()
            .getEntitiesOfClass(LivingEntity.class, new AABB(var0), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(param1)));
        if (var1.isEmpty()) {
            return false;
        } else {
            LivingEntity var2 = var1.get(0);
            EquipmentSlot var3 = Mob.getEquipmentSlotForItem(param1);
            ItemStack var4 = param1.split(1);
            var2.setItemSlot(var3, var4);
            if (var2 instanceof Mob) {
                ((Mob)var2).setDropChance(var3, 2.0F);
                ((Mob)var2).setPersistenceRequired();
            }

            return true;
        }
    }

    public ArmorItem(ArmorMaterial param0, ArmorItem.Type param1, Item.Properties param2) {
        super(param2.defaultDurability(param0.getDurabilityForType(param1)));
        this.material = param0;
        this.type = param1;
        this.defense = param0.getDefenseForType(param1);
        this.toughness = param0.getToughness();
        this.knockbackResistance = param0.getKnockbackResistance();
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
        Builder<Attribute, AttributeModifier> var0 = ImmutableMultimap.builder();
        UUID var1 = ARMOR_MODIFIER_UUID_PER_TYPE.get(param1);
        var0.put(Attributes.ARMOR, new AttributeModifier(var1, "Armor modifier", (double)this.defense, AttributeModifier.Operation.ADDITION));
        var0.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(var1, "Armor toughness", (double)this.toughness, AttributeModifier.Operation.ADDITION));
        if (param0 == ArmorMaterials.NETHERITE) {
            var0.put(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(var1, "Armor knockback resistance", (double)this.knockbackResistance, AttributeModifier.Operation.ADDITION)
            );
        }

        this.defaultModifiers = var0.build();
    }

    public ArmorItem.Type getType() {
        return this.type;
    }

    @Override
    public int getEnchantmentValue() {
        return this.material.getEnchantmentValue();
    }

    public ArmorMaterial getMaterial() {
        return this.material;
    }

    @Override
    public boolean isValidRepairItem(ItemStack param0, ItemStack param1) {
        return this.material.getRepairIngredient().test(param1) || super.isValidRepairItem(param0, param1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        return this.swapWithEquipmentSlot(this, param0, param1, param2);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        return param0 == this.type.getSlot() ? this.defaultModifiers : super.getDefaultAttributeModifiers(param0);
    }

    public int getDefense() {
        return this.defense;
    }

    public float getToughness() {
        return this.toughness;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return this.type.getSlot();
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.getMaterial().getEquipSound();
    }

    public static enum Type {
        HELMET(EquipmentSlot.HEAD, "helmet"),
        CHESTPLATE(EquipmentSlot.CHEST, "chestplate"),
        LEGGINGS(EquipmentSlot.LEGS, "leggings"),
        BOOTS(EquipmentSlot.FEET, "boots");

        private final EquipmentSlot slot;
        private final String name;

        private Type(EquipmentSlot param0, String param1) {
            this.slot = param0;
            this.name = param1;
        }

        public EquipmentSlot getSlot() {
            return this.slot;
        }

        public String getName() {
            return this.name;
        }
    }
}
