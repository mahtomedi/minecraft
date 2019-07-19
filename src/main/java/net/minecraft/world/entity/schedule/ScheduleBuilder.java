package net.minecraft.world.entity.schedule;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleBuilder {
    private final Schedule schedule;
    private final List<ScheduleBuilder.ActivityTransition> transitions = Lists.newArrayList();

    public ScheduleBuilder(Schedule param0) {
        this.schedule = param0;
    }

    public ScheduleBuilder changeActivityAt(int param0, Activity param1) {
        this.transitions.add(new ScheduleBuilder.ActivityTransition(param0, param1));
        return this;
    }

    public Schedule build() {
        this.transitions
            .stream()
            .map(ScheduleBuilder.ActivityTransition::getActivity)
            .collect(Collectors.toSet())
            .forEach(this.schedule::ensureTimelineExistsFor);
        this.transitions.forEach(param0 -> {
            Activity var0 = param0.getActivity();
            this.schedule.getAllTimelinesExceptFor(var0).forEach(param1 -> param1.addKeyframe(param0.getTime(), 0.0F));
            this.schedule.getTimelineFor(var0).addKeyframe(param0.getTime(), 1.0F);
        });
        return this.schedule;
    }

    static class ActivityTransition {
        private final int time;
        private final Activity activity;

        public ActivityTransition(int param0, Activity param1) {
            this.time = param0;
            this.activity = param1;
        }

        public int getTime() {
            return this.time;
        }

        public Activity getActivity() {
            return this.activity;
        }
    }
}
