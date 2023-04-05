package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwordItem extends TieredItem implements Vanishable {
    private final float attackDamage;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public SwordItem(Tier param0, int param1, float param2, Item.Properties param3) {
        super(param0, param3);
        this.attackDamage = (float)param1 + param0.getAttackDamageBonus();
        Builder<Attribute, AttributeModifier> var0 = ImmutableMultimap.builder();
        var0.put(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION)
        );
        var0.put(
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)param2, AttributeModifier.Operation.ADDITION)
        );
        this.defaultModifiers = var0.build();
    }

    public float getDamage() {
        return this.attackDamage;
    }

    @Override
    public boolean canAttackBlock(BlockState param0, Level param1, BlockPos param2, Player param3) {
        return !param3.isCreative();
    }

    @Override
    public float getDestroySpeed(ItemStack param0, BlockState param1) {
        if (param1.is(Blocks.COBWEB)) {
            return 15.0F;
        } else {
            return param1.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack param0, LivingEntity param1, LivingEntity param2) {
        param0.hurtAndBreak(1, param2, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack param0, Level param1, BlockState param2, BlockPos param3, LivingEntity param4) {
        if (param2.getDestroySpeed(param1, param3) != 0.0F) {
            param0.hurtAndBreak(2, param4, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState param0) {
        return param0.is(Blocks.COBWEB);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        return param0 == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(param0);
    }
}
