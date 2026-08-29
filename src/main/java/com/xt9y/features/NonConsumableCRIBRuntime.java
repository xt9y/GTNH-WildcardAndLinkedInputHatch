package com.xt9y.features;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.item.ItemStack;

import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;

/**
 * Runtime bridge between the CRIB reservation mixin and GregTech's ProcessingLogic.
 *
 * The real NC item is kept out of the CRIB's consumable inventory. ProcessingLogic receives disposable copies only,
 * so GregTech can validate and decrement them without touching the reserved item.
 */
public final class NonConsumableCRIBRuntime {

    private static final Map<IDualInputInventoryWithPattern, ItemStack[]> SYNTHETIC_INPUTS = Collections
        .synchronizedMap(new WeakHashMap<>());

    private NonConsumableCRIBRuntime() {}

    public static void set(IDualInputInventoryWithPattern slot, ItemStack[] inputs) {
        if (slot == null || inputs == null || inputs.length == 0) {
            clear(slot);
            return;
        }

        ItemStack[] copy = new ItemStack[inputs.length];
        int count = 0;
        for (ItemStack input : inputs) {
            if (input == null) continue;
            ItemStack stack = input.copy();
            // NC inputs must not cap machine parallelism. The real amount reserved from ME is tracked separately.
            stack.stackSize = Integer.MAX_VALUE;
            copy[count++] = stack;
        }

        if (count == 0) {
            clear(slot);
        } else if (count == copy.length) {
            SYNTHETIC_INPUTS.put(slot, copy);
        } else {
            ItemStack[] compact = new ItemStack[count];
            System.arraycopy(copy, 0, compact, 0, count);
            SYNTHETIC_INPUTS.put(slot, compact);
        }
    }

    public static ItemStack[] get(IDualInputInventoryWithPattern slot) {
        ItemStack[] inputs = SYNTHETIC_INPUTS.get(slot);
        if (inputs == null || inputs.length == 0) return new ItemStack[0];

        ItemStack[] copy = new ItemStack[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            copy[i] = inputs[i].copy();
        }
        return copy;
    }

    public static void clear(IDualInputInventoryWithPattern slot) {
        if (slot != null) SYNTHETIC_INPUTS.remove(slot);
    }
}
