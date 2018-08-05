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

import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.Slot;
import wdl.handler.HandlerException;

public class HopperMinecartHandler extends EntityHandler<EntityMinecartHopper, ContainerHopper> {

	public HopperMinecartHandler() {
		super(EntityMinecartHopper.class, ContainerHopper.class);
	}

	@Override
	public String copyData(ContainerHopper container, EntityMinecartHopper minecart, boolean riding) throws HandlerException {
		for (int i = 0; i < minecart.getSizeInventory(); i++) {
			Slot slot = container.getSlot(i);
			if (slot.getHasStack()) {
				minecart.setInventorySlotContents(i, slot.getStack());
			}
		}

		return "wdl.messages.onGuiClosedInfo.savedEntity.hopperMinecart";
	}

}
