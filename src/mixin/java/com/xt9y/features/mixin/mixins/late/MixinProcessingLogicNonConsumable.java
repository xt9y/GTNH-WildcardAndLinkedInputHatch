package com.xt9y.features.mixin.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.xt9y.features.NonConsumableCRIBRuntime;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;

@Mixin(value = ProcessingLogic.class, remap = false)
public abstract class MixinProcessingLogicNonConsumable {

    @Shadow(remap = false)
    protected ItemStack[] inputItems;

    @Shadow(remap = false)
    protected IDualInputInventoryWithPattern activeDualInv;

    @Inject(method = "process", at = @At("HEAD"))
    private void xt9y$appendSyntheticNonConsumables(CallbackInfoReturnable<CheckRecipeResult> cir) {
        if (activeDualInv == null) return;

        ItemStack[] synthetic = NonConsumableCRIBRuntime.get(activeDualInv);
        if (synthetic.length == 0) return;

        int realLength = inputItems == null ? 0 : inputItems.length;
        ItemStack[] combined = new ItemStack[realLength + synthetic.length];
        if (realLength > 0) System.arraycopy(inputItems, 0, combined, 0, realLength);
        System.arraycopy(synthetic, 0, combined, realLength, synthetic.length);
        inputItems = combined;
    }
}
