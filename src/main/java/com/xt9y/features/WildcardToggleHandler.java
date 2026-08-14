package com.xt9y.features;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

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
        System.err.println(
            "[XT9Y] EVENT: action=" + event.action
                + " remote="
                + event.world.isRemote
                + " pos="
                + event.x
                + ","
                + event.y
                + ","
                + event.z
                + " sneak="
                + event.entityPlayer.isSneaking());

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if (event.world.isRemote) return;
        System.err.println("[XT9Y] SERVER RIGHT_CLICK_BLOCK");

        if (!event.entityPlayer.isSneaking()) {
            System.err.println("[XT9Y] NOT sneaking - returning");
            return;
        }
        System.err.println("[XT9Y] IS sneaking");

        ItemStack stack = event.entityPlayer.getHeldItem();
        if (stack == null) {
            System.err.println("[XT9Y] No held item");
            return;
        }
        System.err.println("[XT9Y] Held item: " + stack.getDisplayName());

        if (!isSoftMallet(stack)) {
            System.err.println("[XT9Y] NOT a soft mallet");
            return;
        }
        System.err.println("[XT9Y] IS soft mallet");

        TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
        if (tile == null) {
            System.err.println("[XT9Y] No tile entity");
            return;
        }
        System.err.println(
            "[XT9Y] Tile: " + tile.getClass()
                .getName());

        // GT machines wrap their meta in BaseMetaTileEntity
        IMetaTileEntity meta = null;
        if (tile instanceof BaseMetaTileEntity baseTile) {
            meta = baseTile.getMetaTileEntity();
            System.err.println(
                "[XT9Y] Meta: " + (meta != null ? meta.getClass()
                    .getName() : "null"));
            if (meta instanceof IWildcardToggleable toggleable) {
                System.err.println("[XT9Y] Meta is IWildcardToggleable - TOGGLING");
                toggleWildcard(event, toggleable);
                return;
            }
            System.err.println("[XT9Y] Meta is NOT IWildcardToggleable");
        }

        // Direct tile implements IWildcardToggleable
        if (tile instanceof IWildcardToggleable toggleable) {
            System.err.println("[XT9Y] Tile is IWildcardToggleable - TOGGLING");
            toggleWildcard(event, toggleable);
            return;
        }
        System.err.println("[XT9Y] NO IWildcardToggleable found");

        // Attempt toggle on meta's enableWildcardExpansion field (CRIB hatch)
        if (meta != null) {
            try {
                java.lang.reflect.Field field = findWildcardField(meta.getClass());
                if (field == null) throw new NoSuchFieldException();
                field.setAccessible(true);
                boolean current = field.getBoolean(meta);
                field.setBoolean(meta, !current);
                boolean newState = !current;
                System.err.println("[XT9Y] Toggled CRIB wildcard expansion to " + newState);
                EntityPlayer player = event.entityPlayer;
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "ggfab.wildcard.toggle",
                        newState ? EnumChatFormatting.GREEN + "ON" + EnumChatFormatting.RESET
                            : EnumChatFormatting.RED + "OFF" + EnumChatFormatting.RESET));
                event.useItem = PlayerInteractEvent.Result.DENY;
                event.useBlock = PlayerInteractEvent.Result.DENY;
                return;
            } catch (Exception ignored) {}
        }

        // Fallback for AE2 Interface (TileInterface) via its duality
        try {
            java.lang.reflect.Method getDuality = tile.getClass()
                .getMethod("getDuality");
            Object dual = getDuality.invoke(tile);
            if (dual instanceof IWildcardToggleable toggleableDual) {
                System.err.println("[XT9Y] Duality is IWildcardToggleable - TOGGLING");
                toggleWildcard(event, toggleableDual);
                return;
            }
        } catch (Exception ignored) {}

    }

    private static java.lang.reflect.Field findWildcardField(Class<?> clazz) {
        for (String name : new String[] { "xt9y$enableWildcardExpansion", "enableWildcardExpansion" }) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(name);
                if (field.getType() == boolean.class) return field;
            } catch (NoSuchFieldException ignored) {}
        }
        Class<?> superClass = clazz.getSuperclass();
        return superClass != null ? findWildcardField(superClass) : null;
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

        event.useItem = PlayerInteractEvent.Result.DENY;
        event.useBlock = PlayerInteractEvent.Result.DENY;
    }

    private static boolean isSoftMallet(ItemStack stack) {
        return gregtech.api.util.GTUtility.isStackInList(stack, gregtech.api.GregTechAPI.sSoftMalletList);
    }
}
