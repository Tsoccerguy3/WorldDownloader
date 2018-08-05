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

import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

public class BrewingStandTest extends AbstractBlockHandlerTest<TileEntityBrewingStand, ContainerBrewingStand, BrewingStandHandler> {

	public BrewingStandTest() {
		super(TileEntityBrewingStand.class, ContainerBrewingStand.class, BrewingStandHandler.class);
	}

	/**
	 * Simple case for brewing stands
	 */
	@Test
	public void testInventory() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		TileEntityBrewingStand te = new TileEntityBrewingStand();
		te.setInventorySlotContents(0, new ItemStack(Items.GLASS_BOTTLE));
		te.setInventorySlotContents(1, new ItemStack(Items.GLASS_BOTTLE));
		te.setInventorySlotContents(2, new ItemStack(Items.GLASS_BOTTLE));
		te.setInventorySlotContents(3, new ItemStack(Items.SPIDER_EYE));
		te.setInventorySlotContents(4, new ItemStack(Items.BLAZE_POWDER));
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	/**
	 * Test brewing stand fields
	 */
	@Test
	public void testFields() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		TileEntityBrewingStand te = new TileEntityBrewingStand();
		te.setField(0, 10); // brew time
		te.setField(1, 13); // fuel
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	/**
	 * Test a normal custom name
	 */
	@Test
	public void testCustomName() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		TileEntityBrewingStand te = new TileEntityBrewingStand();
		te.setName("Potion maker");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	/**
	 * Test a normal custom name matching the vanilla name
	 */
	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeMixinsApplied();

		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		TileEntityBrewingStand te = new TileEntityBrewingStand();
		te.setName("Brewing Stand");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
