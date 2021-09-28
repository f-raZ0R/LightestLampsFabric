package tk.dczippl.lightestlamp.machine.gascentrifuge;

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tk.dczippl.lightestlamp.init.ModBlockEntities;
import tk.dczippl.lightestlamp.init.ModFluids;
import tk.dczippl.lightestlamp.init.ModMiscs;
import tk.dczippl.lightestlamp.items.FilterItem;
import tk.dczippl.lightestlamp.plugins.Config;

import java.util.Map;
import java.util.Random;

public class GasCentrifugeBlockEntity extends LockableContainerBlockEntity implements SidedInventory, BlockEntityTicker<GasCentrifugeBlockEntity>
{
    public GasCentrifugeBlockEntity(BlockEntityType<?> type)
    {
        super(type);
    }
    public GasCentrifugeBlockEntity()
    {
        super(ModBlockEntities.CENTRIFUGE_TE);
    }

    @Override
    protected Text getContainerName()
    {
        return new TranslatableText("container.gascentrifuge");
    }

    protected ScreenHandler createScreenHandler(int id, PlayerInventory player)
    {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(8,8));
        buffer.writeBlockPos(pos);
        return new GasCentrifugeScreenHandler(ModContainers.GAS_CENTRIFUGE,id, player, this, this.furnaceData, buffer);
    }

    /*public FluidTank tank = new FluidTank(4000);*/

    private static final int[] SLOTS_UP = new int[]{0,1};
    private static final int[] SLOTS_DOWN = new int[]{2, 3, 4, 5};
    //private static final int[] SLOTS_HORIZONTAL = new int[]{1};
    protected DefaultedList<ItemStack> items = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private int ticksBeforeDumping;
    private int burnTime;
    private int fluid;
    private int cookTime;
    private int cookTimeTotal;
    private int redstoneMode;
    private int liquidMode;
    public final PropertyDelegate furnaceData = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch(index) {
                case 0:
                    return GasCentrifugeBlockEntity.this.burnTime;
                case 1:
                    return GasCentrifugeBlockEntity.this.redstoneMode;
                case 2:
                    return GasCentrifugeBlockEntity.this.cookTime;
                case 3:
                    return GasCentrifugeBlockEntity.this.cookTimeTotal;
                case 4:
                    return GasCentrifugeBlockEntity.this.liquidMode;
                case 5:
                    return GasCentrifugeBlockEntity.this.tank.getFluidAmount();
                case 6:
                    return GasCentrifugeBlockEntity.this.ticksBeforeDumping;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch(index) {
                case 0:
                    GasCentrifugeBlockEntity.this.burnTime = value;
                    break;
                case 1:
                    GasCentrifugeBlockEntity.this.redstoneMode = value;
                    break;
                case 2:
                    GasCentrifugeBlockEntity.this.cookTime = value;
                    break;
                case 3:
                    GasCentrifugeBlockEntity.this.cookTimeTotal = value;
                    break;
                case 4:
                    GasCentrifugeBlockEntity.this.liquidMode = value;
                    break;
                case 5:
                    break;
                case 6:
                    GasCentrifugeBlockEntity.this.ticksBeforeDumping = value;
                    break;
            }

        }

        public int size() {
            return 7;
        }
    };

    public void setRedstoneMode(int redstoneMode)
    {
        furnaceData.set(1,redstoneMode);
    }

    public int getRedstoneMode()
    {
        return furnaceData.get(1);
    }

    public void setLiquidMode(int liquidMode)
    {
        furnaceData.set(4,liquidMode);
    }

    public int getLiquidMode()
    {
        return furnaceData.get(4);
    }

    public void startTicksBeforeDumping()
    {
        furnaceData.set(6,60);
    }

    public static Map<Item, Integer> getBurnTimes() {
        Map<Item, Integer> map = Maps.newLinkedHashMap();

        int multiplier = Config.GLOWSTONE_FUEL_MULTIPLIER.get() >= 2 ? Config.GLOWSTONE_FUEL_MULTIPLIER.get() : 2;
        //Mekanism compatibility
        // TODO: move to "techreborn"
        Tag<Item> refined_glowstones = ItemTags.getTagGroup().getTag(new Identifier("forge:ingots/refined_glowstone"));
        if (refined_glowstones!=null)
            addItemTagBurnTime(map, refined_glowstones,60*multiplier);
        Tag<Item> refined_glowstones_block = ItemTags.getTagGroup().getTag(new Identifier("forge:storage_blocks/refined_glowstone"));
        if (refined_glowstones_block!=null)
            addItemTagBurnTime(map, refined_glowstones_block,520*multiplier);
        Tag<Item> refined_glowstones_nugget = ItemTags.getTagGroup().getTag(new Identifier("forge:nuggets/refined_glowstone"));
        if (refined_glowstones_nugget!=null)
            addItemTagBurnTime(map, refined_glowstones_nugget,5*multiplier);
        Tag<Item> glowstone_blocks = ItemTags.getTagGroup().getTag(new Identifier("forge:storage_blocks/glowstone"));
        if (glowstone_blocks!=null)
            addItemTagBurnTime(map, glowstone_blocks,360*multiplier);

        addItemTagBurnTime(map, Tags.Items.DUSTS_GLOWSTONE,40*multiplier);
        addItemBurnTime(map, Blocks.GLOWSTONE.asItem(), 160*multiplier);
        addItemBurnTime(map, Blocks.SHROOMLIGHT.asItem(), 240*multiplier);
        return map;
    }

    private static void addItemTagBurnTime(Map<Item, Integer> map, Tag<Item> itemTag, int p_213992_2_) {
        for(Item item : itemTag.values()) {
            map.put(item, p_213992_2_);
        }

    }

    private static void addItemBurnTime(Map<Item, Integer> map, Item itemProvider, int burnTimeIn) {
        map.put(itemProvider, burnTimeIn);
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.items);
        this.burnTime = nbt.getInt("BurnTime");
        this.cookTime = nbt.getInt("CookTime");
        this.cookTimeTotal = nbt.getInt("CookTimeTotal");
        this.redstoneMode = nbt.getInt("RedstoneMode");
        this.liquidMode = nbt.getInt("LiquidMode");
        this.tank.setFluid(new FluidStack(ModFluids.BROMINE_FLUID.get(),nbt.getInt("WasteAmount")));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("BurnTime", this.burnTime);
        nbt.putInt("CookTime", this.cookTime);
        nbt.putInt("CookTimeTotal", this.cookTimeTotal);
        nbt.putInt("RedstoneMode", this.redstoneMode);
        nbt.putInt("LiquidMode", this.liquidMode);
        nbt.putInt("WasteAmount", this.tank.getFluidAmount());
        Inventories.writeNbt(nbt, this.items);
        return nbt;
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, GasCentrifugeBlockEntity blockEntity)
    {
        boolean flag = this.isBurning();
        boolean flag1 = false;
        if (this.isBurning()) {
            --this.burnTime;
        }

        if (!this.world.isClient) {
            if (liquidMode == 2)
            {
                if (ticksBeforeDumping <= 0)
                {
                    if (tank.getFluidAmount() >= 10)
                    {
                        world.getNonSpectatingEntities(Entity.class, new Box(pos.offset(Direction.NORTH, 4).offset(Direction.WEST, 4).offset(Direction.UP, 4),
                                pos.offset(Direction.SOUTH, 4).offset(Direction.EAST, 4).offset(Direction.DOWN, 4))).forEach(entity ->
                        {
                            if (entity instanceof LivingEntity)
                                ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(ModMiscs.BROMINE_POISON, 80, 0));
                        });
                        tank.drain(10, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
                else
                {
                    ticksBeforeDumping--;
                }
            }

            ItemStack itemstack = this.items.get(1);
            if (this.isBurning() || !itemstack.isEmpty() && !this.items.get(0).isEmpty()) {
                if (!this.isBurning() && this.canSmelt()) {
                    this.burnTime = this.getBurnTime(itemstack);
                    //this.recipesUsed = this.burnTime;
                    if (this.isBurning()) {
                        flag1 = true;
                        if (itemstack.hasContainerItem())
                            this.items.set(1, itemstack.getContainerItem());
                        else
                        if (!itemstack.isEmpty()) {
                            Item item = itemstack.getItem();
                            itemstack.decrement(1);
                            if (itemstack.isEmpty()) {
                                this.items.set(1, itemstack.getContainerItem());
                            }
                        }
                    }
                }

                if (this.isBurning() && this.canSmelt()) {
                    ++this.cookTime;
                    if (this.cookTime == this.cookTimeTotal) {
                        this.cookTime = 0;
                        this.cookTimeTotal = getCookTimeTotal();
                        this.placeItemsInRightSlot();
                        flag1 = true;
                    }
                } else {
                    this.cookTime = 0;
                }
            } else if (!this.isBurning() && this.cookTime > 0) {
                this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.cookTimeTotal);
            }

            if (flag != this.isBurning()) {
                flag1 = true;
                //this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, Boolean.valueOf(this.isBurning())), 3);
            }
        }

        if (flag1) {
            this.markDirty();
        }
    }

    private int getCookTimeTotal()
    {
        return 100;
    }

    protected boolean canSmelt() {
        if (!this.items.get(0).isEmpty()) {
            ItemStack[] itemstacks = GasCentrifugeRecipe.getRecipeOutputs(items.get(0));
            if (itemstacks[0].isEmpty()&&itemstacks[1].isEmpty()&&itemstacks[2].isEmpty()&&itemstacks[3].isEmpty())
            {
                return false;
            } else if (redstoneMode==1&&world.getReceivedRedstonePower(pos)>0||redstoneMode==2&&world.getRedstonePowerFromNeighbors(pos)<1)
            {
                return false;
            } else {
                ItemStack[] itemstacks1 = new ItemStack[] {this.items.get(2),this.items.get(3),this.items.get(4),this.items.get(5)};
                boolean[] output0 = new boolean[] {true,true,true,true};
                for (int i = 0; i < itemstacks.length-1; i++)
                {
                    ItemStack itemstack = itemstacks[i];
                    ItemStack itemstack1 = itemstacks1[i];
                    if (itemstack1.isEmpty()) {
                        output0[i] =  true;
                    } else if (!itemstack1.isItemEqual(itemstack)) {
                        output0[i] =  false;
                    } else if (itemstack1.getCount() + itemstack.getCount() <= this.getMaxCountPerStack() && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                        // Forge fix: make furnace respect stack sizes in furnace recipes
                        output0[i] =  true;
                    } else {
                        output0[i] =  itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxCount();
                        // Forge fix: make furnace respect stack sizes in furnace recipes
                    }
                }
                return output0[0]&&output0[1]&&output0[2]&&output0[3];
            }
        } else {
            return false;
        }
    }

    private void placeItemsInRightSlot(/*Here was recipe*/)
    {
        if (this.canSmelt()) {
            ItemStack itemstack = this.items.get(0);
            ItemStack[] itemstacks = GasCentrifugeRecipe.getRecipeOutputs(items.get(0));
            ItemStack itemstack2 = this.items.get(2);
            ItemStack itemstack3 = this.items.get(3);
            ItemStack itemstack4 = this.items.get(4);
            ItemStack itemstack5 = this.items.get(5);
            TheoreticalFluid theoreticalFluid = GasCentrifugeRecipe.getFluid(items.get(0));
            if (itemstack2.isEmpty()) {
                this.items.set(2, itemstacks[0].copy());
            } else if (itemstack2.getItem() == itemstacks[0].getItem()) {
                itemstack2.increment(itemstacks[0].getCount());
            }
            if (itemstack3.isEmpty()) {
                this.items.set(3, itemstacks[1].copy());
            } else if (itemstack3.getItem() == itemstacks[1].getItem()) {
                itemstack3.increment(itemstacks[1].getCount());
            }
            if (itemstack4.isEmpty()) {
                this.items.set(4, itemstacks[2].copy());
            } else if (itemstack4.getItem() == itemstacks[2].getItem()) {
                itemstack4.increment(itemstacks[2].getCount());
            }
            if (itemstack5.isEmpty()) {
                this.items.set(5, itemstacks[3].copy());
            } else if (itemstack5.getItem() == itemstacks[3].getItem()) {
                itemstack5.increment(itemstacks[3].getCount());
            }

            int unbreaking_lvl = 0;
            for (NbtElement enchantment : itemstack.getEnchantments())
            {
                if (((NbtCompound) enchantment).getString("id").equals("minecraft:unbreaking"))
                unbreaking_lvl = ((NbtCompound)enchantment).getShort("lvl");
            }

            itemstack.setDamage(itemstack.getDamage()+
                    (
                            unbreaking_lvl==0
                                    ?
                                    1
                                    :
                                    (new Random().nextInt(unbreaking_lvl+1)==1?1:0)
                    ));
            //unbreaking_lvl==0 ? 1 : (new Random().nextInt(unbreaking_lvl+1)==1?1:0)

            if (itemstack.getDamage() >= itemstack.getMaxDamage())
                this.items.set(0, ItemStack.EMPTY);

            if (theoreticalFluid != null)
            {
                if (tank.getFluidAmount()<1000&&furnaceData.get(4)==0||tank.getFluidAmount()<4000&&furnaceData.get(4)==1)
                    //furnaceData.set(5,furnaceData.get(5)+theoreticalFluid.amount);
                    tank.fill(new FluidStack(ModFluids.BROMINE_FLUID.get(),theoreticalFluid.amount), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    protected int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        } else {
            Item item = stack.getItem();
            return getBurnTimes().getOrDefault(item, 0);
        }
    }

    public boolean isFuel(ItemStack stack) {
        return getBurnTime(stack) > 0;
    }

    /*@Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_DOWN;
        } else {
            return SLOTS_UP;
        }
    }*/

    /**
     * Returns true if automation can insert the given item in the given slot from the given side.
     */
    @Override
    public boolean canInsert(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    /**
     * Returns true if automation can extract the given item in the given slot from the given side.
     */
    @Override
    public boolean canExtract(int index, ItemStack stack, Direction direction)
    {
        return true;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getStack(int slot) {
        return this.items.get(slot);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.items, slot, amount);
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.items, slot);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack itemstack = this.items.get(slot);
        boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemsEqual(stack, itemstack); // areItemStackTagsEqual
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }

        if (slot == 0 && !flag) {
            this.cookTimeTotal = getCookTimeTotal();
            this.cookTime = 0;
            this.markDirty();
        }
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public boolean isFilter(ItemStack stack){
        return stack.getItem() instanceof FilterItem;
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) {
            return isFilter(stack);
        } else if (index == 1) {
            return isFuel(stack);
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    public void onCrafting(PlayerEntity player) {
    }

    /*net.minecraftforge.common.util.LazyOptional<? extends net.minecraftforge.items.IItemHandler>[] handlers =
            net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable Direction facing) {
        if (!this.removed && facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.UP)
                return handlers[0].cast();
            else if (facing == Direction.DOWN)
                return handlers[1].cast();
            else
                return handlers[2].cast();
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, facing)));
        }
        return super.getCapability(capability, facing);
    }*/

    public FluidTank getTank()
    {
        return tank;
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from)
    {
        return new IFluidTank[]{tank};
    }

    @Override
    public IFluidTank[] getAllTanks()
    {
        return new IFluidTank[]{tank};
    }


    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, IFluidHandler.FluidAction fluidAction) {
        if (canFill(from, resource)) {
            return tank.fill(resource, fluidAction);
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, IFluidHandler.FluidAction fluidAction) {
        if (canDrain(from, FluidStack.EMPTY)) {
            return tank.drain(maxDrain, fluidAction);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return true;
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return true;
    }
}