package net.minecraft.gametest.framework;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.slf4j.Logger;

public class TeamcityTestReporter implements TestReporter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Escaper ESCAPER = Escapers.builder()
        .addEscape('\'', "|'")
        .addEscape('\n', "|n")
        .addEscape('\r', "|r")
        .addEscape('|', "||")
        .addEscape('[', "|[")
        .addEscape(']', "|]")
        .build();

    @Override
    public void onTestFailed(GameTestInfo param0) {
        String var0 = ESCAPER.escape(param0.getTestName());
        String var1 = ESCAPER.escape(param0.getError().getMessage());
        String var2 = ESCAPER.escape(Util.describeError(param0.getError()));
        LOGGER.info("##teamcity[testStarted name='{}']", var0);
        if (param0.isRequired()) {
            LOGGER.info("##teamcity[testFailed name='{}' message='{}' details='{}']", var0, var1, var2);
        } else {
            LOGGER.info("##teamcity[testIgnored name='{}' message='{}' details='{}']", var0, var1, var2);
        }

        LOGGER.info("##teamcity[testFinished name='{}' duration='{}']", var0, param0.getRunTime());
    }

    @Override
    public void onTestSuccess(GameTestInfo param0) {
        String var0 = ESCAPER.escape(param0.getTestName());
        LOGGER.info("##teamcity[testStarted name='{}']", var0);
        LOGGER.info("##teamcity[testFinished name='{}' duration='{}']", var0, param0.getRunTime());
    }
}
