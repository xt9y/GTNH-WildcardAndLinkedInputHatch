package com.xt9y.features;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.xt9y.features.mte.MTELinkedInputHatch;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import gregtech.api.GregTechAPI;

@Mod(
    modid = XT9YFeatures.MODID,
    version = Tags.VERSION,
    name = XT9YFeatures.MODNAME,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-before:gregtech")
public class XT9YFeatures {

    public static final String MODID = "xt9yfeatures";
    public static final String MODNAME = "XT9Y Features";
    public static final Logger LOGGER = LogManager.getLogger("XT9Y-Features");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Must run before GT's own preInit so the runnables below are picked up by GT.
        GregTechAPI.sAfterGTPreload.add(() -> {
            XTItemList.LinkedInputHatch.set(
                new MTELinkedInputHatch(
                    MetaTileEntityIDRegistry.register("LinkedInputHatch", 13534),
                    "ggfab.machine.linked_input_hatch",
                    "Linked Input Hatch",
                    6).getStackForm(1));
        });
        GregTechAPI.sBeforeGTPostload.add(new ComponentRecipeLoader());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        WildcardToggleHandler.init();
        WildcardTooltipHandler.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
}
