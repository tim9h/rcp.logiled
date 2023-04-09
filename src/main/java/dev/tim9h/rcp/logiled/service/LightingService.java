package dev.tim9h.rcp.logiled.service;

import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;
import com.logitech.gaming.LogiLED;

import dev.tim9h.rcp.logging.InjectLogger;

@Singleton
public class LightingService {

	@InjectLogger
	private Logger logger;

	private boolean initialized;

	private void init() {
		logger.debug(() -> "Initializing LogiLED");
		initialized = LogiLED.LogiLedInit();
	}

	public void setLighting(int redPercentage, int greenPercentage, int bluePercentage) {
		if (!initialized) {
			init();
		}
		logger.info(() -> "Setting lighting");
		LogiLED.LogiLedSetLighting(redPercentage, greenPercentage, bluePercentage);
	}

	public void setLighting(double red, double green, double blue) {
		if (!initialized) {
			init();
		}
		logger.info(() -> "Setting lighting");
		LogiLED.LogiLedSetLighting((int) (red * 100), (int) (green * 100), (int) (blue * 100));
	}

	public void shutdown() {
		logger.debug(() -> "Shutting down LogiLED");
		initialized = false;
		LogiLED.LogiLedShutdown();
	}

}
