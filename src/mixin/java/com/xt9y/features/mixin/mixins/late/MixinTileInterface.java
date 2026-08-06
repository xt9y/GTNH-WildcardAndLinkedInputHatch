package com.xt9y.features.mixin.mixins.late;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.xt9y.features.api.IWildcardToggleable;

import appeng.helpers.DualityInterface;
import appeng.tile.misc.TileInterface;

@Mixin(value = TileInterface.class, remap = false)
@Implements(@Interface(iface = IWildcardToggleable.class, prefix = "xt9y$"))
public abstract class MixinTileInterface {

    @Accessor("duality")
    public abstract DualityInterface getDuality();

    public boolean xt9y$isWildcardEnabled() {
        return ((IWildcardToggleable) getDuality()).isWildcardEnabled();
    }

    public void xt9y$setWildcardEnabled(boolean enabled) {
        ((IWildcardToggleable) getDuality()).setWildcardEnabled(enabled);
    }
}
