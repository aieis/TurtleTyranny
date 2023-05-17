package com.aieis.cctind.peripherals;

import com.aieis.cctind.registry.TACResourceLocations;
import com.tac.guns.network.message.MessageShoot;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static com.aieis.cctind.peripherals.ArmedTurtle.create_server_player;
import static com.aieis.cctind.peripherals.ArmedTurtle.prepare_player_inventory;

public class MessageShootGeneric extends MessageShoot {


    private int gun_id;
    private int ammo_count;

    private int x;
    private int y;
    private int z;
    public MessageShootGeneric() {
        super();
    }

    public MessageShootGeneric(int gun_id, int ammo_count, Vector3i pos, float yaw, float pitch, float randP, float randY) {
        super(yaw, pitch, randP, randY);
        this.gun_id = gun_id;
        this.ammo_count = ammo_count;
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();

    }

    @Override
    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeInt(ammo_count);
        buffer.writeInt(gun_id);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);

    }

    @Override
    public void decode(PacketBuffer buffer) {
        super.decode(buffer);
        ammo_count = buffer.readInt();
        gun_id = buffer.readInt();
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ((NetworkEvent.Context)supplier.get()).enqueueWork(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity) create_server_player(x, y, z);//((NetworkEvent.Context)supplier.get()).getSender();
            prepare_player_inventory(player, TACResourceLocations.get(this.gun_id), this.ammo_count);
            ArmedTurtle.handleShoot(this, player, this.getRandP(), this.getRandY());
        });
        ((NetworkEvent.Context)supplier.get()).setPacketHandled(true);
    }
}
