package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class PoiTypeTagsProvider extends TagsProvider<PoiType> {
    public PoiTypeTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        super(param0, Registries.POINT_OF_INTEREST_TYPE, param1);
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
            .add(
                PoiTypes.ARMORER,
                PoiTypes.BUTCHER,
                PoiTypes.CARTOGRAPHER,
                PoiTypes.CLERIC,
                PoiTypes.FARMER,
                PoiTypes.FISHERMAN,
                PoiTypes.FLETCHER,
                PoiTypes.LEATHERWORKER,
                PoiTypes.LIBRARIAN,
                PoiTypes.MASON,
                PoiTypes.SHEPHERD,
                PoiTypes.TOOLSMITH,
                PoiTypes.WEAPONSMITH
            );
        this.tag(PoiTypeTags.VILLAGE).addTag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(PoiTypes.HOME, PoiTypes.MEETING);
        this.tag(PoiTypeTags.BEE_HOME).add(PoiTypes.BEEHIVE, PoiTypes.BEE_NEST);
    }
}
