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
package wdl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.init.Bootstrap;

public class WDLTestMain {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		if (Bootstrap.isRegistered()) {
			LOGGER.warn("Bootstrap already initialized.");
			return;
		}
		LOGGER.debug("Initializing bootstrap...");
		Bootstrap.register();
		LOGGER.debug("Initialized bootstrap.");
		// Note: not checking Bootstrap.hasErrored as that didn't exist in this version

		LOGGER.debug("Setting up I18n...");
		// Needed to prepare a valid Locale instance for certain tests that depend upon it
		ReflectionUtils.findAndSetPrivateField(I18n.class, Locale.class, new FakeLocale());
		LOGGER.debug("Set up I18n.");
	}

	/**
	 * A Locale class that delegates to the other, deprecated I18n class (although
	 * the annotation does not exist in a version this old), which works during
	 * testing for some reason (not depending on other assets?)
	 */
	private static class FakeLocale extends Locale {
		@Override
		public String formatMessage(String translateKey, Object[] parameters) {
			return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(translateKey, parameters);
		}
		@Override
		public boolean hasKey(String key) {
			return net.minecraft.util.text.translation.I18n.canTranslate(key);
		}
	}

}
