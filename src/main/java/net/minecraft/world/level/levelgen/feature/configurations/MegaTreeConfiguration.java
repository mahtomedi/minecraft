package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class MegaTreeConfiguration extends TreeConfiguration {
    public final int heightInterval;
    public final int crownHeight;

    protected MegaTreeConfiguration(BlockStateProvider param0, BlockStateProvider param1, List<TreeDecorator> param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3);
        this.heightInterval = param4;
        this.crownHeight = param5;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Dynamic<T> var0 = new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("height_interval"),
                    param0.createInt(this.heightInterval),
                    param0.createString("crown_height"),
                    param0.createInt(this.crownHeight)
                )
            )
        );
        return var0.merge(super.serialize(param0));
    }

    public static <T> MegaTreeConfiguration deserialize(Dynamic<T> param0) {
        TreeConfiguration var0 = TreeConfiguration.deserialize(param0);
        return new MegaTreeConfiguration(
            var0.trunkProvider,
            var0.leavesProvider,
            var0.decorators,
            var0.baseHeight,
            param0.get("height_interval").asInt(0),
            param0.get("crown_height").asInt(0)
        );
    }

    public static class MegaTreeConfigurationBuilder extends TreeConfiguration.TreeConfigurationBuilder {
        private List<TreeDecorator> decorators = ImmutableList.of();
        private int baseHeight;
        private int heightInterval;
        private int crownHeight;

        public MegaTreeConfigurationBuilder(BlockStateProvider param0, BlockStateProvider param1) {
            super(param0, param1);
        }

        public MegaTreeConfiguration.MegaTreeConfigurationBuilder decorators(List<TreeDecorator> param0) {
            this.decorators = param0;
            return this;
        }

        public MegaTreeConfiguration.MegaTreeConfigurationBuilder baseHeight(int param0) {
            this.baseHeight = param0;
            return this;
        }

        public MegaTreeConfiguration.MegaTreeConfigurationBuilder heightInterval(int param0) {
            this.heightInterval = param0;
            return this;
        }

        public MegaTreeConfiguration.MegaTreeConfigurationBuilder crownHeight(int param0) {
            this.crownHeight = param0;
            return this;
        }

        public MegaTreeConfiguration build() {
            return new MegaTreeConfiguration(this.trunkProvider, this.leavesProvider, this.decorators, this.baseHeight, this.heightInterval, this.crownHeight);
        }
    }
}
