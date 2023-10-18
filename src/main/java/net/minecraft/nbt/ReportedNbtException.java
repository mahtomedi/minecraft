package net.minecraft.nbt;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

public class ReportedNbtException extends ReportedException {
    public ReportedNbtException(CrashReport param0) {
        super(param0);
    }
}
