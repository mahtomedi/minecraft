package net.minecraft.world.item;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class SwordItem extends TieredItem {
    private final float attackDamage;
    private final float attackSpeed;

    public SwordItem(Tier param0, int param1, float param2, Item.Properties param3) {
        super(param0, param3);
        this.attackSpeed = param2;
        this.attackDamage = (float)param1 + param0.getAttackDamageBonus();
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
        Block var0 = param1.getBlock();
        if (var0 == Blocks.COBWEB) {
            return 15.0F;
        } else {
            Material var1 = param1.getMaterial();
            return var1 != Material.PLANT
                    && var1 != Material.REPLACEABLE_PLANT
                    && var1 != Material.CORAL
                    && !param1.is(BlockTags.LEAVES)
                    && var1 != Material.VEGETABLE
                ? 1.0F
                : 1.5F;
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
    public boolean canDestroySpecial(BlockState param0) {
        return param0.getBlock() == Blocks.COBWEB;
    }

    @Override
    public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        Multimap<String, AttributeModifier> var0 = super.getDefaultAttributeModifiers(param0);
        if (param0 == EquipmentSlot.MAINHAND) {
            var0.put(
                SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION)
            );
            var0.put(
                SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)this.attackSpeed, AttributeModifier.Operation.ADDITION)
            );
        }

        return var0;
    }
}
