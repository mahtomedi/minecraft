package net.minecraft.world.level.border;

public interface BorderChangeListener {
    void onBorderSizeSet(WorldBorder var1, double var2);

    void onBorderSizeLerping(WorldBorder var1, double var2, double var4, long var6);

    void onBorderCenterSet(WorldBorder var1, double var2, double var4);

    void onBorderSetWarningTime(WorldBorder var1, int var2);

    void onBorderSetWarningBlocks(WorldBorder var1, int var2);

    void onBorderSetDamagePerBlock(WorldBorder var1, double var2);

    void onBorderSetDamageSafeZOne(WorldBorder var1, double var2);

    public static class DelegateBorderChangeListener implements BorderChangeListener {
        private final WorldBorder worldBorder;

        public DelegateBorderChangeListener(WorldBorder param0) {
            this.worldBorder = param0;
        }

        @Override
        public void onBorderSizeSet(WorldBorder param0, double param1) {
            this.worldBorder.setSize(param1);
        }

        @Override
        public void onBorderSizeLerping(WorldBorder param0, double param1, double param2, long param3) {
            this.worldBorder.lerpSizeBetween(param1, param2, param3);
        }

        @Override
        public void onBorderCenterSet(WorldBorder param0, double param1, double param2) {
            this.worldBorder.setCenter(param1, param2);
        }

        @Override
        public void onBorderSetWarningTime(WorldBorder param0, int param1) {
            this.worldBorder.setWarningTime(param1);
        }

        @Override
        public void onBorderSetWarningBlocks(WorldBorder param0, int param1) {
            this.worldBorder.setWarningBlocks(param1);
        }

        @Override
        public void onBorderSetDamagePerBlock(WorldBorder param0, double param1) {
            this.worldBorder.setDamagePerBlock(param1);
        }

        @Override
        public void onBorderSetDamageSafeZOne(WorldBorder param0, double param1) {
            this.worldBorder.setDamageSafeZone(param1);
        }
    }
}
