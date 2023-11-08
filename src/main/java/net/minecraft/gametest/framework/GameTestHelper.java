package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GameTestHelper {
    private final GameTestInfo testInfo;
    private boolean finalCheckAdded;

    public GameTestHelper(GameTestInfo param0) {
        this.testInfo = param0;
    }

    public ServerLevel getLevel() {
        return this.testInfo.getLevel();
    }

    public BlockState getBlockState(BlockPos param0) {
        return this.getLevel().getBlockState(this.absolutePos(param0));
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.getLevel().getBlockEntity(this.absolutePos(param0));
    }

    public void killAllEntities() {
        this.killAllEntitiesOfClass(Entity.class);
    }

    public void killAllEntitiesOfClass(Class param0) {
        AABB var0 = this.getBounds();
        List<Entity> var1 = this.getLevel().getEntitiesOfClass(param0, var0.inflate(1.0), param0x -> !(param0x instanceof Player));
        var1.forEach(Entity::kill);
    }

    public ItemEntity spawnItem(Item param0, float param1, float param2, float param3) {
        ServerLevel var0 = this.getLevel();
        Vec3 var1 = this.absoluteVec(new Vec3((double)param1, (double)param2, (double)param3));
        ItemEntity var2 = new ItemEntity(var0, var1.x, var1.y, var1.z, new ItemStack(param0, 1));
        var2.setDeltaMovement(0.0, 0.0, 0.0);
        var0.addFreshEntity(var2);
        return var2;
    }

    public ItemEntity spawnItem(Item param0, BlockPos param1) {
        return this.spawnItem(param0, (float)param1.getX(), (float)param1.getY(), (float)param1.getZ());
    }

    public <E extends Entity> E spawn(EntityType<E> param0, BlockPos param1) {
        return this.spawn(param0, Vec3.atBottomCenterOf(param1));
    }

    public <E extends Entity> E spawn(EntityType<E> param0, Vec3 param1) {
        ServerLevel var0 = this.getLevel();
        E var1 = param0.create(var0);
        if (var1 == null) {
            throw new NullPointerException("Failed to create entity " + param0.builtInRegistryHolder().key().location());
        } else {
            if (var1 instanceof Mob var2) {
                var2.setPersistenceRequired();
            }

            Vec3 var3 = this.absoluteVec(param1);
            var1.moveTo(var3.x, var3.y, var3.z, var1.getYRot(), var1.getXRot());
            var0.addFreshEntity(var1);
            return var1;
        }
    }

    public <E extends Entity> E spawn(EntityType<E> param0, int param1, int param2, int param3) {
        return this.spawn(param0, new BlockPos(param1, param2, param3));
    }

    public <E extends Entity> E spawn(EntityType<E> param0, float param1, float param2, float param3) {
        return this.spawn(param0, new Vec3((double)param1, (double)param2, (double)param3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> param0, BlockPos param1) {
        E var0 = this.spawn(param0, param1);
        var0.removeFreeWill();
        return var0;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> param0, int param1, int param2, int param3) {
        return this.spawnWithNoFreeWill(param0, new BlockPos(param1, param2, param3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> param0, Vec3 param1) {
        E var0 = this.spawn(param0, param1);
        var0.removeFreeWill();
        return var0;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> param0, float param1, float param2, float param3) {
        return this.spawnWithNoFreeWill(param0, new Vec3((double)param1, (double)param2, (double)param3));
    }

    public GameTestSequence walkTo(Mob param0, BlockPos param1, float param2) {
        return this.startSequence().thenExecuteAfter(2, () -> {
            Path var0 = param0.getNavigation().createPath(this.absolutePos(param1), 0);
            param0.getNavigation().moveTo(var0, (double)param2);
        });
    }

    public void pressButton(int param0, int param1, int param2) {
        this.pressButton(new BlockPos(param0, param1, param2));
    }

    public void pressButton(BlockPos param0) {
        this.assertBlockState(param0, param0x -> param0x.is(BlockTags.BUTTONS), () -> "Expected button");
        BlockPos var0 = this.absolutePos(param0);
        BlockState var1 = this.getLevel().getBlockState(var0);
        ButtonBlock var2 = (ButtonBlock)var1.getBlock();
        var2.press(var1, this.getLevel(), var0);
    }

    public void useBlock(BlockPos param0) {
        this.useBlock(param0, this.makeMockPlayer());
    }

    public void useBlock(BlockPos param0, Player param1) {
        BlockPos var0 = this.absolutePos(param0);
        this.useBlock(param0, param1, new BlockHitResult(Vec3.atCenterOf(var0), Direction.NORTH, var0, true));
    }

    public void useBlock(BlockPos param0, Player param1, BlockHitResult param2) {
        BlockPos var0 = this.absolutePos(param0);
        BlockState var1 = this.getLevel().getBlockState(var0);
        InteractionResult var2 = var1.use(this.getLevel(), param1, InteractionHand.MAIN_HAND, param2);
        if (!var2.consumesAction()) {
            UseOnContext var3 = new UseOnContext(param1, InteractionHand.MAIN_HAND, param2);
            param1.getItemInHand(InteractionHand.MAIN_HAND).useOn(var3);
        }

    }

    public LivingEntity makeAboutToDrown(LivingEntity param0) {
        param0.setAirSupply(0);
        param0.setHealth(0.25F);
        return param0;
    }

    public Player makeMockSurvivalPlayer() {
        return new Player(this.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
    }

    public LivingEntity withLowHealth(LivingEntity param0) {
        param0.setHealth(0.25F);
        return param0;
    }

    public Player makeMockPlayer() {
        return new Player(this.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }

            @Override
            public boolean isLocalPlayer() {
                return true;
            }
        };
    }

    @Deprecated(
        forRemoval = true
    )
    public ServerPlayer makeMockServerPlayerInLevel() {
        CommonListenerCookie var0 = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"));
        ServerPlayer var1 = new ServerPlayer(this.getLevel().getServer(), this.getLevel(), var0.gameProfile(), var0.clientInformation()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        Connection var2 = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel var3 = new EmbeddedChannel(var2);
        var3.attr(Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(ConnectionProtocol.PLAY.codec(PacketFlow.SERVERBOUND));
        this.getLevel().getServer().getPlayerList().placeNewPlayer(var2, var1, var0);
        return var1;
    }

    public void pullLever(int param0, int param1, int param2) {
        this.pullLever(new BlockPos(param0, param1, param2));
    }

    public void pullLever(BlockPos param0) {
        this.assertBlockPresent(Blocks.LEVER, param0);
        BlockPos var0 = this.absolutePos(param0);
        BlockState var1 = this.getLevel().getBlockState(var0);
        LeverBlock var2 = (LeverBlock)var1.getBlock();
        var2.pull(var1, this.getLevel(), var0);
    }

    public void pulseRedstone(BlockPos param0, long param1) {
        this.setBlock(param0, Blocks.REDSTONE_BLOCK);
        this.runAfterDelay(param1, () -> this.setBlock(param0, Blocks.AIR));
    }

    public void destroyBlock(BlockPos param0) {
        this.getLevel().destroyBlock(this.absolutePos(param0), false, null);
    }

    public void setBlock(int param0, int param1, int param2, Block param3) {
        this.setBlock(new BlockPos(param0, param1, param2), param3);
    }

    public void setBlock(int param0, int param1, int param2, BlockState param3) {
        this.setBlock(new BlockPos(param0, param1, param2), param3);
    }

    public void setBlock(BlockPos param0, Block param1) {
        this.setBlock(param0, param1.defaultBlockState());
    }

    public void setBlock(BlockPos param0, BlockState param1) {
        this.getLevel().setBlock(this.absolutePos(param0), param1, 3);
    }

    public void setNight() {
        this.setDayTime(13000);
    }

    public void setDayTime(int param0) {
        this.getLevel().setDayTime((long)param0);
    }

    public void assertBlockPresent(Block param0, int param1, int param2, int param3) {
        this.assertBlockPresent(param0, new BlockPos(param1, param2, param3));
    }

    public void assertBlockPresent(Block param0, BlockPos param1) {
        BlockState var0 = this.getBlockState(param1);
        this.assertBlock(param1, param2 -> var0.is(param0), "Expected " + param0.getName().getString() + ", got " + var0.getBlock().getName().getString());
    }

    public void assertBlockNotPresent(Block param0, int param1, int param2, int param3) {
        this.assertBlockNotPresent(param0, new BlockPos(param1, param2, param3));
    }

    public void assertBlockNotPresent(Block param0, BlockPos param1) {
        this.assertBlock(param1, param2 -> !this.getBlockState(param1).is(param0), "Did not expect " + param0.getName().getString());
    }

    public void succeedWhenBlockPresent(Block param0, int param1, int param2, int param3) {
        this.succeedWhenBlockPresent(param0, new BlockPos(param1, param2, param3));
    }

    public void succeedWhenBlockPresent(Block param0, BlockPos param1) {
        this.succeedWhen(() -> this.assertBlockPresent(param0, param1));
    }

    public void assertBlock(BlockPos param0, Predicate<Block> param1, String param2) {
        this.assertBlock(param0, param1, () -> param2);
    }

    public void assertBlock(BlockPos param0, Predicate<Block> param1, Supplier<String> param2) {
        this.assertBlockState(param0, param1x -> param1.test(param1x.getBlock()), param2);
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos param0, Property<T> param1, T param2) {
        BlockState var0 = this.getBlockState(param0);
        boolean var1 = var0.hasProperty(param1);
        if (!var1 || !var0.<T>getValue(param1).equals(param2)) {
            String var2 = var1 ? "was " + var0.getValue(param1) : "property " + param1.getName() + " is missing";
            String var3 = String.format(Locale.ROOT, "Expected property %s to be %s, %s", param1.getName(), param2, var2);
            throw new GameTestAssertPosException(var3, this.absolutePos(param0), param0, this.testInfo.getTick());
        }
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos param0, Property<T> param1, Predicate<T> param2, String param3) {
        this.assertBlockState(param0, param2x -> {
            if (!param2x.hasProperty(param1)) {
                return false;
            } else {
                T var0 = param2x.getValue(param1);
                return param2.test(var0);
            }
        }, () -> param3);
    }

    public void assertBlockState(BlockPos param0, Predicate<BlockState> param1, Supplier<String> param2) {
        BlockState var0 = this.getBlockState(param0);
        if (!param1.test(var0)) {
            throw new GameTestAssertPosException(param2.get(), this.absolutePos(param0), param0, this.testInfo.getTick());
        }
    }

    public void assertRedstoneSignal(BlockPos param0, Direction param1, IntPredicate param2, Supplier<String> param3) {
        BlockPos var0 = this.absolutePos(param0);
        ServerLevel var1 = this.getLevel();
        BlockState var2 = var1.getBlockState(var0);
        int var3 = var2.getSignal(var1, var0, param1);
        if (!param2.test(var3)) {
            throw new GameTestAssertPosException(param3.get(), var0, param0, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityType<?> param0) {
        List<? extends Entity> var0 = this.getLevel().getEntities(param0, this.getBounds(), Entity::isAlive);
        if (var0.isEmpty()) {
            throw new GameTestAssertException("Expected " + param0.toShortString() + " to exist");
        }
    }

    public void assertEntityPresent(EntityType<?> param0, int param1, int param2, int param3) {
        this.assertEntityPresent(param0, new BlockPos(param1, param2, param3));
    }

    public void assertEntityPresent(EntityType<?> param0, BlockPos param1) {
        BlockPos var0 = this.absolutePos(param1);
        List<? extends Entity> var1 = this.getLevel().getEntities(param0, new AABB(var0), Entity::isAlive);
        if (var1.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + param0.toShortString(), var0, param1, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityType<?> param0, Vec3 param1, Vec3 param2) {
        List<? extends Entity> var0 = this.getLevel().getEntities(param0, new AABB(param1, param2), Entity::isAlive);
        if (var0.isEmpty()) {
            throw new GameTestAssertPosException(
                "Expected " + param0.toShortString() + " between ", BlockPos.containing(param1), BlockPos.containing(param2), this.testInfo.getTick()
            );
        }
    }

    public void assertEntitiesPresent(EntityType<?> param0, int param1) {
        List<? extends Entity> var0 = this.getLevel().getEntities(param0, this.getBounds(), Entity::isAlive);
        if (var0.size() != param1) {
            throw new GameTestAssertException("Expected " + param1 + " of type " + param0.toShortString() + " to exist, found " + var0.size());
        }
    }

    public void assertEntitiesPresent(EntityType<?> param0, BlockPos param1, int param2, double param3) {
        BlockPos var0 = this.absolutePos(param1);
        List<? extends Entity> var1 = this.getEntities(param0, param1, param3);
        if (var1.size() != param2) {
            throw new GameTestAssertPosException(
                "Expected " + param2 + " entities of type " + param0.toShortString() + ", actual number of entities found=" + var1.size(),
                var0,
                param1,
                this.testInfo.getTick()
            );
        }
    }

    public void assertEntityPresent(EntityType<?> param0, BlockPos param1, double param2) {
        List<? extends Entity> var0 = this.getEntities(param0, param1, param2);
        if (var0.isEmpty()) {
            BlockPos var1 = this.absolutePos(param1);
            throw new GameTestAssertPosException("Expected " + param0.toShortString(), var1, param1, this.testInfo.getTick());
        }
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> param0, BlockPos param1, double param2) {
        BlockPos var0 = this.absolutePos(param1);
        return this.getLevel().getEntities(param0, new AABB(var0).inflate(param2), Entity::isAlive);
    }

    public void assertEntityInstancePresent(Entity param0, int param1, int param2, int param3) {
        this.assertEntityInstancePresent(param0, new BlockPos(param1, param2, param3));
    }

    public void assertEntityInstancePresent(Entity param0, BlockPos param1) {
        BlockPos var0 = this.absolutePos(param1);
        List<? extends Entity> var1 = this.getLevel().getEntities(param0.getType(), new AABB(var0), Entity::isAlive);
        var1.stream()
            .filter(param1x -> param1x == param0)
            .findFirst()
            .orElseThrow(() -> new GameTestAssertPosException("Expected " + param0.getType().toShortString(), var0, param1, this.testInfo.getTick()));
    }

    public void assertItemEntityCountIs(Item param0, BlockPos param1, double param2, int param3) {
        BlockPos var0 = this.absolutePos(param1);
        List<ItemEntity> var1 = this.getLevel().getEntities(EntityType.ITEM, new AABB(var0).inflate(param2), Entity::isAlive);
        int var2 = 0;

        for(ItemEntity var3 : var1) {
            ItemStack var4 = var3.getItem();
            if (var4.is(param0)) {
                var2 += var4.getCount();
            }
        }

        if (var2 != param3) {
            throw new GameTestAssertPosException(
                "Expected " + param3 + " " + param0.getDescription().getString() + " items to exist (found " + var2 + ")",
                var0,
                param1,
                this.testInfo.getTick()
            );
        }
    }

    public void assertItemEntityPresent(Item param0, BlockPos param1, double param2) {
        BlockPos var0 = this.absolutePos(param1);

        for(Entity var2 : this.getLevel().getEntities(EntityType.ITEM, new AABB(var0).inflate(param2), Entity::isAlive)) {
            ItemEntity var3 = (ItemEntity)var2;
            if (var3.getItem().getItem().equals(param0)) {
                return;
            }
        }

        throw new GameTestAssertPosException("Expected " + param0.getDescription().getString() + " item", var0, param1, this.testInfo.getTick());
    }

    public void assertItemEntityNotPresent(Item param0, BlockPos param1, double param2) {
        BlockPos var0 = this.absolutePos(param1);

        for(Entity var2 : this.getLevel().getEntities(EntityType.ITEM, new AABB(var0).inflate(param2), Entity::isAlive)) {
            ItemEntity var3 = (ItemEntity)var2;
            if (var3.getItem().getItem().equals(param0)) {
                throw new GameTestAssertPosException("Did not expect " + param0.getDescription().getString() + " item", var0, param1, this.testInfo.getTick());
            }
        }

    }

    public void assertItemEntityPresent(Item param0) {
        for(Entity var1 : this.getLevel().getEntities(EntityType.ITEM, this.getBounds(), Entity::isAlive)) {
            ItemEntity var2 = (ItemEntity)var1;
            if (var2.getItem().getItem().equals(param0)) {
                return;
            }
        }

        throw new GameTestAssertException("Expected " + param0.getDescription().getString() + " item");
    }

    public void assertItemEntityNotPresent(Item param0) {
        for(Entity var1 : this.getLevel().getEntities(EntityType.ITEM, this.getBounds(), Entity::isAlive)) {
            ItemEntity var2 = (ItemEntity)var1;
            if (var2.getItem().getItem().equals(param0)) {
                throw new GameTestAssertException("Did not expect " + param0.getDescription().getString() + " item");
            }
        }

    }

    public void assertEntityNotPresent(EntityType<?> param0) {
        List<? extends Entity> var0 = this.getLevel().getEntities(param0, this.getBounds(), Entity::isAlive);
        if (!var0.isEmpty()) {
            throw new GameTestAssertException("Did not expect " + param0.toShortString() + " to exist");
        }
    }

    public void assertEntityNotPresent(EntityType<?> param0, int param1, int param2, int param3) {
        this.assertEntityNotPresent(param0, new BlockPos(param1, param2, param3));
    }

    public void assertEntityNotPresent(EntityType<?> param0, BlockPos param1) {
        BlockPos var0 = this.absolutePos(param1);
        List<? extends Entity> var1 = this.getLevel().getEntities(param0, new AABB(var0), Entity::isAlive);
        if (!var1.isEmpty()) {
            throw new GameTestAssertPosException("Did not expect " + param0.toShortString(), var0, param1, this.testInfo.getTick());
        }
    }

    public void assertEntityTouching(EntityType<?> param0, double param1, double param2, double param3) {
        Vec3 var0 = new Vec3(param1, param2, param3);
        Vec3 var1 = this.absoluteVec(var0);
        Predicate<? super Entity> var2 = param1x -> param1x.getBoundingBox().intersects(var1, var1);
        List<? extends Entity> var3 = this.getLevel().getEntities(param0, this.getBounds(), var2);
        if (var3.isEmpty()) {
            throw new GameTestAssertException("Expected " + param0.toShortString() + " to touch " + var1 + " (relative " + var0 + ")");
        }
    }

    public void assertEntityNotTouching(EntityType<?> param0, double param1, double param2, double param3) {
        Vec3 var0 = new Vec3(param1, param2, param3);
        Vec3 var1 = this.absoluteVec(var0);
        Predicate<? super Entity> var2 = param1x -> !param1x.getBoundingBox().intersects(var1, var1);
        List<? extends Entity> var3 = this.getLevel().getEntities(param0, this.getBounds(), var2);
        if (var3.isEmpty()) {
            throw new GameTestAssertException("Did not expect " + param0.toShortString() + " to touch " + var1 + " (relative " + var0 + ")");
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPos param0, EntityType<E> param1, Function<? super E, T> param2, @Nullable T param3) {
        BlockPos var0 = this.absolutePos(param0);
        List<E> var1 = this.getLevel().getEntities(param1, new AABB(var0), Entity::isAlive);
        if (var1.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + param1.toShortString(), var0, param0, this.testInfo.getTick());
        } else {
            for(E var2 : var1) {
                T var3 = param2.apply(var2);
                if (var3 == null) {
                    if (param3 != null) {
                        throw new GameTestAssertException("Expected entity data to be: " + param3 + ", but was: " + var3);
                    }
                } else if (!var3.equals(param3)) {
                    throw new GameTestAssertException("Expected entity data to be: " + param3 + ", but was: " + var3);
                }
            }

        }
    }

    public <E extends LivingEntity> void assertEntityIsHolding(BlockPos param0, EntityType<E> param1, Item param2) {
        BlockPos var0 = this.absolutePos(param0);
        List<E> var1 = this.getLevel().getEntities(param1, new AABB(var0), Entity::isAlive);
        if (var1.isEmpty()) {
            throw new GameTestAssertPosException("Expected entity of type: " + param1, var0, param0, this.getTick());
        } else {
            for(E var2 : var1) {
                if (var2.isHolding(param2)) {
                    return;
                }
            }

            throw new GameTestAssertPosException("Entity should be holding: " + param2, var0, param0, this.getTick());
        }
    }

    public <E extends Entity & InventoryCarrier> void assertEntityInventoryContains(BlockPos param0, EntityType<E> param1, Item param2) {
        BlockPos var0 = this.absolutePos(param0);
        List<E> var1 = this.getLevel().getEntities(param1, new AABB(var0), param0x -> param0x.isAlive());
        if (var1.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + param1.toShortString() + " to exist", var0, param0, this.getTick());
        } else {
            for(E var2 : var1) {
                if (var2.getInventory().hasAnyMatching(param1x -> param1x.is(param2))) {
                    return;
                }
            }

            throw new GameTestAssertPosException("Entity inventory should contain: " + param2, var0, param0, this.getTick());
        }
    }

    public void assertContainerEmpty(BlockPos param0) {
        BlockPos var0 = this.absolutePos(param0);
        BlockEntity var1 = this.getLevel().getBlockEntity(var0);
        if (var1 instanceof BaseContainerBlockEntity && !((BaseContainerBlockEntity)var1).isEmpty()) {
            throw new GameTestAssertException("Container should be empty");
        }
    }

    public void assertContainerContains(BlockPos param0, Item param1) {
        BlockPos var0 = this.absolutePos(param0);
        BlockEntity var1 = this.getLevel().getBlockEntity(var0);
        if (!(var1 instanceof BaseContainerBlockEntity)) {
            throw new GameTestAssertException("Expected a container at " + param0 + ", found " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(var1.getType()));
        } else if (((BaseContainerBlockEntity)var1).countItem(param1) != 1) {
            throw new GameTestAssertException("Container should contain: " + param1);
        }
    }

    public void assertSameBlockStates(BoundingBox param0, BlockPos param1) {
        BlockPos.betweenClosedStream(param0).forEach(param2 -> {
            BlockPos var0 = param1.offset(param2.getX() - param0.minX(), param2.getY() - param0.minY(), param2.getZ() - param0.minZ());
            this.assertSameBlockState(param2, var0);
        });
    }

    public void assertSameBlockState(BlockPos param0, BlockPos param1) {
        BlockState var0 = this.getBlockState(param0);
        BlockState var1 = this.getBlockState(param1);
        if (var0 != var1) {
            this.fail("Incorrect state. Expected " + var1 + ", got " + var0, param0);
        }

    }

    public void assertAtTickTimeContainerContains(long param0, BlockPos param1, Item param2) {
        this.runAtTickTime(param0, () -> this.assertContainerContains(param1, param2));
    }

    public void assertAtTickTimeContainerEmpty(long param0, BlockPos param1) {
        this.runAtTickTime(param0, () -> this.assertContainerEmpty(param1));
    }

    public <E extends Entity, T> void succeedWhenEntityData(BlockPos param0, EntityType<E> param1, Function<E, T> param2, T param3) {
        this.succeedWhen(() -> this.assertEntityData(param0, param1, param2, param3));
    }

    public <E extends Entity> void assertEntityProperty(E param0, Predicate<E> param1, String param2) {
        if (!param1.test(param0)) {
            throw new GameTestAssertException("Entity " + param0 + " failed " + param2 + " test");
        }
    }

    public <E extends Entity, T> void assertEntityProperty(E param0, Function<E, T> param1, String param2, T param3) {
        T var0 = param1.apply(param0);
        if (!var0.equals(param3)) {
            throw new GameTestAssertException("Entity " + param0 + " value " + param2 + "=" + var0 + " is not equal to expected " + param3);
        }
    }

    public void assertLivingEntityHasMobEffect(LivingEntity param0, MobEffect param1, int param2) {
        MobEffectInstance var0 = param0.getEffect(param1);
        if (var0 == null || var0.getAmplifier() != param2) {
            int var1 = param2 + 1;
            throw new GameTestAssertException("Entity " + param0 + " failed has " + param1.getDescriptionId() + " x " + var1 + " test");
        }
    }

    public void succeedWhenEntityPresent(EntityType<?> param0, int param1, int param2, int param3) {
        this.succeedWhenEntityPresent(param0, new BlockPos(param1, param2, param3));
    }

    public void succeedWhenEntityPresent(EntityType<?> param0, BlockPos param1) {
        this.succeedWhen(() -> this.assertEntityPresent(param0, param1));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> param0, int param1, int param2, int param3) {
        this.succeedWhenEntityNotPresent(param0, new BlockPos(param1, param2, param3));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> param0, BlockPos param1) {
        this.succeedWhen(() -> this.assertEntityNotPresent(param0, param1));
    }

    public void succeed() {
        this.testInfo.succeed();
    }

    private void ensureSingleFinalCheck() {
        if (this.finalCheckAdded) {
            throw new IllegalStateException("This test already has final clause");
        } else {
            this.finalCheckAdded = true;
        }
    }

    public void succeedIf(Runnable param0) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(0L, param0).thenSucceed();
    }

    public void succeedWhen(Runnable param0) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(param0).thenSucceed();
    }

    public void succeedOnTickWhen(int param0, Runnable param1) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil((long)param0, param1).thenSucceed();
    }

    public void runAtTickTime(long param0, Runnable param1) {
        this.testInfo.setRunAtTickTime(param0, param1);
    }

    public void runAfterDelay(long param0, Runnable param1) {
        this.runAtTickTime(this.testInfo.getTick() + param0, param1);
    }

    public void randomTick(BlockPos param0) {
        BlockPos var0 = this.absolutePos(param0);
        ServerLevel var1 = this.getLevel();
        var1.getBlockState(var0).randomTick(var1, var0, var1.random);
    }

    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        BlockPos var0 = this.absolutePos(new BlockPos(param1, 0, param2));
        return this.relativePos(this.getLevel().getHeightmapPos(param0, var0)).getY();
    }

    public void fail(String param0, BlockPos param1) {
        throw new GameTestAssertPosException(param0, this.absolutePos(param1), param1, this.getTick());
    }

    public void fail(String param0, Entity param1) {
        throw new GameTestAssertPosException(param0, param1.blockPosition(), this.relativePos(param1.blockPosition()), this.getTick());
    }

    public void fail(String param0) {
        throw new GameTestAssertException(param0);
    }

    public void failIf(Runnable param0) {
        this.testInfo.createSequence().thenWaitUntil(param0).thenFail(() -> new GameTestAssertException("Fail conditions met"));
    }

    public void failIfEver(Runnable param0) {
        LongStream.range(this.testInfo.getTick(), (long)this.testInfo.getTimeoutTicks()).forEach(param1 -> this.testInfo.setRunAtTickTime(param1, param0::run));
    }

    public GameTestSequence startSequence() {
        return this.testInfo.createSequence();
    }

    public BlockPos absolutePos(BlockPos param0) {
        BlockPos var0 = this.testInfo.getStructureBlockPos();
        BlockPos var1 = var0.offset(param0);
        return StructureTemplate.transform(var1, Mirror.NONE, this.testInfo.getRotation(), var0);
    }

    public BlockPos relativePos(BlockPos param0) {
        BlockPos var0 = this.testInfo.getStructureBlockPos();
        Rotation var1 = this.testInfo.getRotation().getRotated(Rotation.CLOCKWISE_180);
        BlockPos var2 = StructureTemplate.transform(param0, Mirror.NONE, var1, var0);
        return var2.subtract(var0);
    }

    public Vec3 absoluteVec(Vec3 param0) {
        Vec3 var0 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
        return StructureTemplate.transform(var0.add(param0), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
    }

    public Vec3 relativeVec(Vec3 param0) {
        Vec3 var0 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
        return StructureTemplate.transform(param0.subtract(var0), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
    }

    public void assertTrue(boolean param0, String param1) {
        if (!param0) {
            throw new GameTestAssertException(param1);
        }
    }

    public void assertFalse(boolean param0, String param1) {
        if (param0) {
            throw new GameTestAssertException(param1);
        }
    }

    public long getTick() {
        return this.testInfo.getTick();
    }

    public AABB getBounds() {
        return this.testInfo.getStructureBounds();
    }

    private AABB getRelativeBounds() {
        AABB var0 = this.testInfo.getStructureBounds();
        return var0.move(BlockPos.ZERO.subtract(this.absolutePos(BlockPos.ZERO)));
    }

    public void forEveryBlockInStructure(Consumer<BlockPos> param0) {
        AABB var0 = this.getRelativeBounds();
        BlockPos.MutableBlockPos.betweenClosedStream(var0.move(0.0, 1.0, 0.0)).forEach(param0);
    }

    public void onEachTick(Runnable param0) {
        LongStream.range(this.testInfo.getTick(), (long)this.testInfo.getTimeoutTicks()).forEach(param1 -> this.testInfo.setRunAtTickTime(param1, param0::run));
    }

    public void placeAt(Player param0, ItemStack param1, BlockPos param2, Direction param3) {
        BlockPos var0 = this.absolutePos(param2.relative(param3));
        BlockHitResult var1 = new BlockHitResult(Vec3.atCenterOf(var0), param3, var0, false);
        UseOnContext var2 = new UseOnContext(param0, InteractionHand.MAIN_HAND, var1);
        param1.useOn(var2);
    }
}
