package shadows.fastbench.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.fastbench.net.LastRecipeMessage;
import shadows.placebo.Placebo;

public class ContainerFastBench extends ContainerWorkbench {

	public IRecipe lastRecipe;
	IRecipe lastLastRecipe;

	public ContainerFastBench(EntityPlayer player, World world, int x, int y, int z) {
		this(player, world, new BlockPos(x, y, z));
	}

	public ContainerFastBench(EntityPlayer player, World world, BlockPos pos) {
		super(player.inventory, world, pos);
		this.inventorySlots.clear();
		this.inventoryItemStacks.clear();

		this.addSlotToContainer(new SlotCraftingSucks(this, player, this.craftMatrix, this.craftResult, 0, 124, 35));

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
			}
		}

		for (int k = 0; k < 3; ++k) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.addSlotToContainer(new Slot(player.inventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for (int l = 0; l < 9; ++l) {
			this.addSlotToContainer(new Slot(player.inventory, l, 8 + l * 18, 142));
		}
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		if (this.world.getBlockState(this.pos).getBlock() != Blocks.CRAFTING_TABLE) {
			return false;
		} else {
			return playerIn.getDistanceSq(pos) <= 64.0D;
		}
	}

	@Override
	protected void slotChangedCraftingGrid(World world, EntityPlayer player, InventoryCrafting inv, InventoryCraftResult result) {
		ItemStack itemstack = ItemStack.EMPTY;

		if (lastRecipe == null || !lastRecipe.matches(inv, world)) lastRecipe = CraftingManager.findMatchingRecipe(inv, world);

		if (lastRecipe != null) {
			itemstack = lastRecipe.getCraftingResult(inv);
		}

		if (!world.isRemote) {
			result.setInventorySlotContents(0, itemstack);
			EntityPlayerMP entityplayermp = (EntityPlayerMP) player;
			if (lastLastRecipe != lastRecipe) entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
			else if (lastLastRecipe != null && lastLastRecipe == lastRecipe && !ItemStack.areItemStacksEqual(lastLastRecipe.getCraftingResult(inv), lastRecipe.getCraftingResult(inv))) entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
			Placebo.NETWORK.sendTo(new LastRecipeMessage(lastRecipe), entityplayermp);
		}

		lastLastRecipe = lastRecipe;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		if (pos != BlockPos.ORIGIN) super.onContainerClosed(player);
		else {
			InventoryPlayer inv = player.inventory;
			if (!inv.getItemStack().isEmpty()) {
				player.dropItem(inv.getItemStack(), false);
				inv.setItemStack(ItemStack.EMPTY);
			}
			if (!this.world.isRemote) this.clearContainer(player, this.world, this.craftMatrix);
		}
	}

}