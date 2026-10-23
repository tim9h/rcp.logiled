package dev.tim9h.rcp.logiled;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.rcp.event.Event;
import dev.tim9h.rcp.event.EventManager;
import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.logiled.service.LightingService;
import dev.tim9h.rcp.logiled.util.ColorUtils;
import dev.tim9h.rcp.settings.Settings;
import dev.tim9h.rcp.spi.CommandBuilder;
import dev.tim9h.rcp.spi.CommandNode;
import dev.tim9h.rcp.spi.Mode;
import dev.tim9h.rcp.spi.Plugin;
import javafx.scene.paint.Color;

public class LogiledView implements Plugin {

	private static final String SETTING_LOGILED_COLOR = "logiled.lighting.color";

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
	public String getId() {
		return "logiled";
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
			restoreLighting();
		} else {
			enableAlertLighting();
		}
	}

	private void restoreLighting() {
		if (settings.getStringSet(SETTING_MODES).contains(MODE_LOGILED)) {
			var color = ColorUtils.getColor(settings.getString(SETTING_LOGILED_COLOR));
			lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
			eventManager.post(new Event(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
		} else {
			lightingService.shutdown();
			eventManager.post(new Event(EVENT_LIGHTING_CHANGED));
		}
	}

	private void enableAlertLighting() {
		var color = Color.RED;
		lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
		eventManager.post(new Event(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
	}

	private void handleAfk(Object[] args) {
		if ("off".equals(StringUtils.join(args))) {
			restoreLighting();
		} else {
			var color = Color.BLACK;
			lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
			eventManager.post(new Event(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
		}
	}

	@Override
	public void onShutdown() {
		lightingService.close();
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
				setColor(settings.getString(SETTING_LOGILED_COLOR));
			}

			@Override
			public Optional<CommandNode> getModeCommands() {
				return new CommandBuilder().command("logiled").argumentAction(LogiledView.this::setColor).build();
			}

			@Override
			public void onDisable() {
				lightingService.shutdown();
				eventManager.post(new Event(EVENT_LIGHTING_CHANGED));
				eventManager.echo("Disabled lighting control");
			}

			@Override
			public boolean requiresInitialization() {
				return false;
			}
		}));
	}

	private void setColor(String c) {
		var color = ColorUtils.getColor(c);
		var hex = ColorUtils.colorToHex(color);
		if (!hex.equalsIgnoreCase(settings.getString(SETTING_LOGILED_COLOR))) {
			settings.persist(SETTING_LOGILED_COLOR, hex);
		}
		if (!settings.getStringSet(SETTING_MODES).contains("alert")) {
			eventManager.echo("Lighting changed");
			lightingService.setLighting(color.getRed(), color.getGreen(), color.getBlue());
			eventManager.post(new Event(EVENT_LIGHTING_CHANGED, ColorUtils.getNamedColorFromColor(color)));
		} else {
			eventManager.echo("Alert mode active");
		}
	}

	@Override
	public Map<String, String> getSettingsContributions() {
		return Map.of(SETTING_LOGILED_COLOR, "cyan");
	}

}