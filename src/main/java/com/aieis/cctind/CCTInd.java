package com.aieis.cctind;

import com.aieis.cctind.registry.ComputerCraftRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("cctind")
public class CCTInd
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String mod_id = "cctind";

    public CCTInd() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ComputerCraftRegister.registerTurtleUpgrades();
    }
}
