package com.xt9y.features.mixin.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.xt9y.features.api.IWildcardToggleable;

import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.hatch.MTEHatchCraftingInputMEGui;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;

@Mixin(value = MTEHatchCraftingInputMEGui.class, remap = false)
public abstract class MixinCRIBGuiWildcard {

    @Inject(method = "createBottomLeftCornerFlow", at = @At("RETURN"), remap = false)
    private void gt5u$appendWildcardButton(ModularPanel panel, PanelSyncManager syncManager,
        CallbackInfoReturnable<Flow> cir) {
        cir.setReturnValue(
            cir.getReturnValue()
                .child(createWildcardButton()));
    }

    @Unique
    private ToggleButton createWildcardButton() {
        MTEHatchCraftingInputME machine = (MTEHatchCraftingInputME) ((MixinMTETieredMachineBlockBaseGui) (Object) this)
            .getMachine();
        IWildcardToggleable toggleable = (IWildcardToggleable) machine;
        BooleanSyncValue wildcardSync = new BooleanSyncValue(
            () -> toggleable.isWildcardEnabled(),
            val -> toggleable.setWildcardEnabled(val)).allowC2S();

        return new ToggleButton().value(wildcardSync)
            .overlay(GTGuiTextures.OVERLAY_BUTTON_BATCH_MODE_ON)
            .addTooltip(false, "Wildcard Expansion:\n§7Disabled")
            .addTooltip(true, "Wildcard Expansion:\n§7Enabled");
    }
}
