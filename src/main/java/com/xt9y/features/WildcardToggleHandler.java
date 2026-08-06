package com.xt9y.features;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import com.xt9y.features.api.IWildcardToggleable;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;

public class WildcardToggleHandler {

    public static void init() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new WildcardToggleHandler());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.entityPlayer.isSneaking()) return;
        if (event.world.isRemote) return;

        ItemStack stack = event.entityPlayer.getHeldItem();
        if (stack == null) return;
        if (!isSoftMallet(stack)) return;

        TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
        if (tile == null) return;

        // GT machines use BaseMetaTileEntity wrapping an IMetaTileEntity
        if (tile instanceof BaseMetaTileEntity baseTile) {
            IMetaTileEntity meta = baseTile.getMetaTileEntity();
            if (meta instanceof IWildcardToggleable toggleable) {
                toggleWildcard(event, toggleable);
                return;
            }
        }

        if (!(tile instanceof IWildcardToggleable toggleable)) return;
        toggleWildcard(event, toggleable);
    }

    private static void toggleWildcard(PlayerInteractEvent event, IWildcardToggleable toggleable) {
        boolean newState = !toggleable.isWildcardEnabled();
        toggleable.setWildcardEnabled(newState);

        EntityPlayer player = event.entityPlayer;
        player.addChatMessage(
            new ChatComponentTranslation(
                "ggfab.wildcard.toggle",
                newState ? EnumChatFormatting.GREEN + "ON" + EnumChatFormatting.RESET
                    : EnumChatFormatting.RED + "OFF" + EnumChatFormatting.RESET));

        event.useItem = net.minecraftforge.event.entity.player.PlayerInteractEvent.Result.DENY;
        event.useBlock = net.minecraftforge.event.entity.player.PlayerInteractEvent.Result.DENY;
    }

    private static boolean isSoftMallet(ItemStack stack) {
        return gregtech.api.util.GTUtility.isStackInList(stack, gregtech.api.GregTechAPI.sSoftMalletList);
    }
}
