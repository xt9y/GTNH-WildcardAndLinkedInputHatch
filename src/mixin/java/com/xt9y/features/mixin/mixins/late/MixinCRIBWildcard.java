package com.xt9y.features.mixin.mixins.late;

import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.xt9y.features.WildcardPatternHelper;
import com.xt9y.features.api.IWildcardToggleable;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.items.misc.ItemEncodedPattern;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.CommonMetaTileEntity;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME.PatternSlot;

@Mixin(value = MTEHatchCraftingInputME.class, remap = false)
@Implements(@Interface(iface = IWildcardToggleable.class, prefix = "xt9y$"))
public abstract class MixinCRIBWildcard {

    @Shadow(remap = false)
    private PatternSlot<MTEHatchCraftingInputME>[] internalInventory;

    @Shadow(remap = false)
    private Map<ICraftingPatternDetails, PatternSlot<MTEHatchCraftingInputME>> patternDetailsPatternSlotMap;

    @Shadow(remap = false)
    private boolean needPatternSync;

    @Shadow(remap = false)
    public abstract boolean isActive();

    @Unique
    private boolean xt9y$enableWildcardExpansion = false;

    public boolean xt9y$isWildcardEnabled() {
        return xt9y$enableWildcardExpansion;
    }

    public void xt9y$setWildcardEnabled(boolean val) {
        if (xt9y$enableWildcardExpansion != val) {
            xt9y$enableWildcardExpansion = val;
            needPatternSync = true;
        }
    }

    @Inject(method = "saveNBTData", at = @At("TAIL"))
    private void gt5u$onSaveNBT(NBTTagCompound aNBT, CallbackInfo ci) {
        aNBT.setBoolean("enableWildcardExpansion", xt9y$enableWildcardExpansion);
    }

    @Inject(method = "loadNBTData", at = @At("TAIL"))
    private void gt5u$onLoadNBT(NBTTagCompound aNBT, CallbackInfo ci) {
        if (aNBT.hasKey("enableWildcardExpansion")) {
            xt9y$enableWildcardExpansion = aNBT.getBoolean("enableWildcardExpansion");
        }
    }

    @Inject(method = "provideCrafting", at = @At("RETURN"))
    private void gt5u$onProvideCrafting(ICraftingProviderHelper craftingTracker, CallbackInfo ci) {
        if (!isActive() || !xt9y$enableWildcardExpansion) return;

        World world = ((IMetaTileEntity) this).getBaseMetaTileEntity()
            .getWorld();
        if (world == null) return;

        @SuppressWarnings("rawtypes")
        PatternSlot<MTEHatchCraftingInputME>[] inv = internalInventory;

        ItemStack[] mInv = ((CommonMetaTileEntity) (Object) this).mInventory;
        for (int slotIndex = 0; slotIndex < 4 * 9; slotIndex++) {
            ItemStack patternStack = slotIndex < mInv.length ? mInv[slotIndex] : null;
            if (patternStack == null || !(patternStack.getItem() instanceof ItemEncodedPattern)) continue;

            ICraftingPatternDetails originalDetails = ((ItemEncodedPattern) patternStack.getItem())
                .getPatternForItem(patternStack, world);
            if (originalDetails == null) continue;

            List<ICraftingPatternDetails> expanded = WildcardPatternHelper
                .expandWildcard(originalDetails, patternStack, world);
            PatternSlot<MTEHatchCraftingInputME> patternSlot = slotIndex < inv.length ? inv[slotIndex] : null;
            for (ICraftingPatternDetails expandedDetail : expanded) {
                craftingTracker
                    .addCraftingOption((appeng.api.networking.crafting.ICraftingProvider) this, expandedDetail);
                if (patternSlot != null) {
                    patternDetailsPatternSlotMap.put(expandedDetail, patternSlot);
                }
            }
        }
    }

    @Inject(method = "pushPattern", at = @At("HEAD"), cancellable = true)
    private void gt5u$onPushPattern(ICraftingPatternDetails patternDetails,
        net.minecraft.inventory.InventoryCrafting table, CallbackInfoReturnable<Boolean> cir) {
        if (xt9y$enableWildcardExpansion && patternDetailsPatternSlotMap.get(patternDetails) == null) {
            cir.setReturnValue(false);
        }
    }
}
