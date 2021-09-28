package tk.dczippl.lightestlamp.machine.gascentrifuge;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tk.dczippl.lightestlamp.Config;
import tk.dczippl.lightestlamp.init.ModContainers;

public class GasCentrifugeScreenHandler extends ScreenHandler
{
    private final Inventory furnaceInventory;
    private final BlockPos pos;
    public final PropertyDelegate delegate;
    protected final World world;

    protected GasCentrifugeScreenHandler(ScreenHandlerType<?> containerTypeIn, int id, PlayerInventory playerInventoryIn, PacketBuffer buf) {
        this(containerTypeIn, id, playerInventoryIn, new SimpleInventory(6), new ArrayPropertyDelegate(7),buf);
    }

    protected GasCentrifugeScreenHandler(ScreenHandlerType<?> containerTypeIn, int id, PlayerInventory playerInventoryIn, Inventory furnaceInventoryIn, PropertyDelegate delegate, PacketBuffer buf) {
        super(containerTypeIn, id);
        checkSize(furnaceInventoryIn, 6);
        checkDataCount(delegate, 7);
        this.furnaceInventory = furnaceInventoryIn;
        this.delegate = delegate;
        this.world = playerInventoryIn.player.world;
        this.pos = buf.readBlockPos();
        this.addSlot(new Slot(furnaceInventoryIn, 0, 16, 35));
        this.addSlot(new Slot(furnaceInventoryIn, 1, 41, 35));
        this.addSlot(new FurnaceOutputSlot(playerInventoryIn.player, furnaceInventoryIn, 2, 99, 19));
        this.addSlot(new FurnaceOutputSlot(playerInventoryIn.player, furnaceInventoryIn, 3, 127, 19));
        this.addSlot(new FurnaceOutputSlot(playerInventoryIn.player, furnaceInventoryIn, 4, 99, 51));
        this.addSlot(new FurnaceOutputSlot(playerInventoryIn.player, furnaceInventoryIn, 5, 127, 51));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventoryIn, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventoryIn, k, 8 + k * 18, 142));
        }

        this.addProperties(delegate);
    }

    public GasCentrifugeScreenHandler(int i, PlayerInventory playerInventory, PacketBuffer packetBuffer)
    {
        this(ModContainers.GAS_CENTRIFUGE, i, playerInventory, new Inventory(6), new IntArray(7),packetBuffer);
    }

    public BlockPos getBlockPos()
    {
        return pos;
    }

    public void clear() {
        this.furnaceInventory.clear();
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.furnaceInventory.isUsableByPlayer(playerIn);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 2) {
                if (!this.insertItem(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 1 && index != 0) {
                if (index >= 3 && index < 30) {
                    if (!this.insertItem(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.insertItem(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Environment(EnvType.CLIENT)
    public int getCookProgressionScaled() {
        int i = this.delegate.get(2);
        int j = this.delegate.get(3);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    @Environment(EnvType.CLIENT)
    public int getBurnLeftScaled()
    {
        int multiplier = Config.GLOWSTONE_FUEL_MULTIPLIER.get() >= 2 ? Config.GLOWSTONE_FUEL_MULTIPLIER.get() : 2;
        return this.delegate.get(0) * 13 / 180 / multiplier;
    }

    @Environment(EnvType.CLIENT)
    public int getLiquidScaled()
    {
        return this.delegate.get(5) / 50;
    }

    @Environment(EnvType.CLIENT)
    public boolean func_217061_l() {
        return this.delegate.get(0) > 0;
    }
}