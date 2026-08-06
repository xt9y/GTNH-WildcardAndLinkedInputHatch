package com.xt9y.features.gui;

import static net.minecraft.util.StatCollector.translateToLocal;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.fluid.FluidStackTank;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.xt9y.features.mte.MTELinkedInputHatch;

import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.hatch.base.MTEHatchBaseGui;

public class MTELinkedInputHatchGui extends MTEHatchBaseGui<MTELinkedInputHatch> {

    public MTELinkedInputHatchGui(MTELinkedInputHatch hatch) {
        super(hatch);
    }

    @Override
    protected ParentWidget<?> createContentSection(ModularPanel panel, PanelSyncManager syncManager) {
        StringSyncValue channelSyncer = new StringSyncValue(machine::getChannel, machine::setChannel).allowC2S();
        BooleanSyncValue isPrivateSyncer = new BooleanSyncValue(machine::isPrivate, machine::setPrivate).allowC2S();

        Flow mainColumn = Flow.column()
            .coverChildren()
            .childPadding(3)
            .paddingLeft(3);

        Flow inputRow = Flow.row()
            .coverChildren()
            .childPadding(3);

        // channel label
        inputRow.child(
            IKey.lang("ggfab.gui.linked_input_bus.channel")
                .asWidget());

        // channel input field
        inputRow.child(
            new TextFieldWidget().value(channelSyncer)
                .width(60)
                .tooltip(t -> t.addLine(translateToLocal("ggfab.tooltip.linked_input_hatch.change_freq_warn"))));

        // private label
        inputRow.child(
            IKey.lang("ggfab.gui.linked_input_bus.private")
                .asWidget());

        // private toggle button
        inputRow.child(
            new ToggleButton().value(isPrivateSyncer)
                .size(18)
                .overlay(true, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
                .overlay(false, GTGuiTextures.OVERLAY_BUTTON_CROSS)
                .tooltip(t -> t.addLine(translateToLocal("ggfab.tooltip.linked_input_hatch.private"))));

        mainColumn.child(inputRow);

        // fluid tanks
        mainColumn.child(createFluidSlots(syncManager));

        return super.createContentSection(panel, syncManager).child(mainColumn);
    }

    private IWidget createFluidSlots(PanelSyncManager syncManager) {
        FluidStackTank[] fluidTanks = machine.getFluidTanks();
        return new Grid().coverChildren()
            .gridOfWidthHeight(
                2,
                2,
                ($x, $y, index) -> new FluidSlot().syncHandler(new FluidSlotSyncHandler(fluidTanks[index])))
            .center();
    }
}
