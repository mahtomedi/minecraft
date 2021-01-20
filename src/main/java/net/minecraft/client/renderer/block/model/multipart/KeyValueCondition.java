package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyValueCondition implements Condition {
    private static final Splitter PIPE_SPLITTER = Splitter.on('|').omitEmptyStrings();
    private final String key;
    private final String value;

    public KeyValueCondition(String param0, String param1) {
        this.key = param0;
        this.value = param1;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> param0) {
        Property<?> var0 = param0.getProperty(this.key);
        if (var0 == null) {
            throw new RuntimeException(String.format("Unknown property '%s' on '%s'", this.key, param0.getOwner()));
        } else {
            String var1 = this.value;
            boolean var2 = !var1.isEmpty() && var1.charAt(0) == '!';
            if (var2) {
                var1 = var1.substring(1);
            }

            List<String> var3 = PIPE_SPLITTER.splitToList(var1);
            if (var3.isEmpty()) {
                throw new RuntimeException(String.format("Empty value '%s' for property '%s' on '%s'", this.value, this.key, param0.getOwner()));
            } else {
                Predicate<BlockState> var4;
                if (var3.size() == 1) {
                    var4 = this.getBlockStatePredicate(param0, var0, var1);
                } else {
                    List<Predicate<BlockState>> var5 = var3.stream()
                        .map(param2 -> this.getBlockStatePredicate(param0, var0, param2))
                        .collect(Collectors.toList());
                    var4 = param1 -> var5.stream().anyMatch(param1x -> param1x.test(param1));
                }

                return var2 ? var4.negate() : var4;
            }
        }
    }

    private Predicate<BlockState> getBlockStatePredicate(StateDefinition<Block, BlockState> param0, Property<?> param1, String param2) {
        Optional<?> var0 = param1.getValue(param2);
        if (!var0.isPresent()) {
            throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s' in '%s'", param2, this.key, param0.getOwner(), this.value));
        } else {
            return param2x -> param2x.getValue(param1).equals(var0.get());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("key", this.key).add("value", this.value).toString();
    }
}
