package com.aieis.cctind.peripherals;

import com.sun.istack.internal.NotNull;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.logging.Level;

public class FirearmPeripheral  implements IPeripheral, IPeripheralProvider {


    @Override
    public String getType() {
        return "firearm";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
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

    @NotNull
    @Override
    public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
        if(world.getBlockState(pos).getBlock().equals(Blocks.ANVIL)||
                world.getBlockState(pos).getBlock().equals(Blocks.CHIPPED_ANVIL)||
                world.getBlockState(pos).getBlock().equals(Blocks.DAMAGED_ANVIL)){
            return LazyOptional.of(() -> this);
        }
        return LazyOptional.empty();
    }
}
