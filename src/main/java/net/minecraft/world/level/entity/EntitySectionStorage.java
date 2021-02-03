package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.Spliterators;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public class EntitySectionStorage<T extends EntityAccess> {
    private final Class<T> entityClass;
    private final Long2ObjectFunction<Visibility> intialSectionVisibility;
    private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap<>();
    private final LongSortedSet sectionIds = new LongAVLTreeSet();

    public EntitySectionStorage(Class<T> param0, Long2ObjectFunction<Visibility> param1) {
        this.entityClass = param0;
        this.intialSectionVisibility = param1;
    }

    public void forEachAccessibleSection(AABB param0, Consumer<EntitySection<T>> param1) {
        int var0 = SectionPos.posToSectionCoord(param0.minX - 2.0);
        int var1 = SectionPos.posToSectionCoord(param0.minY - 2.0);
        int var2 = SectionPos.posToSectionCoord(param0.minZ - 2.0);
        int var3 = SectionPos.posToSectionCoord(param0.maxX + 2.0);
        int var4 = SectionPos.posToSectionCoord(param0.maxY + 2.0);
        int var5 = SectionPos.posToSectionCoord(param0.maxZ + 2.0);

        for(int var6 = var0; var6 <= var3; ++var6) {
            long var7 = SectionPos.asLong(var6, 0, 0);
            long var8 = SectionPos.asLong(var6, -1, -1);
            LongIterator var9 = this.sectionIds.subSet(var7, var8 + 1L).iterator();

            while(var9.hasNext()) {
                long var10 = var9.nextLong();
                int var11 = SectionPos.y(var10);
                int var12 = SectionPos.z(var10);
                if (var11 >= var1 && var11 <= var4 && var12 >= var2 && var12 <= var5) {
                    EntitySection<T> var13 = this.sections.get(var10);
                    if (var13 != null && var13.getStatus().isAccessible()) {
                        param1.accept(var13);
                    }
                }
            }
        }

    }

    public LongStream getExistingSectionPositionsInChunk(long param0) {
        int var0 = ChunkPos.getX(param0);
        int var1 = ChunkPos.getZ(param0);
        LongSortedSet var2 = this.getChunkSections(var0, var1);
        if (var2.isEmpty()) {
            return LongStream.empty();
        } else {
            OfLong var3 = var2.iterator();
            return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(var3, 1301), false);
        }
    }

    private LongSortedSet getChunkSections(int param0, int param1) {
        long var0 = SectionPos.asLong(param0, 0, param1);
        long var1 = SectionPos.asLong(param0, -1, param1);
        return this.sectionIds.subSet(var0, var1 + 1L);
    }

    public Stream<EntitySection<T>> getExistingSectionsInChunk(long param0) {
        return this.getExistingSectionPositionsInChunk(param0).mapToObj(this.sections::get).filter(Objects::nonNull);
    }

    private static long getChunkKeyFromSectionKey(long param0) {
        return ChunkPos.asLong(SectionPos.x(param0), SectionPos.z(param0));
    }

    public EntitySection<T> getOrCreateSection(long param0) {
        return this.sections.computeIfAbsent(param0, this::createSection);
    }

    @Nullable
    public EntitySection<T> getSection(long param0) {
        return this.sections.get(param0);
    }

    private EntitySection<T> createSection(long param0x) {
        long var0 = getChunkKeyFromSectionKey(param0x);
        Visibility var1 = this.intialSectionVisibility.get(var0);
        this.sectionIds.add(param0x);
        return new EntitySection<>(this.entityClass, var1);
    }

    public LongSet getAllChunksWithExistingSections() {
        LongSet var0 = new LongOpenHashSet();
        this.sections.keySet().forEach(param1 -> var0.add(getChunkKeyFromSectionKey(param1)));
        return var0;
    }

    private static <T extends EntityAccess> Predicate<T> createBoundingBoxCheck(AABB param0) {
        return param1 -> param1.getBoundingBox().intersects(param0);
    }

    public void getEntities(AABB param0, Consumer<T> param1) {
        this.forEachAccessibleSection(param0, param2 -> param2.getEntities(createBoundingBoxCheck(param0), param1));
    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> param0, AABB param1, Consumer<U> param2) {
        this.forEachAccessibleSection(param1, param3 -> param3.getEntities(param0, createBoundingBoxCheck(param1), param2));
    }

    public void remove(long param0) {
        this.sections.remove(param0);
        this.sectionIds.remove(param0);
    }

    public int count() {
        return this.sectionIds.size();
    }
}
