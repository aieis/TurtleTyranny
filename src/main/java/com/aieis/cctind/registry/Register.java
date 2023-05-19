package com.aieis.cctind.registry;

import com.aieis.cctind.common.ShootingHandlerManager;
import com.aieis.cctind.network.PacketHandlerExtra;
import com.aieis.cctind.peripherals.TurtleFirearm;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import static com.tac.guns.init.ModItems.*;

public class Register {

    public static void registerTurtleUpgrades(){

        ComputerCraftAPI.registerTurtleUpgrade(new TurtleFirearm(new ResourceLocation( "cctind", "turtle_glock_17" ),
                TurtleUpgradeType.PERIPHERAL, "pacifier_glock17", GLOCK_17::get));

        ComputerCraftAPI.registerTurtleUpgrade(new TurtleFirearm(new ResourceLocation( "cctind", "turtle_m16a4" ),
                TurtleUpgradeType.PERIPHERAL, "pacifier_m16a4", M16A4::get));

        ComputerCraftAPI.registerTurtleUpgrade(new TurtleFirearm(new ResourceLocation( "cctind", "turtle_m4" ),
                TurtleUpgradeType.PERIPHERAL, "pacifier_m4", M4::get));

    }

    public static void registerTurtlePeripherals(){

    }
    public static void registerTACExtras() {
        PacketHandlerExtra.init();
    }

    public static void setup( Minecraft mc )
    {
        MinecraftForge.EVENT_BUS.register(ShootingHandlerManager.get());
    }



}
