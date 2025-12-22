package dev.tim9h.rcp.logiled;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.rcp.event.CcEvent;
import dev.tim9h.rcp.event.EventManager;
import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.logiled.service.LightingService;
import dev.tim9h.rcp.logiled.util.ColorUtils;
import dev.tim9h.rcp.settings.Settings;
import dev.tim9h.rcp.spi.Mode;
import dev.tim9h.rcp.spi.Plugin;
import javafx.scene.paint.Color;

public class LogiledView implements Plugin {

	private static final String EVENT_LIGHTING_CHANGED = "LOGILED_LIGHTING_CHANGED";

	private static final String MODE_LOGILED = "logiled";

	private static final String SETTING_MODES = "core.modes";

	@InjectLogger
	private Logger logger;

	@Inject
	private EventManager eventManager;

	@Inject
	private LightingService lightingService;

	@Inject
	private Settings settings;

	@Override
	public String getName() {
		return "Logiled Integration";
	}

	@Override
	public void init() {
		logger.info(() -> "Created LogiledView");
		if (settings.getStringSet(SETTING_MODES).contains("alert")) {
			enableAlertLighting();
		}
	}

	@Override
	public void initBus(EventManager em) {
		Plugin.super.initBus(eventManager);
		em.listen("MODE_ALERT", this::handleAlert);
		em.listen("MODE_AFK", this::handleAfk);
	}

	private void handleAlert(Object[] args) {
		if ("off".equals(StringUtils.join(args))) {
			if (settings.getStringSet(SETTING_MODES).contains(MODE_LOGILED)) {
				var color = getColor(settings.getString(LogiledViewFactory.SETTING_LOGILED_COLOR));
				lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
				eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
			} else {
				lightingService.shutdown();
				eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED));
			}
		} else {
			enableAlertLighting();
		}
	}

	private void enableAlertLighting() {
		var color = Color.RED;
		lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
		eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
	}

	private void handleAfk(Object[] args) {
		if ("off".equals(StringUtils.join(args))) {
			if (settings.getStringSet(SETTING_MODES).contains(MODE_LOGILED)) {
				var color = getColor(settings.getString(LogiledViewFactory.SETTING_LOGILED_COLOR));
				lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
				eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
			} else {
				lightingService.shutdown();
				eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED));
			}
		} else {
			var color = Color.BLACK;
			lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
			eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
		}
	}

	private static Color getColor(String value) {
		Color color = null;
		try {
			color = Color.web(value);
		} catch (IllegalArgumentException _) {
			var split = value.replace(",", " ").split(" ");
			if (split.length == 3) {
				try {
					color = Color.rgb(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
							Integer.parseInt(split[2]));
				} catch (NumberFormatException _) {
					color = Color.BLACK;
				}
			} else {
				color = Color.BLACK;
			}
		}
		return color;
	}

	@Override
	public void onShutdown() {
		lightingService.shutdown();
	}

	@Override
	public Optional<List<Mode>> getModes() {
		return Optional.of(Arrays.asList(new Mode() {

			@Override
			public String getName() {
				return MODE_LOGILED;
			}

			@Override
			public void onEnable() {
				onCommand(settings.getString(LogiledViewFactory.SETTING_LOGILED_COLOR));
			}

			@Override
			public void onCommand(String command, String... args) {
				var commandArgs = String.format("%s %s", command, StringUtils.join(args, " ")).trim();
				var color = getColor(commandArgs);
				settings.persist(LogiledViewFactory.SETTING_LOGILED_COLOR, colorToHex(color));
				if (!settings.getStringSet(SETTING_MODES).contains("alert")) {
					eventManager.echo("Lighting changed");
					lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
					eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
				} else {
					eventManager.echo("Alert mode active");
				}
			}

			@Override
			public void onDisable() {
				lightingService.shutdown();
				eventManager.post(new CcEvent(EVENT_LIGHTING_CHANGED));
				eventManager.echo("Disabled lighting control");
			}

			@Override
			public boolean requiresInitialization() {
				return false;
			}
		}));
	}

	private static String colorToHex(Color color) {
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

}
