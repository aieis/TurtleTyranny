package com.aieis.cctind.common;

import com.tac.guns.Config;
import com.tac.guns.common.Gun;
import com.tac.guns.item.GunItem;
import com.tac.guns.item.TransitionalTypes.TimelessGunItem;
import com.tac.guns.mixin.client.MinecraftStaticMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: Forked from MrCrayfish, continued by Timeless devs
 */
public class ShootingHandlerGen {

    // TODO: Cleanup and document ShootingHandler fire code, what is important or able to be simplified.
    private PlayerEntity player;

    private Runnable updatePosition;

    public boolean isShooting() {
        return shooting;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public void setShootingError(boolean shootErr) {
        this.shootErr = shootErr;
    }

    private boolean shooting;
    private boolean reloading;
    private int reloadTicksRemaining = 0;
    private boolean shootErr;
    private boolean clickUp = false;
    public int burstTracker = 0;
    private int burstCooldown = 0;

    public ShootingHandlerGen(PlayerEntity player, Runnable updatePosition) {
        this.player = player;
        this.updatePosition = updatePosition;
        reset();
    }

    public void updatePlayerPosition()
    {
        updatePosition.run();
    }

    public void reset() {
        this.burstTracker = 0;
        this.burstCooldown = 0;
        this.clickUp = false;
        this.shootErr = false;
    }

    public void beginFire() {
        ItemStack heldItem = player.getItemInHand(Hand.MAIN_HAND);
        if (heldItem.getItem() instanceof GunItem) {
            if (heldItem.getItem() instanceof TimelessGunItem && heldItem.getTag().getInt("CurrentFireMode") == 3 && this.burstCooldown == 0) {
                this.burstTracker = ((TimelessGunItem) heldItem.getItem()).getGun().getGeneral().getBurstCount();
                fire(player, heldItem);
                this.burstCooldown = ((TimelessGunItem) heldItem.getItem()).getGun().getGeneral().getBurstRate();
            } else if (this.burstCooldown == 0)
                fire(player, heldItem);

            if (!(heldItem.getTag().getInt("AmmoCount") > 0)) {
                ServerPlayHandlerGen.EmptyMag(player);
                startReload(heldItem);
            }
        }
    }

    private void startReload(ItemStack heldItem)
    {
        if (reloading) {
            return;
        }

        reloading = true;
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        reloadTicksRemaining = gun.getReloads().getReloadMagTimer();

    }

    private float shootTickGapLeft = 0F;

    public float getShootTickGapLeft() {
        return shootTickGapLeft;
    }

    public float shootMsGap = 0F;

    public float getshootMsGap() {
        return shootMsGap;
    }

    public float calcShootTickGap(int rpm) {
        float shootTickGap = 60F / rpm * 20F;
        return shootTickGap;
    }


    private static float hitmarkerCooldownMultiplier() {
        int fps = ((MinecraftStaticMixin) Minecraft.getInstance()).getCurrentFPS();
        if (fps < 11)
            return 16f;
        else if (fps < 21)
            return 14.5f;
        else if (fps < 31)
            return 4f;
        else if (fps < 61)
            return 2f;
        else if (fps < 121)
            return 1f;
        else if (fps < 181)
            return 0.7f;
        else if (fps < 201)
            return 0.5f;
        else
            return 0.375f;
    }

    private static float visualCooldownMultiplier() {
        int fps = ((MinecraftStaticMixin) Minecraft.getInstance()).getCurrentFPS();
        if (fps < 11)
            return 8f;
        else if (fps < 21)
            return 6.25f;
        else if (fps < 31)
            return 1.25f;
        else if (fps < 61)
            return 0.95f;
        else if (fps < 121)
            return 0.625f;
        else if (fps < 181)
            return 0.425f;
        else if (fps < 201)
            return 0.35f;
        else
            return 0.25f;
    }

    public void renderTickLow(float renderTickTime) {
        if (shootMsGap > 0F) {
            shootMsGap -= renderTickTime * visualCooldownMultiplier();
        } else if (shootMsGap < -0.05F)
            shootMsGap = 0F;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderTick() {
        if (player == null || !player.isAlive() || player.getMainHandItem().getItem() instanceof GunItem)
            return;
//        GunAnimationController controller = GunAnimationController.fromItem(Minecraft.getInstance().player.getMainHandItem().getItem());
//        if(controller == null)
//            return;
        else if ( /* controller.isAnimationRunning() && */(shootMsGap < 0F && this.burstTracker != 0)) {
//            if(controller.isAnimationRunning(GunAnimationController.AnimationLabel.PUMP) || controller.isAnimationRunning(GunAnimationController.AnimationLabel.PULL_BOLT))
//                return;
            if (Config.CLIENT.controls.burstPress.get())
                this.burstTracker = 0;
            this.clickUp = true;
        }
    }

    public void onHandleShooting() {
        shootTickGapLeft -= shootTickGapLeft > 0F ? 1F : 0F;
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem && !Gun.hasAmmo(heldItem) && !reloading) {
            startReload(heldItem);
        }
    }

    public void onClientTick() {
        burstCooldown = Math.max(burstCooldown - 1, 0);
    }

    public void reload(ItemStack stack) {
        setShooting(ReloadTrackerGen.handleReload(player, stack));
    }

    public void onPostClientTick() {
        ItemStack heldItem = player.getMainHandItem();

        if (!(heldItem.getItem() instanceof TimelessGunItem)) {
            return;
        }

        if (heldItem.getTag() == null) {
            heldItem.getOrCreateTag();
            return;
        }

        if (reloading && reloadTicksRemaining > 1) {
            reloadTicksRemaining -= 1;
            return;
        }

        if (reloading) {
            reloading = false;
            reloadTicksRemaining = 0;
            reload(heldItem);
        }

        TimelessGunItem gunItem = (TimelessGunItem) heldItem.getItem();
        if (heldItem.getTag().getInt("CurrentFireMode") == 3 && Config.CLIENT.controls.burstPress.get()) {
            if (this.burstTracker > 0)
                fire(player, heldItem);
            return;
        }

        if (shooting) {
            Gun gun = ((TimelessGunItem) heldItem.getItem()).getModifiedGun(heldItem);
            if (gun.getGeneral().isAuto() && heldItem.getTag().getInt("CurrentFireMode") == 2) {
                fire(player, heldItem);
                return;
            }
            if (heldItem.getTag().getInt("CurrentFireMode") == 3 && !Config.CLIENT.controls.burstPress.get() && !this.clickUp && this.burstCooldown == 0) {
                if (this.burstTracker < gun.getGeneral().getBurstCount()) {
                    if (getshootMsGap() <= 0) {
                        fire(player, heldItem);
                        if (!this.shootErr)
                            this.burstTracker++;
                    }
                } else if (heldItem.getTag().getInt("AmmoCount") > 0 && this.burstTracker > 0) {
                    this.burstTracker = 0;
                    this.clickUp = true;
                    this.burstCooldown = gun.getGeneral().getBurstRate();
                }
                return;
            }
        } else if (this.clickUp) {
            if (heldItem.getTag().getInt("CurrentFireMode") == 3 && this.burstTracker > 0) {
                this.burstCooldown = gunItem.getGun().getGeneral().getBurstRate();
            }
            this.burstTracker = 0;
            this.clickUp = false;
        }
    }

    public void fire(PlayerEntity player, ItemStack heldItem) {
        if (!(heldItem.getItem() instanceof GunItem) || !(Gun.hasAmmo(heldItem) || player.isCreative()) || player.isSpectator() || shootTickGapLeft > 0.0F) {
            return;
        }
        updatePlayerPosition();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);

        float rpm = (float) modifiedGun.getGeneral().getRate();
        shootTickGapLeft += calcShootTickGap((int) rpm);
        shootMsGap = calcShootTickGap((int) rpm);
        ServerPlayHandlerGen.handleShoot(player, player.getViewYRot(1.0F), player.getViewXRot(1.0F), 0, 0);

        burstTracker += shooting? -1 : 1;
    }
}
