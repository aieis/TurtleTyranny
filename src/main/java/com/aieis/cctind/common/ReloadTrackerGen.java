package com.aieis.cctind.common;

import com.tac.guns.common.Gun;
import com.tac.guns.inventory.gear.armor.ArmorRigCapabilityProvider;
import com.tac.guns.inventory.gear.armor.RigSlotsHandler;
import com.tac.guns.item.GunItem;
import com.tac.guns.network.PacketHandler;
import com.tac.guns.network.message.MessageGunSound;
import com.tac.guns.util.GunEnchantmentHelper;
import com.tac.guns.util.WearableHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;

import static com.aieis.cctind.peripherals.ArmedTurtle.live_log;
import static com.tac.guns.common.Gun.isAmmo;
import static com.tac.guns.common.ReloadTracker.calcMaxReserveAmmo;

public class ReloadTrackerGen {

    public static ItemStack[] findAmmoInventory(IInventory inventory, ResourceLocation id) {
        ArrayList<ItemStack> stacks = new ArrayList();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (isAmmo(stack, id)) {
                stacks.add(stack);
            }
        }

        return stacks.toArray(new ItemStack[0]);
    }

    public static boolean handleReload(PlayerEntity player, ItemStack stack)
    {
        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        if (gun.getReloads().isMagFed()) {
            return increaseMagAmmo(player, stack);
        } else {
            return increaseAmmo(player, stack);
        }
    }
    public static boolean increaseMagAmmo(PlayerEntity player, ItemStack stack)
    {
        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        ItemStack[] ammoStacks = Gun.findAmmo(player, gun.getProjectile().getItem());
        if (ammoStacks.length > 0) {
            CompoundNBT tag = stack.getTag();
            int ammoAmount = Math.min(calcMaxReserveAmmo(ammoStacks), GunEnchantmentHelper.getAmmoCapacity(stack, gun));
            if (tag != null) {
                int currentAmmo = tag.getInt("AmmoCount");
                int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(stack, gun);
                int amount = maxAmmo - currentAmmo;
                if (currentAmmo == 0 && !gun.getReloads().isOpenBolt()) {
                    if (ammoAmount < amount) {
                        tag.putInt("AmmoCount", currentAmmo + ammoAmount);
                        shrinkFromAmmoPool(ammoStacks, player, ammoAmount, gun);
                    } else {
                        tag.putInt("AmmoCount", maxAmmo - 1);
                        shrinkFromAmmoPool(ammoStacks, player, amount - 1, gun);
                    }
                } else if (ammoAmount < amount) {
                    tag.putInt("AmmoCount", currentAmmo + ammoAmount);
                    shrinkFromAmmoPool(ammoStacks, player, ammoAmount, gun);
                } else {
                    tag.putInt("AmmoCount", maxAmmo);
                    shrinkFromAmmoPool(ammoStacks, player, amount, gun);
                }
            }
        }

        ResourceLocation reloadSound = gun.getSounds().getReload();
        if (reloadSound != null) {
            MessageGunSound message = new MessageGunSound(reloadSound, SoundCategory.PLAYERS, (float)player.getX(), (float)player.getY() + 1.0F, (float)player.getZ(), 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() -> {
                return new PacketDistributor.TargetPoint(player.getX(), player.getY() + 1.0, player.getZ(), 16.0, player.level.dimension());
            }), message);
        }

        return ammoStacks.length > 0;

    }

    public static boolean increaseAmmo(PlayerEntity player, ItemStack stack) {
        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        ItemStack[] ammoStacks = Gun.findAmmo(player, gun.getProjectile().getItem());
        live_log("Ammo Stacks: " + ammoStacks.length);
        if (ammoStacks.length == 0) {
            return false;
        }
        ItemStack ammo = ammoStacks[0];
        if (!ammo.isEmpty()) {
            CompoundNBT tag = stack.getTag();
            int amount = Math.min(ammo.getCount(), gun.getReloads().getReloadAmount());
            if (tag != null) {
                int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(stack, gun);
                amount = Math.min(amount, maxAmmo - tag.getInt("AmmoCount"));
                tag.putInt("AmmoCount", tag.getInt("AmmoCount") + amount);
            }

            shrinkFromAmmoPool(new ItemStack[]{ammo}, player, amount, gun);
        } else {
            return false;
        }

        ResourceLocation reloadSound = gun.getSounds().getReload();
        if (reloadSound != null) {
            MessageGunSound message = new MessageGunSound(reloadSound, SoundCategory.PLAYERS, (float)player.getX(), (float)player.getY() + 1.0F, (float)player.getZ(), 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() -> {
                return new PacketDistributor.TargetPoint(player.getX(), player.getY() + 1.0, player.getZ(), 16.0, player.level.dimension());
            }), message);
        }

        return true;
    }


    public static void shrinkFromAmmoPool(ItemStack[] ammoStacks, PlayerEntity player, int shrinkAmount, Gun gun)
    {
        int shrinkAmt = shrinkAmount;
        ArrayList<ItemStack> stacks = new ArrayList<>();

        ItemStack rig = WearableHelper.PlayerWornRig(player);
        if(rig != null) {
            RigSlotsHandler itemHandler = (RigSlotsHandler) rig.getCapability(ArmorRigCapabilityProvider.capability).resolve().get();
            for (ItemStack x : itemHandler.getStacks()) {
                if(isAmmo(x, gun.getProjectile().getItem()))
                    stacks.add(x);
            }
            for (ItemStack x: stacks)
            {
                if(!x.isEmpty())
                {
                    int max = shrinkAmt > x.getCount() ? x.getCount() : shrinkAmt;
                    x.shrink(max);
                    shrinkAmt-=max;
                }
                if(shrinkAmt==0)
                    return;
            }
        }

        for (ItemStack x: ammoStacks)
        {
            if(!x.isEmpty())
            {
                int max = shrinkAmt > x.getCount() ? x.getCount() : shrinkAmt;
                x.shrink(max);
                shrinkAmt-=max;
            }
            if(shrinkAmt==0)
                return;
        }
    }
}
