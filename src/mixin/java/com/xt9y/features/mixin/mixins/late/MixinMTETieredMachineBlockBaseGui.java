package com.xt9y.features.mixin.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import gregtech.common.gui.modularui.singleblock.base.MTETieredMachineBlockBaseGui;

@Mixin(value = MTETieredMachineBlockBaseGui.class, remap = false)
public interface MixinMTETieredMachineBlockBaseGui {

    @Accessor("machine")
    MTETieredMachineBlock getMachine();
}
