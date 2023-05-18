package com.aieis.cctind.registry;

import com.aieis.cctind.common.ShootingHandlerManager;
import com.aieis.cctind.network.PacketHandlerExtra;
import com.aieis.cctind.peripherals.TurtleFirearm;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
public class Register {

    public static void registerTurtleUpgrades(){

        ComputerCraftAPI.registerTurtleUpgrade(new TurtleFirearm(new ResourceLocation( "minecraft", "test_equip" ),
                TurtleUpgradeType.PERIPHERAL, "test_equip", () -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("tac", "glock_17"))));

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
