package tk.dczippl.lightestlamp.util;

import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerWrapper implements IFluidHandler {

    public IFluidHandlerWrapper wrapper;

    public Direction side;

    public FluidHandlerWrapper(IFluidHandlerWrapper w, Direction s) {
        wrapper = w;
        side = s;
    }

    @Override
    public int getTanks() {
        //TODO: Is this correct
        return wrapper.getAllTanks().length;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        //TODO: Is tank zero indexed or 1 indexed
        //TODO: Decide if we should clone the stack before returning
        return wrapper.getAllTanks()[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return wrapper.getAllTanks()[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return wrapper.getAllTanks()[tank].isFluidValid(stack);
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction fluidAction) {
        if (side == null || resource.isEmpty()) {
            return 0;
        }
        if (wrapper.canFill(side, resource)) {
            return wrapper.fill(side, resource, fluidAction);
        }
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(@Nonnull FluidStack resource, FluidAction fluidAction) {
        if (side == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (wrapper.canDrain(side, resource)) {
            return wrapper.drain(side, resource, fluidAction);
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction fluidAction) {
        if (side == null) {
            return FluidStack.EMPTY;
        }
        if (wrapper.canDrain(side, FluidStack.EMPTY)) {
            return wrapper.drain(side, maxDrain, fluidAction);
        }
        return FluidStack.EMPTY;
    }
}