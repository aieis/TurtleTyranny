package com.aieis.cctind.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShootingHandlerManager {

    private static ShootingHandlerManager instance;

    private static Map<PlayerEntity, ShootingHandlerGen> ShootingPlayers;
    public static ShootingHandlerManager get() {
        if (instance == null) {
            instance = new ShootingHandlerManager();
        }
        return instance;
    }

    private ShootingHandlerManager () {
        ShootingPlayers = new HashMap<>();
    }

    private boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            if (mc.overlay != null) {
                return false;
            } else if (mc.screen != null) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    public static void setShooting(PlayerEntity player, Runnable updater, boolean shooting) {
        if (ShootingPlayers.containsKey(player) && !shooting) {
            ShootingPlayers.get(player).setShooting(false);
            ShootingPlayers.remove(player);
        }

        if (!ShootingPlayers.containsKey(player) && shooting) {
            ShootingHandlerGen shootingHandler = new ShootingHandlerGen(player, updater);
            shootingHandler.setShooting(true);
            shootingHandler.beginFire();
            ShootingPlayers.put(player, shootingHandler);
        } else if (ShootingPlayers.containsKey(player)){
            ShootingPlayers.get(player).setShooting(shooting);
        }
    }

    @SubscribeEvent(
            priority = EventPriority.LOW
    )
    public void renderTickLow(TickEvent.RenderTickEvent evt) {
        if (evt.type.equals(TickEvent.Type.RENDER) && !evt.phase.equals(TickEvent.Phase.START)) {
            ShootingPlayers.forEach(((player, shootingHandlerGen) -> {
                shootingHandlerGen.renderTickLow(evt.renderTickTime);
            }));
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void renderTick(TickEvent.RenderTickEvent evt) {
            ShootingPlayers.forEach(((player, shootingHandlerGen) -> {
                shootingHandlerGen.renderTick();
            }));
    }

    @SubscribeEvent
    public void onHandleShooting(TickEvent.ClientTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            ArrayList<PlayerEntity> keys = new ArrayList<>();
            ShootingPlayers.forEach(((player, shootingHandlerGen) -> {
                shootingHandlerGen.onHandleShooting();
                if (!shootingHandlerGen.isShooting())
                    keys.add(player);
            }));
            keys.forEach(player -> {ShootingPlayers.remove(player);});
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        ShootingPlayers.forEach(((player, shootingHandlerGen) -> {
            shootingHandlerGen.onClientTick();
        }));
    }

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ShootingPlayers.forEach(((player, shootingHandlerGen) -> {
                shootingHandlerGen.onPostClientTick();
            }));
        }
    }

}
