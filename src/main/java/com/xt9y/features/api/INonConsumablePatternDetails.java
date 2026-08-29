package com.xt9y.features.api;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public interface INonConsumablePatternDetails {

    IAEItemStack[] xt9y$getNonConsumableInputs();

    ICraftingPatternDetails xt9y$getDelegate();
}
