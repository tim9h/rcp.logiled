package dev.tim9h.rcp.logiled.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;
import com.logitech.gaming.LogiLED;

import dev.tim9h.rcp.logging.InjectLogger;

@Singleton
public class LightingService {

	@InjectLogger
	private Logger logger;

	private static final long INIT_RETRY_DELAY_MS = 5000;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
		var thread = new Thread(r, "logiled");
		thread.setDaemon(true);
		return thread;
	});

	// only accessed from the executor thread
	private boolean initialized;

	private long lastInitAttempt;

	private boolean ensureInitialized() {
		if (initialized) {
			return true;
		}
		var now = System.currentTimeMillis();
		if (lastInitAttempt != 0 && now - lastInitAttempt < INIT_RETRY_DELAY_MS) {
			return false;
		}
		lastInitAttempt = now;
		logger.debug(() -> "Initializing LogiLED");
		initialized = LogiLED.LogiLedInit();
		if (!initialized) {
			logger.warn(() -> "LogiLED initialization failed");
		}
		return initialized;
	}

	public void setLighting(int redPercentage, int greenPercentage, int bluePercentage) {
		submit(() -> {
			if (ensureInitialized()) {
				logger.debug(() -> "Setting lighting");
				LogiLED.LogiLedSetLighting(redPercentage, greenPercentage, bluePercentage);
			}
		});
	}

	public void setLighting(double red, double green, double blue) {
		setLighting((int) (red * 100), (int) (green * 100), (int) (blue * 100));
	}

	public void shutdown() {
		submit(this::shutdownNative);
	}

	/**
	 * Releases the native handle and stops the background thread. The service can
	 * no longer be used afterwards.
	 */
	public void close() {
		try {
			executor.execute(this::shutdownNative);
		} catch (RejectedExecutionException _) {
			return;
		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException _) {
			Thread.currentThread().interrupt();
			executor.shutdownNow();
		}
	}

	private void shutdownNative() {
		lastInitAttempt = 0;
		if (!initialized) {
			logger.debug(() -> "LogiLED not initialized, skipping shutdown");
			return;
		}
		logger.debug(() -> "Shutting down LogiLED");
		initialized = false;
		LogiLED.LogiLedShutdown();
	}

	private void submit(Runnable task) {
		try {
			executor.execute(task);
		} catch (RejectedExecutionException _) {
			logger.debug(() -> "LightingService closed, ignoring request");
		}
	}

}
