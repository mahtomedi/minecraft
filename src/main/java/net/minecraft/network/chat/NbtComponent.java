package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NbtComponent extends BaseComponent implements ContextAwareComponent {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final boolean interpreting;
    protected final Optional<Component> separator;
    protected final String nbtPathPattern;
    @Nullable
    protected final NbtPathArgument.NbtPath compiledNbtPath;

    @Nullable
    private static NbtPathArgument.NbtPath compileNbtPath(String param0) {
        try {
            return new NbtPathArgument().parse(new StringReader(param0));
        } catch (CommandSyntaxException var2) {
            return null;
        }
    }

    public NbtComponent(String param0, boolean param1, Optional<Component> param2) {
        this(param0, compileNbtPath(param0), param1, param2);
    }

    protected NbtComponent(String param0, @Nullable NbtPathArgument.NbtPath param1, boolean param2, Optional<Component> param3) {
        this.nbtPathPattern = param0;
        this.compiledNbtPath = param1;
        this.interpreting = param2;
        this.separator = param3;
    }

    protected abstract Stream<CompoundTag> getData(CommandSourceStack var1) throws CommandSyntaxException;

    public String getNbtPath() {
        return this.nbtPathPattern;
    }

    public boolean isInterpreting() {
        return this.interpreting;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        if (param0 != null && this.compiledNbtPath != null) {
            Stream<String> var0 = this.getData(param0).flatMap(param0x -> {
                try {
                    return this.compiledNbtPath.get(param0x).stream();
                } catch (CommandSyntaxException var3x) {
                    return Stream.empty();
                }
            }).map(Tag::getAsString);
            if (this.interpreting) {
                Component var1 = DataFixUtils.orElse(
                    ComponentUtils.updateForEntity(param0, this.separator, param1, param2), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR
                );
                return var0.flatMap(param3 -> {
                    try {
                        MutableComponent var0x = Component.Serializer.fromJson(param3);
                        return Stream.of(ComponentUtils.updateForEntity(param0, var0x, param1, param2));
                    } catch (Exception var5x) {
                        LOGGER.warn("Failed to parse component: {}", param3, var5x);
                        return Stream.of();
                    }
                }).reduce((param1x, param2x) -> param1x.append(var1).append(param2x)).orElseGet(() -> new TextComponent(""));
            } else {
                return ComponentUtils.updateForEntity(param0, this.separator, param1, param2)
                    .map(
                        param1x -> var0.map(param0x -> new TextComponent(param0x))
                                .reduce((param1xx, param2x) -> param1xx.append(param1x).append(param2x))
                                .orElseGet(() -> new TextComponent(""))
                    )
                    .orElseGet(() -> new TextComponent(var0.collect(Collectors.joining(", "))));
            }
        } else {
            return new TextComponent("");
        }
    }

    public static class BlockNbtComponent extends NbtComponent {
        private final String posPattern;
        @Nullable
        private final Coordinates compiledPos;

        public BlockNbtComponent(String param0, boolean param1, String param2, Optional<Component> param3) {
            super(param0, param1, param3);
            this.posPattern = param2;
            this.compiledPos = this.compilePos(this.posPattern);
        }

        @Nullable
        private Coordinates compilePos(String param0) {
            try {
                return BlockPosArgument.blockPos().parse(new StringReader(param0));
            } catch (CommandSyntaxException var3) {
                return null;
            }
        }

        private BlockNbtComponent(
            String param0, @Nullable NbtPathArgument.NbtPath param1, boolean param2, String param3, @Nullable Coordinates param4, Optional<Component> param5
        ) {
            super(param0, param1, param2, param5);
            this.posPattern = param3;
            this.compiledPos = param4;
        }

        @Nullable
        public String getPos() {
            return this.posPattern;
        }

        public NbtComponent.BlockNbtComponent plainCopy() {
            return new NbtComponent.BlockNbtComponent(
                this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.posPattern, this.compiledPos, this.separator
            );
        }

        @Override
        protected Stream<CompoundTag> getData(CommandSourceStack param0) {
            if (this.compiledPos != null) {
                ServerLevel var0 = param0.getLevel();
                BlockPos var1 = this.compiledPos.getBlockPos(param0);
                if (var0.isLoaded(var1)) {
                    BlockEntity var2 = var0.getBlockEntity(var1);
                    if (var2 != null) {
                        return Stream.of(var2.save(new CompoundTag()));
                    }
                }
            }

            return Stream.empty();
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof NbtComponent.BlockNbtComponent)) {
                return false;
            } else {
                NbtComponent.BlockNbtComponent var0 = (NbtComponent.BlockNbtComponent)param0;
                return Objects.equals(this.posPattern, var0.posPattern) && Objects.equals(this.nbtPathPattern, var0.nbtPathPattern) && super.equals(param0);
            }
        }

        @Override
        public String toString() {
            return "BlockPosArgument{pos='"
                + this.posPattern
                + '\''
                + "path='"
                + this.nbtPathPattern
                + '\''
                + ", siblings="
                + this.siblings
                + ", style="
                + this.getStyle()
                + '}';
        }
    }

    public static class EntityNbtComponent extends NbtComponent {
        private final String selectorPattern;
        @Nullable
        private final EntitySelector compiledSelector;

        public EntityNbtComponent(String param0, boolean param1, String param2, Optional<Component> param3) {
            super(param0, param1, param3);
            this.selectorPattern = param2;
            this.compiledSelector = compileSelector(param2);
        }

        @Nullable
        private static EntitySelector compileSelector(String param0) {
            try {
                EntitySelectorParser var0 = new EntitySelectorParser(new StringReader(param0));
                return var0.parse();
            } catch (CommandSyntaxException var2) {
                return null;
            }
        }

        private EntityNbtComponent(
            String param0, @Nullable NbtPathArgument.NbtPath param1, boolean param2, String param3, @Nullable EntitySelector param4, Optional<Component> param5
        ) {
            super(param0, param1, param2, param5);
            this.selectorPattern = param3;
            this.compiledSelector = param4;
        }

        public String getSelector() {
            return this.selectorPattern;
        }

        public NbtComponent.EntityNbtComponent plainCopy() {
            return new NbtComponent.EntityNbtComponent(
                this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.selectorPattern, this.compiledSelector, this.separator
            );
        }

        @Override
        protected Stream<CompoundTag> getData(CommandSourceStack param0) throws CommandSyntaxException {
            if (this.compiledSelector != null) {
                List<? extends Entity> var0 = this.compiledSelector.findEntities(param0);
                return var0.stream().map(NbtPredicate::getEntityTagToCompare);
            } else {
                return Stream.empty();
            }
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof NbtComponent.EntityNbtComponent)) {
                return false;
            } else {
                NbtComponent.EntityNbtComponent var0 = (NbtComponent.EntityNbtComponent)param0;
                return Objects.equals(this.selectorPattern, var0.selectorPattern)
                    && Objects.equals(this.nbtPathPattern, var0.nbtPathPattern)
                    && super.equals(param0);
            }
        }

        @Override
        public String toString() {
            return "EntityNbtComponent{selector='"
                + this.selectorPattern
                + '\''
                + "path='"
                + this.nbtPathPattern
                + '\''
                + ", siblings="
                + this.siblings
                + ", style="
                + this.getStyle()
                + '}';
        }
    }

    public static class StorageNbtComponent extends NbtComponent {
        private final ResourceLocation id;

        public StorageNbtComponent(String param0, boolean param1, ResourceLocation param2, Optional<Component> param3) {
            super(param0, param1, param3);
            this.id = param2;
        }

        public StorageNbtComponent(String param0, @Nullable NbtPathArgument.NbtPath param1, boolean param2, ResourceLocation param3, Optional<Component> param4) {
            super(param0, param1, param2, param4);
            this.id = param3;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public NbtComponent.StorageNbtComponent plainCopy() {
            return new NbtComponent.StorageNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.id, this.separator);
        }

        @Override
        protected Stream<CompoundTag> getData(CommandSourceStack param0) {
            CompoundTag var0 = param0.getServer().getCommandStorage().get(this.id);
            return Stream.of(var0);
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof NbtComponent.StorageNbtComponent)) {
                return false;
            } else {
                NbtComponent.StorageNbtComponent var0 = (NbtComponent.StorageNbtComponent)param0;
                return Objects.equals(this.id, var0.id) && Objects.equals(this.nbtPathPattern, var0.nbtPathPattern) && super.equals(param0);
            }
        }

        @Override
        public String toString() {
            return "StorageNbtComponent{id='"
                + this.id
                + '\''
                + "path='"
                + this.nbtPathPattern
                + '\''
                + ", siblings="
                + this.siblings
                + ", style="
                + this.getStyle()
                + '}';
        }
    }
}
