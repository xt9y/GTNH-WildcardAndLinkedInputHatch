package com.xt9y.features.mixin.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME.PatternSlot;

@Mixin(value = MTEHatchCraftingInputME.class, remap = false)
public abstract class MixinCRIBCircuitFromPattern {

    @Shadow(remap = false)
    public abstract AENetworkProxy getProxy();

    @Shadow(remap = false)
    private BaseActionSource requestSource;

    @Shadow(remap = false)
    private PatternSlot<MTEHatchCraftingInputME>[] internalInventory;

    @Unique
    private static final ItemList[] NON_CONSUMABLE_SHAPES = { ItemList.Shape_Mold_Bottle, ItemList.Shape_Mold_Plate,
        ItemList.Shape_Mold_Ingot, ItemList.Shape_Mold_Casing, ItemList.Shape_Mold_Gear, ItemList.Shape_Mold_Gear_Small,
        ItemList.Shape_Mold_Credit, ItemList.Shape_Mold_Nugget, ItemList.Shape_Mold_Block, ItemList.Shape_Mold_Ball,
        ItemList.Shape_Mold_Bun, ItemList.Shape_Mold_Bread, ItemList.Shape_Mold_Baguette, ItemList.Shape_Mold_Cylinder,
        ItemList.Shape_Mold_Anvil, ItemList.Shape_Mold_Arrow, ItemList.Shape_Mold_Name, ItemList.Shape_Mold_Rod,
        ItemList.Shape_Mold_Bolt, ItemList.Shape_Mold_Round, ItemList.Shape_Mold_Screw, ItemList.Shape_Mold_Ring,
        ItemList.Shape_Mold_Rod_Long, ItemList.Shape_Mold_Rotor, ItemList.Shape_Mold_Turbine_Blade,
        ItemList.Shape_Mold_Pipe_Tiny, ItemList.Shape_Mold_Pipe_Small, ItemList.Shape_Mold_Pipe_Medium,
        ItemList.Shape_Mold_Pipe_Large, ItemList.Shape_Mold_Pipe_Huge, ItemList.Shape_Mold_ToolHeadDrill,
        ItemList.Shape_Slicer_Flat, ItemList.Shape_Slicer_Stripes, ItemList.Shape_Extruder_Bottle,
        ItemList.Shape_Extruder_Plate, ItemList.Shape_Extruder_Cell, ItemList.Shape_Extruder_Ring,
        ItemList.Shape_Extruder_Rod, ItemList.Shape_Extruder_Bolt, ItemList.Shape_Extruder_Ingot,
        ItemList.Shape_Extruder_Wire, ItemList.Shape_Extruder_Casing, ItemList.Shape_Extruder_Pipe_Tiny,
        ItemList.Shape_Extruder_Pipe_Small, ItemList.Shape_Extruder_Pipe_Medium, ItemList.Shape_Extruder_Pipe_Large,
        ItemList.Shape_Extruder_Pipe_Huge, ItemList.Shape_Extruder_Block, ItemList.Shape_Extruder_Sword,
        ItemList.Shape_Extruder_Pickaxe, ItemList.Shape_Extruder_Shovel, ItemList.Shape_Extruder_Axe,
        ItemList.Shape_Extruder_Hoe, ItemList.Shape_Extruder_Hammer, ItemList.Shape_Extruder_File,
        ItemList.Shape_Extruder_Saw, ItemList.Shape_Extruder_Gear, ItemList.Shape_Extruder_Rotor,
        ItemList.Shape_Extruder_Turbine_Blade, ItemList.Shape_Extruder_Small_Gear,
        ItemList.Shape_Extruder_ToolHeadDrill, ItemList.Circuit_Integrated };

    @Unique
    private static boolean gt5u$isNonConsumableShape(ItemStack stack) {
        for (ItemList shape : NON_CONSUMABLE_SHAPES) {
            if (shape.isStackEqual(stack, false, true)) return true;
        }
        return false;
    }

    @Inject(method = "onPostTick", at = @At("TAIL"))
    private void gt5u$onPostTick(IGregTechTileEntity base, long timer, CallbackInfo ci) {
        if (!base.isServerSide()) return;

        BaseActionSource src = requestSource;
        if (src == null) {
            src = new MachineSource((IActionHost) ((IMetaTileEntity) this).getBaseMetaTileEntity());
        }

        for (PatternSlot<MTEHatchCraftingInputME> slot : internalInventory) {
            if (slot == null) continue;

            ItemStack[] items = slot.getItemInputs();
            boolean hasRealItems = false;
            boolean hasNonConsumableShapes = false;

            for (ItemStack stack : items) {
                if (gt5u$isNonConsumableShape(stack)) {
                    hasNonConsumableShapes = true;
                } else {
                    hasRealItems = true;
                }
            }

            if (hasRealItems || !hasNonConsumableShapes || !slot.isFluidEmpty()) continue;

            try {
                slot.refund(getProxy(), src, false);
            } catch (GridAccessException ignored) {}
        }
    }
}
