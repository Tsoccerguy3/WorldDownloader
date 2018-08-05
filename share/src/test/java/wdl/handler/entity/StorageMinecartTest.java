/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.entity;

import org.junit.Test;

import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import wdl.handler.HandlerException;

public class StorageMinecartTest extends AbstractEntityHandlerTest<EntityMinecartChest, ContainerChest, StorageMinecartHandler> {

	public StorageMinecartTest() {
		super(EntityMinecartChest.class, ContainerChest.class, StorageMinecartHandler.class);
	}

	@Test
	public void testStorageMinecart() throws HandlerException {
		makeMockWorld();
		EntityMinecartChest minecart = new EntityMinecartChest(serverWorld);
		minecart.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		addEntity(minecart);

		runHandler(minecart.getEntityId(), createClientContainer(minecart));

		checkAllEntities();
	}
}
