package com.aieis.cctind.peripherals;

import com.aieis.cctind.common.ShootingHandlerManager;
import com.tac.guns.common.Gun;
import com.tac.guns.item.GunItem;
import com.tac.guns.util.GunEnchantmentHelper;
import dan200.computercraft.api.client.TransformedModel;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.aieis.cctind.peripherals.ArmedTurtle.make_clock;


public class TurtleFirearm extends AbstractTurtleUpgrade {

    protected final IItemProvider item_prov;
    protected ItemStack item = null;

    public TurtleFirearm(ResourceLocation id, TurtleUpgradeType type, String adjective, IItemProvider item) {
        super(id, type, adjective, item);
        this.item_prov = item;
    }

    @Override
    public boolean isItemSuitable(@Nonnull ItemStack stack)
    {
        return true;
    }

    @Nonnull
    @Override
    public TurtleCommandResult useTool(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb, @Nonnull Direction dir )
    {
        return fire(turtle, dir, side);
    }

    @LuaFunction
    public final MethodResult custom_test() {
        return MethodResult.of(true);
    }
    @LuaFunction
    public final MethodResult pullTrigger(@Nonnull ITurtleAccess turtle)
    {
        Direction dir = turtle.getDirection();
        if (item == null) {
            item = new ItemStack(item_prov.asItem());
            Gun ngun = make_clock();
            CompoundNBT tag = ngun.serializeNBT();
            Gun gun = ((GunItem) item.getItem()).getGun();
            gun.deserializeNBT(tag);
        }

        // Create a fake player, and orient it appropriately
        World world = turtle.getWorld();
        BlockPos position = turtle.getPosition();
        TileEntity turtleTile = turtle instanceof TurtleBrain ? ((TurtleBrain) turtle).getOwner() : world.getBlockEntity( position );
        if( turtleTile == null ) return MethodResult.of(false);

        final TurtlePlayer turtlePlayer = TurtlePlayer.getWithPosition( turtle, position, dir );
        turtlePlayer.xOld = position.getX();
        turtlePlayer.yOld = position.getY();
        turtlePlayer.zOld = position.getZ();
        ItemStack stack = item;//item.copy();
        turtlePlayer.setItemInHand(Hand.MAIN_HAND, stack);

        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        Item addAmmo = ForgeRegistries.ITEMS.getValue(gun.getProjectile().getItem());
        turtlePlayer.loadInventory( stack );
        turtlePlayer.inventory.setItem(1, new ItemStack(addAmmo, 64));
        if (!Gun.hasAmmo(stack)) {
            ItemStack ammo = Gun.findAmmo(turtlePlayer, gun.getProjectile().getItem())[0];
            if(!ammo.isEmpty())
            {
                CompoundNBT tag = stack.getTag();
                int amount = Math.min(ammo.getCount(), gun.getReloads().getReloadAmount());
                if (tag != null) {
                    int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(stack, gun);
                    amount = Math.min(amount, maxAmmo - tag.getInt("AmmoCount"));
                    tag.putInt("AmmoCount", tag.getInt("AmmoCount") + amount);
                }
            }

            PlayerEntity pe = Minecraft.getInstance().player;
            pe.displayClientMessage((new TranslationTextComponent("Reloading")).withStyle(TextFormatting.GREEN), true);
        } else {
            PlayerEntity pe = Minecraft.getInstance().player;
            pe.displayClientMessage((new TranslationTextComponent("Already loaded")).withStyle(TextFormatting.GREEN), true);
        }

        stack.getTag().putInt("CurrentFireMode", 1);
        ShootingHandlerManager.setShooting(turtlePlayer, true);
        return MethodResult.of(true);
    }

    @LuaFunction
    public final MethodResult releaseTrigger(@Nonnull ITurtleAccess turtle)
    {
        Direction dir = turtle.getDirection();
        World world = turtle.getWorld();
        BlockPos position = turtle.getPosition();
        TileEntity turtleTile = turtle instanceof TurtleBrain ? ((TurtleBrain) turtle).getOwner() : world.getBlockEntity( position );
        if( turtleTile == null ) return MethodResult.of(false);
        final TurtlePlayer turtlePlayer = TurtlePlayer.getWithPosition( turtle, position, dir );
        ShootingHandlerManager.setShooting(turtlePlayer, false);
        return MethodResult.of(true);
    }

    private TurtleCommandResult fire( ITurtleAccess turtle, Direction direction, TurtleSide side )
    {
        if (item == null) {
            item = new ItemStack(item_prov.asItem());
            Gun ngun = make_clock();
            CompoundNBT tag = ngun.serializeNBT();
            Gun gun = ((GunItem) item.getItem()).getGun();
            gun.deserializeNBT(tag);
        }

        // Create a fake player, and orient it appropriately
        World world = turtle.getWorld();
        BlockPos position = turtle.getPosition();
        TileEntity turtleTile = turtle instanceof TurtleBrain ? ((TurtleBrain) turtle).getOwner() : world.getBlockEntity( position );
        if( turtleTile == null ) return TurtleCommandResult.failure( "Turtle has vanished from existence." );

        final TurtlePlayer turtlePlayer = TurtlePlayer.getWithPosition( turtle, position, direction );
        turtlePlayer.xOld = position.getX();
        turtlePlayer.yOld = position.getY();
        turtlePlayer.zOld = position.getZ();
        ItemStack stack = item;//item.copy();
        turtlePlayer.setItemInHand(Hand.MAIN_HAND, stack);

        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        Item addAmmo = ForgeRegistries.ITEMS.getValue(gun.getProjectile().getItem());
        turtlePlayer.loadInventory( stack );
        turtlePlayer.inventory.setItem(1, new ItemStack(addAmmo, 64));

        if (!Gun.hasAmmo(stack)) {
            ItemStack ammo = Gun.findAmmo(turtlePlayer, gun.getProjectile().getItem())[0];
            if(!ammo.isEmpty())
            {
                CompoundNBT tag = stack.getTag();
                int amount = Math.min(ammo.getCount(), gun.getReloads().getReloadAmount());
                if (tag != null) {
                    int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(stack, gun);
                    amount = Math.min(amount, maxAmmo - tag.getInt("AmmoCount"));
                    tag.putInt("AmmoCount", tag.getInt("AmmoCount") + amount);
                }
            }

            PlayerEntity pe = Minecraft.getInstance().player;
            pe.displayClientMessage((new TranslationTextComponent("Reloading")).withStyle(TextFormatting.GREEN), true);
        } else {
            PlayerEntity pe = Minecraft.getInstance().player;
            pe.displayClientMessage((new TranslationTextComponent("Already loaded")).withStyle(TextFormatting.GREEN), true);
        }

        stack.getTag().putInt("CurrentFireMode", 1);

        ArmedTurtle.fire(turtlePlayer, stack, position);
        turtlePlayer.inventory.clearContent();
        return TurtleCommandResult.success();
    }

    @Nonnull
    @Override
    public TransformedModel getModel(@Nullable ITurtleAccess iTurtleAccess, @Nonnull TurtleSide side) {
        return TransformedModel.of( getCraftingItem(), side == TurtleSide.LEFT ? Transforms.leftTransform : Transforms.rightTransform );
    }

    private static class Transforms
    {
        static final TransformationMatrix leftTransform = getMatrixFor( -0.40625f );
        static final TransformationMatrix rightTransform = getMatrixFor( 0.40625f );

        private static TransformationMatrix getMatrixFor( float offset )
        {
            return new TransformationMatrix( new Matrix4f( new float[] {
                    0.0f, 0.0f, -1.0f, 1.0f + offset,
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, -1.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 0.0f, 1.0f,
            } ) );
        }
    }

}
