/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import wdl.WDL;
import wdl.gui.widget.Screen;

/**
 * Base GUI with the player in the background turning slowly to show the
 * entire world.
 */
public abstract class GuiTurningCameraBase extends Screen {
	/**
	 * Current yaw.
	 */
	private float yaw;
	/**
	 * The previous mode for the camera (First person, 3rd person, ect)
	 */
	private int oldCameraMode;
	/**
	 * The previous state as to whether the hud was hidden with F1.
	 */
	private boolean oldHideHud;
	/**
	 * The previous state as to whether the debug menu was enabled with F#.
	 */
	private boolean oldShowDebug;
	/**
	 * The previous chat visibility.
	 */
	private EnumChatVisibility oldChatVisibility;
	/**
	 * The player to preview.
	 */
	private EntityPlayerSP cam;
	/**
	 * The previous render view entity (the entity which Minecraft uses
	 * for the camera)
	 */
	private Entity oldRenderViewEntity;
	/**
	 * Whether the camera has been set up.
	 */
	private boolean initializedCamera = false;

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	public void initGui() {
		if (!initializedCamera) {
			this.cam = LocalUtils.makePlayer();
			this.cam.setLocationAndAngles(WDL.thePlayer.posX, WDL.thePlayer.posY,
					WDL.thePlayer.posZ, WDL.thePlayer.rotationYaw, 0.0F);
			this.yaw = WDL.thePlayer.rotationYaw;
			this.oldCameraMode = WDL.minecraft.gameSettings.thirdPersonView;
			this.oldHideHud = WDL.minecraft.gameSettings.hideGUI;
			this.oldShowDebug = WDL.minecraft.gameSettings.showDebugInfo;
			this.oldChatVisibility = WDL.minecraft.gameSettings.chatVisibility;
			this.oldRenderViewEntity = WDL.minecraft.getRenderViewEntity();

			initializedCamera = true;
		}
	}

	/**
	 * Speed for a rotation, as a rough scale, in degrees per frame.
	 */
	private static final float ROTATION_SPEED = 1.0f;
	/**
	 * Change between the slowest speed and the average speed.
	 */
	private static final float ROTATION_VARIANCE = .7f;

	/**
	 * Increment yaw to the yaw for the next tick.
	 */
	@Override
	public void updateScreen() {
		if (mc.world != null && this.initializedCamera) {
			this.cam.prevRotationPitch = this.cam.rotationPitch = 0.0F;
			this.cam.prevRotationYaw = this.yaw;
			this.cam.lastTickPosY = this.cam.prevPosY = this.cam.posY;
			this.cam.lastTickPosX = this.cam.prevPosX = this.cam.posX;
			this.cam.lastTickPosZ = this.cam.prevPosZ = this.cam.posZ;

			// TODO: Rewrite this function as a function of time, rather than
			// an incremental function, if it's possible to do so.
			// Due to the fact that it refers to itself, I have no idea how to
			// approach the problem - it's some kind of integration that would be
			// needed, but it's really complex.

			// Yaw is in degrees, but Math.cos is in radians. The
			// "(this.yaw + 45) / 45.0 * Math.PI)" portion basically makes cosine
			// give the lowest values in each cardinal direction and the highest
			// while looking diagonally.  These are then multiplied by .7 and added
			// to 1, which creates a speed varying from .3 to 1.7.  This causes it
			// to speed through diagonals and go slow in cardinal directions, which
			// is the behavior we want.
			this.yaw = (this.yaw + ROTATION_SPEED
					* (float) (1 + ROTATION_VARIANCE
							* Math.cos((this.yaw + 45) / 45.0 * Math.PI)));

			this.cam.rotationYaw = this.yaw;

			double x = Math.cos(yaw / 180.0D * Math.PI);
			double z = Math.sin((yaw - 90) / 180.0D * Math.PI);

			double distance = truncateDistanceIfBlockInWay(x, z, .5);
			this.cam.posY = WDL.thePlayer.posY;
			this.cam.posX = WDL.thePlayer.posX - distance * x;
			this.cam.posZ = WDL.thePlayer.posZ + distance * z;

			this.cam.chunkCoordX = MathHelper.floor(this.cam.posX / 16.0D);
			this.cam.chunkCoordY = MathHelper.floor(this.cam.posY / 16.0D);
			this.cam.chunkCoordZ = MathHelper.floor(this.cam.posZ / 16.0D);
		}

		this.deactivateRenderViewEntity();

		super.updateScreen();
	}

	/**
	 * Truncates the distance so that the camera does not clip into blocks.
	 * Based off of {@link net.minecraft.client.renderer.EntityRenderer#orientCamera(float)}.
	 * @param camX X-position of the camera.
	 * @param camZ Z-position of the camera.
	 * @param currentDistance Current distance from the camera.
	 * @return A new distance, equal to or less than <code>currentDistance</code>.
	 */
	private double truncateDistanceIfBlockInWay(double camX, double camZ, double currentDistance) {
		Vec3d playerPos = WDL.thePlayer.getPositionVector().add(0, WDL.thePlayer.getEyeHeight(), 0);
		Vec3d offsetPos = new Vec3d(WDL.thePlayer.posX - currentDistance * camX, WDL.thePlayer.posY + WDL.thePlayer.getEyeHeight(), WDL.thePlayer.posZ + camZ);

		// NOTE: Vec3.addVector and Vec3.add return new vectors and leave the
		// current vector unmodified.
		for (int i = 0; i < 9; i++) {
			// Check offset slightly in all directions.
			float offsetX = ((i & 0x01) != 0) ? -.1f : .1f;
			float offsetY = ((i & 0x02) != 0) ? -.1f : .1f;
			float offsetZ = ((i & 0x04) != 0) ? -.1f : .1f;

			if (i == 8) {
				offsetX = 0;
				offsetY = 0;
				offsetZ = 0;
			}

			Vec3d from = playerPos.add(offsetX, offsetY, offsetZ);
			Vec3d to = offsetPos.add(offsetX, offsetY, offsetZ);

			RayTraceResult pos = mc.world.rayTraceBlocks(from, to);

			if (pos != null) {
				double distance = pos.hitVec.distanceTo(playerPos);
				if (distance < currentDistance && distance > 0) {
					currentDistance = distance;
				}
			}
		}

		return currentDistance - .25;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		this.deactivateRenderViewEntity();
	}

	/**
	 * Draws a dirt background if there is no world.  Subclasses must call this,
	 * to prevent <a href="https://user-images.githubusercontent.com/8334194/35199077-95ed2e84-fef0-11e7-83d7-bd6088169c84.png">
	 * graphical issues</a> when there is no world.
	 */
	@Override
	public void drawDefaultBackground() {
		if (mc.world == null) {
			this.drawBackground(0);
		}
	}

	/**
	 * Called when the client world ticks, from a static context.
	 */
	public static void onWorldTick() {
		GuiScreen screen = WDL.minecraft.currentScreen;
		if (screen instanceof GuiTurningCameraBase) {
			((GuiTurningCameraBase) screen).onWorldTick0();
		}
	}

	/**
	 * Called when the world ticks.
	 * Note that we do this in the world tick instead of the normal GUI tick,
	 * because the GUI tick happens before the world ticks entities while this happens
	 * after entities have been ticked.  We don't want the camera to be active when
	 * entities are being ticked, because that causes some subtle issues.
	 */
	private void onWorldTick0() {
		this.activateRenderViewEntity();
	}

	/**
	 * Sets the render view entity to the custom camera.
	 */
	private void activateRenderViewEntity() {
		if (!this.initializedCamera) return;

		WDL.minecraft.gameSettings.thirdPersonView = 0;
		WDL.minecraft.gameSettings.hideGUI = true;
		WDL.minecraft.gameSettings.showDebugInfo = false;
		WDL.minecraft.gameSettings.chatVisibility = EnumChatVisibility.HIDDEN;
		WDL.minecraft.setRenderViewEntity(this.cam);
	}

	/**
	 * Returns the render view entity to the normal player.
	 */
	private void deactivateRenderViewEntity() {
		if (!this.initializedCamera) return;

		WDL.minecraft.gameSettings.thirdPersonView = this.oldCameraMode;
		WDL.minecraft.gameSettings.hideGUI = oldHideHud;
		WDL.minecraft.gameSettings.showDebugInfo = oldShowDebug;
		WDL.minecraft.gameSettings.chatVisibility = oldChatVisibility;
		WDL.minecraft.setRenderViewEntity(this.oldRenderViewEntity);
	}
}
