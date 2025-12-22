package dev.tim9h.rcp.logiled;

import java.util.Map;

import com.google.inject.Inject;

import dev.tim9h.rcp.spi.Plugin;
import dev.tim9h.rcp.spi.PluginFactory;

public class LogiledViewFactory implements PluginFactory {

	static final String SETTING_LOGILED_COLOR = "logiled.lighting.color";

	@Inject
	private LogiledView view;

	@Override
	public String getId() {
		return "logiled";
	}

	@Override
	public Plugin create() {
		return view;
	}

	@Override
	public Map<String, String> getSettingsContributions() {
		return Map.of(SETTING_LOGILED_COLOR, "cyan");
	}

}
