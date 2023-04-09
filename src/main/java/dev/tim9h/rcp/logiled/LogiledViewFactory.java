package dev.tim9h.rcp.logiled;

import java.util.Map;

import com.google.inject.Inject;

import dev.tim9h.rcp.spi.CCard;
import dev.tim9h.rcp.spi.CCardFactory;

public class LogiledViewFactory implements CCardFactory {

	static final String SETTING_LOGILED_COLOR = "logiled.lighting.color";

	@Inject
	private LogiledView view;

	@Override
	public String getId() {
		return "logiled";
	}

	@Override
	public CCard createCCard() {
		return view;
	}

	@Override
	public Map<String, String> getSettingsContributions() {
		return Map.of(SETTING_LOGILED_COLOR, "cyan");
	}

}
