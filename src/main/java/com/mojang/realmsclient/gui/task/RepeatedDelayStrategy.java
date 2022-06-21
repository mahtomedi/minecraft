package com.mojang.realmsclient.gui.task;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public interface RepeatedDelayStrategy {
    RepeatedDelayStrategy CONSTANT = new RepeatedDelayStrategy() {
        @Override
        public long delayCyclesAfterSuccess() {
            return 1L;
        }

        @Override
        public long delayCyclesAfterFailure() {
            return 1L;
        }
    };

    long delayCyclesAfterSuccess();

    long delayCyclesAfterFailure();

    static RepeatedDelayStrategy exponentialBackoff(final int param0) {
        return new RepeatedDelayStrategy() {
            private static final Logger LOGGER = LogUtils.getLogger();
            private int failureCount;

            @Override
            public long delayCyclesAfterSuccess() {
                this.failureCount = 0;
                return 1L;
            }

            @Override
            public long delayCyclesAfterFailure() {
                ++this.failureCount;
                long var0 = Math.min(1L << this.failureCount, (long)param0);
                LOGGER.debug("Skipping for {} extra cycles", var0);
                return var0;
            }
        };
    }
}
