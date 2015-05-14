/*
 * This file is part of Restructured, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.Restructured;

import net.minecraftforge.common.config.Configuration;

public final class ModOptions {

	protected static final String CATEGORY_MODS = "mods";

	protected static final String CATEGORY_LOGGING_CONTROL = "logging";
	protected static final String CONFIG_ENABLE_DEBUG_LOGGING = "Enable Debug Logging";
	protected static final String CONFIG_ENABLE_WAILA = "Enable Waila Display";

	protected static boolean enableDebugLogging = false;
	protected static boolean enableWailaDisplay = true;

	public static void load(Configuration config) {

		enableDebugLogging = config.getBoolean(CONFIG_ENABLE_DEBUG_LOGGING,
				CATEGORY_LOGGING_CONTROL, enableDebugLogging,
				"Enables/disables debug logging of the mod");

		enableWailaDisplay = config.getBoolean(CONFIG_ENABLE_WAILA,
				CATEGORY_MODS, enableWailaDisplay,
				"Enables/disables display of scrap information via Waila");
	}

	public static boolean getEnableDebugLogging() {
		return enableDebugLogging;
	}

	public static boolean getEnableWaila() {
		return enableWailaDisplay;
	}
}
