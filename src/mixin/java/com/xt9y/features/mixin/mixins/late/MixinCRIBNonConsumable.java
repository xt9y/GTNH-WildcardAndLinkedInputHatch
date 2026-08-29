package com.xt9y.features.mixin.mixins.late;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.xt9y.features.api.INonConsumablePatternDetails;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.Platform;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME.PatternSlot;

@Mixin(value = MTEHatchCraftingInputME.class, remap = false)
public abstract class MixinCRIBNonConsumable {

    @Shadow(remap = false)
    public abstract AENetworkProxy getProxy();

    @Shadow(remap = false)
    private BaseActionSource requestSource;

    @Shadow(remap = false)
    private PatternSlot<MTEHatchCraftingInputME>[] internalInventory;

    @Shadow(remap = false)
    private Map<ICraftingPatternDetails, PatternSlot<MTEHatchCraftingInputME>> patternDetailsPatternSlotMap;

    @Unique
    private final IdentityHashMap<PatternSlot<MTEHatchCraftingInputME>, Map<GTUtility.ItemId, Integer>>
        xt9y$borrowedNc = new IdentityHashMap<>();

    @Unique
    private BaseActionSource xt9y$requestSource() {
        if (requestSource == null) {
            requestSource = new MachineSource((IActionHost) ((IMetaTileEntity) this).getBaseMetaTileEntity());
        }
        return requestSource;
    }

    @Unique
    private Map<GTUtility.ItemId, Integer> xt9y$actualItems(PatternSlot<MTEHatchCraftingInputME> slot) {
        Map<GTUtility.ItemId, Integer> result = new HashMap<>();
        for (ItemStack stack : slot.getItemInputs()) {
            if (stack == null || stack.stackSize <= 0) continue;
            result.merge(GTUtility.ItemId.create(stack), stack.stackSize, Integer::sum);
        }
        return result;
    }

    @Unique
    private void xt9y$reconcileBorrowed(PatternSlot<MTEHatchCraftingInputME> slot) {
        Map<GTUtility.ItemId, Integer> borrowed = xt9y$borrowedNc.get(slot);
        if (borrowed == null) return;

        Map<GTUtility.ItemId, Integer> actual = xt9y$actualItems(slot);
        borrowed.replaceAll((id, amount) -> Math.min(amount, actual.getOrDefault(id, 0)));
        borrowed.entrySet()
            .removeIf(entry -> entry.getValue() <= 0);
        if (borrowed.isEmpty()) xt9y$borrowedNc.remove(slot);
    }

    @Unique
    private int xt9y$borrowedAmount(PatternSlot<MTEHatchCraftingInputME> slot, GTUtility.ItemId id) {
        xt9y$reconcileBorrowed(slot);
        Map<GTUtility.ItemId, Integer> borrowed = xt9y$borrowedNc.get(slot);
        return borrowed == null ? 0 : borrowed.getOrDefault(id, 0);
    }

    @Unique
    private void xt9y$recordBorrowed(PatternSlot<MTEHatchCraftingInputME> slot, ItemStack stack, int amount) {
        if (amount <= 0) return;
        xt9y$borrowedNc.computeIfAbsent(slot, ignored -> new HashMap<>())
            .merge(GTUtility.ItemId.create(stack), amount, Integer::sum);
    }

    @Unique
    private boolean xt9y$ensureNonConsumables(PatternSlot<MTEHatchCraftingInputME> slot,
        INonConsumablePatternDetails details) {
        IAEItemStack[] requirements = details.xt9y$getNonConsumableInputs();
        if (requirements.length == 0) return true;

        try {
            IMEMonitor<IAEItemStack> storage = getProxy().getStorage()
                .getItemInventory();
            BaseActionSource source = xt9y$requestSource();
            List<IAEItemStack> missing = new ArrayList<>();

            for (IAEItemStack required : requirements) {
                ItemStack requiredStack = required.getItemStack();
                GTUtility.ItemId id = GTUtility.ItemId.create(requiredStack);
                long amount = required.getStackSize() - xt9y$borrowedAmount(slot, id);
                if (amount <= 0) continue;

                IAEItemStack request = required.copy()
                    .setStackSize(amount);
                IAEItemStack simulated = Platform.poweredExtraction(
                    getProxy().getEnergy(), storage, request, source, Actionable.SIMULATE);
                if (simulated == null || simulated.getStackSize() < amount) return false;
                missing.add(request);
            }

            List<IAEItemStack> extracted = new ArrayList<>();
            for (IAEItemStack request : missing) {
                IAEItemStack got = Platform.poweredExtraction(getProxy().getEnergy(), storage, request, source);
                if (got == null || got.getStackSize() < request.getStackSize()) {
                    if (got != null && got.getStackSize() > 0) extracted.add(got);
                    xt9y$rollbackExtraction(slot, storage, source, extracted);
                    return false;
                }
                extracted.add(got);
            }

            for (IAEItemStack got : extracted) {
                int amount = (int) got.getStackSize();
                ItemStack stack = got.getItemStack();
                xt9y$recordBorrowed(slot, stack, amount);
                ((AccessorCRIBPatternSlot) (Object) slot).xt9y$insertItem(got);
            }
            return true;
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    @Unique
    private void xt9y$rollbackExtraction(PatternSlot<MTEHatchCraftingInputME> slot, IMEMonitor<IAEItemStack> storage,
        BaseActionSource source, List<IAEItemStack> extracted) throws GridAccessException {
        for (IAEItemStack stack : extracted) {
            IAEItemStack rest = Platform.poweredInsert(getProxy().getEnergy(), storage, stack, source);
            if (rest == null || rest.getStackSize() <= 0) continue;

            int amount = (int) rest.getStackSize();
            ItemStack item = rest.getItemStack();
            xt9y$recordBorrowed(slot, item, amount);
            ((AccessorCRIBPatternSlot) (Object) slot).xt9y$insertItem(rest);
        }
    }

    @Inject(method = "pushPattern", at = @At("HEAD"), cancellable = true)
    private void xt9y$borrowNonConsumables(ICraftingPatternDetails patternDetails,
        net.minecraft.inventory.InventoryCrafting table, CallbackInfoReturnable<Boolean> cir) {
        if (!(patternDetails instanceof INonConsumablePatternDetails ncDetails)) return;

        PatternSlot<MTEHatchCraftingInputME> slot = patternDetailsPatternSlotMap.get(patternDetails);
        if (slot == null || !xt9y$ensureNonConsumables(slot, ncDetails)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onPostTick", at = @At("TAIL"))
    private void xt9y$returnUnusedNonConsumables(IGregTechTileEntity base, long timer, CallbackInfo ci) {
        if (!base.isServerSide() || xt9y$borrowedNc.isEmpty()) return;

        xt9y$borrowedNc.keySet()
            .removeIf(slot -> !xt9y$isCurrentSlot(slot));

        BaseActionSource source = xt9y$requestSource();
        for (PatternSlot<MTEHatchCraftingInputME> slot : internalInventory) {
            if (slot == null || !xt9y$borrowedNc.containsKey(slot)) continue;

            xt9y$reconcileBorrowed(slot);
            Map<GTUtility.ItemId, Integer> borrowed = xt9y$borrowedNc.get(slot);
            if (borrowed == null || borrowed.isEmpty()) continue;
            if (!slot.isFluidEmpty()) continue;

            Map<GTUtility.ItemId, Integer> actual = xt9y$actualItems(slot);
            boolean hasConsumables = false;
            for (Map.Entry<GTUtility.ItemId, Integer> entry : actual.entrySet()) {
                if (entry.getValue() > borrowed.getOrDefault(entry.getKey(), 0)) {
                    hasConsumables = true;
                    break;
                }
            }
            if (hasConsumables) continue;

            try {
                slot.refund(getProxy(), source, false);
            } catch (GridAccessException ignored) {
                continue;
            }

            xt9y$reconcileBorrowed(slot);
        }
    }

    @Unique
    private boolean xt9y$isCurrentSlot(PatternSlot<MTEHatchCraftingInputME> candidate) {
        for (PatternSlot<MTEHatchCraftingInputME> slot : internalInventory) {
            if (slot == candidate) return true;
        }
        return false;
    }

    @Inject(method = "saveNBTData", at = @At("TAIL"))
    private void xt9y$saveBorrowedNonConsumables(NBTTagCompound nbt, CallbackInfo ci) {
        NBTTagList slots = new NBTTagList();

        for (int index = 0; index < internalInventory.length; index++) {
            PatternSlot<MTEHatchCraftingInputME> slot = internalInventory[index];
            if (slot == null) continue;
            xt9y$reconcileBorrowed(slot);

            Map<GTUtility.ItemId, Integer> borrowed = xt9y$borrowedNc.get(slot);
            if (borrowed == null || borrowed.isEmpty()) continue;

            NBTTagCompound slotTag = new NBTTagCompound();
            slotTag.setInteger("slot", index);
            NBTTagList items = new NBTTagList();

            for (Map.Entry<GTUtility.ItemId, Integer> entry : borrowed.entrySet()) {
                ItemStack stack = entry.getKey()
                    .getItemStack();
                stack.stackSize = entry.getValue();
                items.appendTag(GTUtility.saveItem(stack));
            }

            slotTag.setTag("items", items);
            slots.appendTag(slotTag);
        }

        if (slots.tagCount() > 0) nbt.setTag("xt9yBorrowedNc", slots);
        else nbt.removeTag("xt9yBorrowedNc");
    }

    @Inject(method = "loadNBTData", at = @At("TAIL"))
    private void xt9y$loadBorrowedNonConsumables(NBTTagCompound nbt, CallbackInfo ci) {
        xt9y$borrowedNc.clear();
        NBTTagList slots = nbt.getTagList("xt9yBorrowedNc", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < slots.tagCount(); i++) {
            NBTTagCompound slotTag = slots.getCompoundTagAt(i);
            int index = slotTag.getInteger("slot");
            if (index < 0 || index >= internalInventory.length) continue;

            PatternSlot<MTEHatchCraftingInputME> slot = internalInventory[index];
            if (slot == null) continue;

            NBTTagList items = slotTag.getTagList("items", Constants.NBT.TAG_COMPOUND);
            Map<GTUtility.ItemId, Integer> borrowed = new HashMap<>();
            for (int j = 0; j < items.tagCount(); j++) {
                ItemStack stack = GTUtility.loadItem(items.getCompoundTagAt(j));
                if (stack == null || stack.stackSize <= 0) continue;
                borrowed.merge(GTUtility.ItemId.create(stack), stack.stackSize, Integer::sum);
            }

            if (!borrowed.isEmpty()) xt9y$borrowedNc.put(slot, borrowed);
        }

        for (PatternSlot<MTEHatchCraftingInputME> slot : internalInventory) {
            if (slot != null) xt9y$reconcileBorrowed(slot);
        }
    }
}
