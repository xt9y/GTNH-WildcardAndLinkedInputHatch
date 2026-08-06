package com.xt9y.features;

import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.util.GTRecipeBuilder.INGOTS;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GTOreDictUnificator;

class ComponentRecipeLoader implements Runnable {

    @Override
    public void run() {
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.Hatch_Input_Multi_2x2_LuV.get(1L),
                ItemList.Emitter_LuV.get(1L),
                ItemList.Sensor_LuV.get(1L),
                GTOreDictUnificator.get(OrePrefixes.plateDense, Materials.Enderium, 1L))
            .circuit(12)
            .itemOutputs(XTItemList.LinkedInputHatch.get(1L))
            .fluidInputs(Materials.Polybenzimidazole.getMolten(1 * INGOTS))
            .duration(30 * SECONDS)
            .eut(TierEU.RECIPE_LuV)
            .addTo(assemblerRecipes);
    }
}
