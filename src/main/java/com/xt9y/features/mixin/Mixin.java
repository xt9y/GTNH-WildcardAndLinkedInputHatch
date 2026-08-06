package com.xt9y.features.mixin;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixin implements IMixins {

    // spotless:off
    XT9Y_CRIB_CIRCUIT_FROM_PATTERN(new MixinBuilder("Per-pattern circuit configuration from CRIB patterns")
        .addCommonMixins("MixinCRIBCircuitFromPattern")
        .setPhase(Phase.LATE)),

    XT9Y_CRIB_WILDCARD(new MixinBuilder("Wildcard pattern expansion for CRIB")
        .addCommonMixins("MixinCRIBWildcard", "MixinCRIBGuiWildcard", "MixinMTETieredMachineBlockBaseGui")
        .setPhase(Phase.LATE)),

    XT9Y_INTERFACE_WILDCARD(new MixinBuilder("Wildcard pattern expansion for AE2 Interface")
        .addCommonMixins("MixinDualityInterface", "MixinTileInterface")
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetedMod.APPLIED_ENERGISTICS_2_UNOFFICIAL));
    // spotless:on

    private final MixinBuilder builder;

    Mixin(MixinBuilder builder) {
        this.builder = builder;
    }

    @NotNull
    @Override
    public MixinBuilder getBuilder() {
        return this.builder;
    }
}
