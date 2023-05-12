package com.aieis.cctind.registry;

import com.aieis.cctind.peripherals.FirearmPeripheral;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import net.minecraft.util.ResourceLocation;

import static com.tac.guns.init.ModItems.DEAGLE_357;


public class ComputerCraftRegister {
    public static void registerTurtleUpgrades(){
        ComputerCraftAPI.registerTurtleUpgrade(new FirearmPeripheral(new ResourceLocation( "minecraft", "super_diamond_sword" ), TurtleUpgradeType.BOTH, "super_new_sword", () -> DEAGLE_357.get()));    }
}
