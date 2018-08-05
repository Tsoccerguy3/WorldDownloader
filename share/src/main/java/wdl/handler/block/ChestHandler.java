/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.inventory.ContainerChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import wdl.WDLMessageTypes;
import wdl.handler.HandlerException;

public class ChestHandler extends BlockHandler<TileEntityChest, ContainerChest> {
	public ChestHandler() {
		super(TileEntityChest.class, ContainerChest.class, "container.chest", "container.chestDouble");
	}

	@Override
	public String handle(BlockPos clickedPos, ContainerChest container,
			TileEntityChest blockEntity, IBlockReader world,
			BiConsumer<BlockPos, TileEntityChest> saveMethod) throws HandlerException {
		String title = getCustomDisplayName(container.getLowerChestInventory());

		if (container.inventorySlots.size() > 63) {
			saveDoubleChest(clickedPos, container, blockEntity, world, saveMethod, title);
			return "wdl.messages.onGuiClosedInfo.savedTileEntity.doubleChest";
		} else {
			saveSingleChest(clickedPos, container, blockEntity, world, saveMethod, title);
			return "wdl.messages.onGuiClosedInfo.savedTileEntity.singleChest";
		}
	}
	/**
	 * Saves the contents of a single chest.
	 *
	 * @param clickedPos As per {@link #handle}
	 * @param container As per {@link #handle}
	 * @param blockEntity As per {@link #handle}
	 * @param world As per {@link #handle}
	 * @param saveMethod As per {@link #handle}
	 * @param displayName The custom name of the chest, or <code>null</code> if none is set.
	 * @throws HandlerException As per {@link #handle}
	 */
	private void saveSingleChest(BlockPos clickedPos, ContainerChest container,
			TileEntityChest blockEntity, IBlockReader world,
			BiConsumer<BlockPos, TileEntityChest> saveMethod,
			@Nullable String displayName) throws HandlerException {
		saveContainerItems(container, blockEntity, 0);
		if (displayName != null) {
			blockEntity.setCustomName(displayName);
		}
		saveMethod.accept(clickedPos, blockEntity);
	}
	/**
	 * Saves the contents of a double-chest, first identifying the location of both
	 * chests. This method does not handle triple/quadruple/quintuple chests.
	 *
	 * @param clickedPos As per {@link #handle}
	 * @param container As per {@link #handle}
	 * @param blockEntity As per {@link #handle}
	 * @param world As per {@link #handle}
	 * @param saveMethod As per {@link #handle}
	 * @param displayName The custom name of the chest, or <code>null</code> if none is set.
	 * @throws HandlerException As per {@link #handle}
	 */
	private void saveDoubleChest(BlockPos clickedPos, ContainerChest container,
			TileEntityChest blockEntity, IBlockReader world,
			BiConsumer<BlockPos, TileEntityChest> saveMethod,
			@Nullable String displayName) throws HandlerException {
		// This is messy, but it needs to be like this because
		// the left and right chests must be in the right positions.

		BlockPos pos1, pos2;
		TileEntity te1, te2;

		pos1 = clickedPos;
		te1 = world.getTileEntity(clickedPos);
		assert te1 instanceof TileEntityChest;

		// We need separate variables for the above reason --
		// pos1 isn't always the same as chestPos1 (and thus
		// chest1 isn't always te1).
		BlockPos chestPos1 = null, chestPos2 = null;
		TileEntityChest chest1 = null, chest2 = null;

		pos2 = pos1.add(0, 0, 1);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {

			chest1 = (TileEntityChest) te1;
			chest2 = (TileEntityChest) te2;

			chestPos1 = pos1;
			chestPos2 = pos2;
		}

		pos2 = pos1.add(0, 0, -1);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {

			chest1 = (TileEntityChest) te2;
			chest2 = (TileEntityChest) te1;

			chestPos1 = pos2;
			chestPos2 = pos1;
		}

		pos2 = pos1.add(1, 0, 0);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {
			chest1 = (TileEntityChest) te1;
			chest2 = (TileEntityChest) te2;

			chestPos1 = pos1;
			chestPos2 = pos2;
		}

		pos2 = pos1.add(-1, 0, 0);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {
			chest1 = (TileEntityChest) te2;
			chest2 = (TileEntityChest) te1;

			chestPos1 = pos2;
			chestPos2 = pos1;
		}

		if (chest1 == null || chest2 == null ||
				chestPos1 == null || chestPos2 == null) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.failedToFindDoubleChest", WDLMessageTypes.ERROR);
		}

		saveContainerItems(container, chest1, 0);
		saveContainerItems(container, chest2, 27);

		if (displayName != null) {
			// This is NOT server-accurate.  But making it correct is not easy.
			// Only one of the chests needs to have the name.
			chest1.setCustomName(displayName);
			chest2.setCustomName(displayName);
		}

		saveMethod.accept(chestPos1, chest1);
		saveMethod.accept(chestPos2, chest2);
	}
}
