package com.xt9y.features.mixin.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import appeng.api.storage.data.IAEItemStack;

@Mixin(targets = "gregtech.common.tileentities.machines.MTEHatchCraftingInputME$PatternSlot", remap = false)
public interface AccessorCRIBPatternSlot {

    @Invoker("insertItem")
    void xt9y$insertItem(IAEItemStack inserted);
}
