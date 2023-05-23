package com.aieis.cctind.peripherals;

import com.aieis.cctind.common.ShootingHandlerManager;
import com.tac.guns.common.Gun;
import com.tac.guns.item.GunItem;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

import static com.aieis.cctind.common.ReloadTrackerGen.findAmmoInventory;

public class FirearmPeripheral implements IPeripheral {

    private final ITurtleAccess turtle;

    protected final IItemProvider item_prov;
    protected ItemStack item = null;


    private float xRot;
    private float yRot;

    public FirearmPeripheral( ITurtleAccess turtle, IItemProvider item_prov )
    {
        this.turtle = turtle;
        this.item_prov = item_prov;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "firearm";
    }

    private int find_item() {
        Item target_gun = item_prov.asItem();
        String str = ForgeRegistries.ITEMS.getKey(target_gun).toString();
        for (int i = 0; i < turtle.getInventory().getContainerSize(); i++){
            ItemStack item = turtle.getInventory().getItem(i);
            if ( str.equals(ForgeRegistries.ITEMS.getKey(item.getItem()).toString())) {
                this.item = item;
                return i;
            }
        }
        return -1;
    }

    @LuaFunction
    public final MethodResult pullTrigger() throws LuaException
    {

        if (item == null && find_item() == -1) {
            return MethodResult.of(false);
        }

        // Create a fake player, and orient it appropriately
        World world = turtle.getWorld();
        BlockPos position = turtle.getPosition();
        Direction direction = turtle.getDirection();
        TileEntity turtleTile = turtle instanceof TurtleBrain ? ((TurtleBrain) turtle).getOwner() : world.getBlockEntity( position );
        if( turtleTile == null ) return MethodResult.of(false);

        final TurtlePlayer turtlePlayer = TurtlePlayer.getWithPosition( turtle, position, direction );
        turtlePlayer.xOld = position.getX();
        turtlePlayer.yOld = position.getY();
        turtlePlayer.zOld = position.getZ();
        ItemStack stack = item;
        turtlePlayer.setItemInHand(Hand.MAIN_HAND, stack);

        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        ItemStack[] ammoStacks = findAmmoInventory(turtle.getInventory(), gun.getProjectile().getItem());
        turtlePlayer.loadInventory( stack );

        for (int i = 0; i < Math.min(ammoStacks.length, turtlePlayer.inventory.getContainerSize() - 1); i++) {
            turtlePlayer.inventory.setItem(i + 1, ammoStacks[i]);
        }

        ShootingHandlerManager.setShooting(turtlePlayer, () -> {
            BlockPos pos = turtle.getPosition();
            Direction dir = turtle.getDirection();
            turtlePlayer.setPosition(turtle, pos, dir);
            turtlePlayer.xRot = xRot;
            turtlePlayer.yRot = yRot;
            }, true);

        return MethodResult.of(true);
    }

    @LuaFunction
    public final MethodResult releaseTrigger() throws LuaException
    {
        Direction dir = turtle.getDirection();
        World world = turtle.getWorld();
        BlockPos position = turtle.getPosition();
        TileEntity turtleTile = turtle instanceof TurtleBrain ? ((TurtleBrain) turtle).getOwner() : world.getBlockEntity( position );
        if( turtleTile == null ) return MethodResult.of(false);
        final TurtlePlayer turtlePlayer = TurtlePlayer.getWithPosition( turtle, position, dir );
        ShootingHandlerManager.setShooting(turtlePlayer, null, false);
        return MethodResult.of(true);
    }

    @LuaFunction MethodResult setRotation(float x, float y) throws LuaException
    {
        xRot = x;
        yRot = y;
        return MethodResult.of(true);
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        return other instanceof FirearmPeripheral;
    }

    @Nonnull
    @Override
    public Object getTarget()
    {
        return turtle;
    }
}
