package net.minecraft.world.item;

import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class ArmorItem extends Item {
    private static final UUID[] ARMOR_MODIFIER_UUID_PER_SLOT = new UUID[]{
        UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
        UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
        UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
        UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
    };
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        protected ItemStack execute(BlockSource param0, ItemStack param1) {
            return ArmorItem.dispenseArmor(param0, param1) ? param1 : super.execute(param0, param1);
        }
    };
    protected final EquipmentSlot slot;
    protected final int defense;
    protected final float toughness;
    protected final float knockbackResistance;
    protected final ArmorMaterial material;

    public static boolean dispenseArmor(BlockSource param0, ItemStack param1) {
        BlockPos var0 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
        List<LivingEntity> var1 = param0.getLevel()
            .getEntitiesOfClass(LivingEntity.class, new AABB(var0), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmourEntitySelector(param1)));
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

    public ArmorItem(ArmorMaterial param0, EquipmentSlot param1, Item.Properties param2) {
        super(param2.defaultDurability(param0.getDurabilityForSlot(param1)));
        this.material = param0;
        this.slot = param1;
        this.defense = param0.getDefenseForSlot(param1);
        this.toughness = param0.getToughness();
        this.knockbackResistance = param0.getKnockbackResistance();
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    public EquipmentSlot getSlot() {
        return this.slot;
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
        ItemStack var0 = param1.getItemInHand(param2);
        EquipmentSlot var1 = Mob.getEquipmentSlotForItem(var0);
        ItemStack var2 = param1.getItemBySlot(var1);
        if (var2.isEmpty()) {
            param1.setItemSlot(var1, var0.copy());
            var0.setCount(0);
            return InteractionResultHolder.success(var0);
        } else {
            return InteractionResultHolder.fail(var0);
        }
    }

    @Override
    public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        Multimap<String, AttributeModifier> var0 = super.getDefaultAttributeModifiers(param0);
        if (param0 == this.slot) {
            var0.put(
                SharedMonsterAttributes.ARMOR.getName(),
                new AttributeModifier(
                    ARMOR_MODIFIER_UUID_PER_SLOT[param0.getIndex()], "Armor modifier", (double)this.defense, AttributeModifier.Operation.ADDITION
                )
            );
            var0.put(
                SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(),
                new AttributeModifier(
                    ARMOR_MODIFIER_UUID_PER_SLOT[param0.getIndex()], "Armor toughness", (double)this.toughness, AttributeModifier.Operation.ADDITION
                )
            );
            if (this.material == ArmorMaterials.NETHERITE) {
                var0.put(
                    SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
                    new AttributeModifier(
                        ARMOR_MODIFIER_UUID_PER_SLOT[param0.getIndex()],
                        "Armor knockback resistance",
                        (double)this.knockbackResistance,
                        AttributeModifier.Operation.ADDITION
                    )
                );
            }
        }

        return var0;
    }

    public int getDefense() {
        return this.defense;
    }
}
