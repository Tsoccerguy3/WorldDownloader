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

import java.util.Arrays;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import wdl.VersionedProperties;
import wdl.ducks.INetworkNameable;
import wdl.handler.BaseHandler;
import wdl.handler.HandlerException;

/**
 * A handler for an arbitrary block entity.
 *
 * @param <B> The type of block entity to handle.
 * @param <C> The type of container associated with that block entity.
 */
public abstract class BlockHandler<B extends TileEntity, C extends Container> extends BaseHandler {
	/**
	 * Constructor.
	 *
	 * @param blockEntityClass
	 *            A strong reference to the block entity class this handles.
	 * @param containerClass
	 *            A strong reference to the container class this handles.
	 * @param defaultNames
	 *            A (potentially empty) list of the I18n keys for the default names,
	 *            which is used if {@link INetworkNameable} is not available.
	 */
	protected BlockHandler(Class<B> blockEntityClass, Class<C> containerClass, String... defaultNames) {
		this.blockEntityClass = blockEntityClass;
		this.containerClass = containerClass;
		this.defaultNames = defaultNames;
		Arrays.sort(defaultNames);
	}
	protected final @Nonnull Class<B> blockEntityClass;
	protected final @Nonnull Class<C> containerClass;
	/** Translation strings for the default name(s) */
	private final String[] defaultNames;
	/** Gets the type of block entity handled by this. */
	public final Class<B> getBlockEntityClass() {
		return blockEntityClass;
	}
	/** Gets the type of container handled by this. */
	public final Class<C> getContainerClass() {
		return containerClass;
	}

	/**
	 * Saves the contents of a block entity from the container. This method casts
	 * its parameters to combat type erasure.
	 *
	 * @param clickedPos
	 *            The position that the clicked block is at. It is assumed that
	 *            blockEntity is at that position.
	 * @param container
	 *            The container to grab items from. Must be an instance of
	 *            <code>B</code>.
	 * @param blockEntity
	 *            The block entity at the given position. Must be an instance of
	 *            <code>C</code>.
	 * @param world
	 *            The world to query if more information is needed.
	 * @param saveMethod
	 *            The method to call to save block entities.
	 * @return A translation key to put into chat describing what was saved.
	 * @throws HandlerException
	 *             When something is handled wrong.
	 * @throws ClassCastException
	 *             If container or blockEntity are not instances of the handled class.
	 */
	public final String handleCasting(BlockPos clickedPos, Container container,
			TileEntity blockEntity, IBlockReader world,
			BiConsumer<BlockPos, B> saveMethod) throws HandlerException, ClassCastException {
		B b = blockEntityClass.cast(blockEntity);
		C c = containerClass.cast(container);
		return handle(clickedPos, c, b, world, saveMethod);
	}

	/**
	 * Saves the contents of a block entity from the container.
	 *
	 * @param clickedPos
	 *            The position that the clicked block is at.  It is assumed that
	 *            blockEntity is at that position.
	 * @param container
	 *            The container to grab items from.
	 * @param blockEntity
	 *            The block entity at the given position.
	 * @param world
	 *            The world to query if more information is needed.
	 * @param saveMethod
	 *            The method to call to save block entities.
	 * @return A translation key to put into chat describing what was saved.
	 * @throws HandlerException
	 *             When something is handled wrong.
	 */
	public abstract String handle(BlockPos clickedPos, C container,
			B blockEntity, IBlockReader world,
			BiConsumer<BlockPos, B> saveMethod) throws HandlerException;

	/**
	 * Saves the fields of an inventory.
	 * Fields are pieces of data such as furnace smelt time and
	 * beacon effects.
	 *
	 * @param inventory The inventory to save from.
	 * @param tileEntity The inventory to save to.
	 */
	protected static void saveInventoryFields(IInventory inventory,
			IInventory tileEntity) {
		for (int i = 0; i < inventory.getFieldCount(); i++) {
			tileEntity.setField(i, inventory.getField(i));
		}
	}

	/**
	 * Gets the "true" custom display name of this item. For instance, a furnace
	 * that has not been renamed will return null, but a furnace that has been named
	 * "smelter" will return "smelter", and a furnace that has been renamed to
	 * "Furnace" will return "Furnace" (and not null).
	 *
	 * <p>Note that this method will attempt to use the {@link INetworkNameable}
	 * implementation, but if the given inventory does not implement that,
	 * then it will ignore it.  This may happen if mixins failed to apply,
	 * for instance.
	 *
	 * @param inventory the inventory to check
	 * @return The actual name from the network, or null if no custom name was set.
	 */
	@Nullable
	protected String getCustomDisplayName(IInventory inventory) {
		if (inventory instanceof INetworkNameable) {
			return ((INetworkNameable) inventory).getCustomDisplayName();
		}
		// Fallback, will fail for situations where the custom name
		// is the vanilla name
		String name = inventory.getDisplayName().getUnformattedText();
		for (String key : defaultNames) {
			if (I18n.format(key).equals(name)) {
				return null;
			}
		}
		return name;
	}

	/**
	 * Looks up the handler that handles the given block entity/container combo,
	 * from {@link VersionedProperties#BLOCK_HANDLERS}.
	 *
	 * @param blockEntityClass The type for the block entity.
	 * @param containerClass The type for the container.
	 * @return The handler, or null if none is found.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <B extends TileEntity, C extends Container> BlockHandler<B, C> getHandler(Class<B> blockEntityClass, Class<C> containerClass) {
		for (BlockHandler<?, ?> h : VersionedProperties.BLOCK_HANDLERS) {
			if (h.getBlockEntityClass().equals(blockEntityClass) &&
					h.getContainerClass().equals(containerClass)) {
				return (BlockHandler<B, C>)h;
			}
		}

		return null;
	}
}
