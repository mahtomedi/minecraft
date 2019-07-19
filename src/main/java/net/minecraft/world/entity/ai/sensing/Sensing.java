package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class Sensing {
    private final Mob mob;
    private final List<Entity> seen = Lists.newArrayList();
    private final List<Entity> unseen = Lists.newArrayList();

    public Sensing(Mob param0) {
        this.mob = param0;
    }

    public void tick() {
        this.seen.clear();
        this.unseen.clear();
    }

    public boolean canSee(Entity param0) {
        if (this.seen.contains(param0)) {
            return true;
        } else if (this.unseen.contains(param0)) {
            return false;
        } else {
            this.mob.level.getProfiler().push("canSee");
            boolean var0 = this.mob.canSee(param0);
            this.mob.level.getProfiler().pop();
            if (var0) {
                this.seen.add(param0);
            } else {
                this.unseen.add(param0);
            }

            return var0;
        }
    }
}
