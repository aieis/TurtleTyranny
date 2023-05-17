package com.aieis.cctind.network;

import com.aieis.cctind.peripherals.MessageShootGeneric;
import com.tac.guns.network.message.IMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

import static com.aieis.cctind.CCTInd.mod_id;
import static com.tac.guns.network.PacketHandler.PROTOCOL_VERSION;

public class PacketHandlerExtra {
    private static int nextMessageId = 0;
    private static SimpleChannel playChannel;
    public static void init() {
        playChannel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(mod_id, "play"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        registerPlayMessage(MessageShootGeneric.class, MessageShootGeneric::new, LogicalSide.SERVER);
    }

    private static <T extends IMessage> void registerPlayMessage(Class<T> clazz, Supplier<T> messageSupplier, LogicalSide side)
    {
        playChannel.registerMessage(nextMessageId++, clazz, IMessage::encode, buffer -> {
            T t = messageSupplier.get();
            t.decode(buffer);
            return t;
        }, (t, supplier) -> {
            if(supplier.get().getDirection().getReceptionSide() != side)
                throw new RuntimeException("Attempted to handle message " + clazz.getSimpleName() + " on the wrong logical side!");
            t.handle(supplier);
        });
    }
    public static SimpleChannel getPlayChannel() {
        return playChannel;
    }
}
