package net.minecraft.world.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AdventureModeCheck {
    public static final BlockPredicateArgument PREDICATE_PARSER = BlockPredicateArgument.blockPredicate();
    private final String tagName;
    @Nullable
    private BlockInWorld lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    public AdventureModeCheck(String param0) {
        this.tagName = param0;
    }

    private static boolean areSameBlocks(BlockInWorld param0, @Nullable BlockInWorld param1, boolean param2) {
        if (param1 == null || param0.getState() != param1.getState()) {
            return false;
        } else if (!param2) {
            return true;
        } else if (param0.getEntity() == null && param1.getEntity() == null) {
            return true;
        } else {
            return param0.getEntity() != null && param1.getEntity() != null
                ? Objects.equals(param0.getEntity().saveWithId(), param1.getEntity().saveWithId())
                : false;
        }
    }

    public boolean test(ItemStack param0, Registry<Block> param1, BlockInWorld param2) {
        if (areSameBlocks(param2, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        } else {
            this.lastCheckedBlock = param2;
            this.checksBlockEntity = false;
            CompoundTag var0 = param0.getTag();
            if (var0 != null && var0.contains(this.tagName, 9)) {
                ListTag var1 = var0.getList(this.tagName, 8);

                for(int var2 = 0; var2 < var1.size(); ++var2) {
                    String var3 = var1.getString(var2);

                    try {
                        BlockPredicateArgument.Result var4 = PREDICATE_PARSER.parse(new StringReader(var3));
                        this.checksBlockEntity |= var4.requiresNbt();
                        Predicate<BlockInWorld> var5 = var4.create(param1);
                        if (var5.test(param2)) {
                            this.lastResult = true;
                            return true;
                        }
                    } catch (CommandSyntaxException var10) {
                    }
                }
            }

            this.lastResult = false;
            return false;
        }
    }
}
