package dev.tim9h.rcp.logiled.util;

public record NamedColor(String name, int red, int green, int blue) {

	public int computeMSE(int pixR, int pixG, int pixB) {
		return ((pixR - red()) * (pixR - red()) + (pixG - green()) * (pixG - green())
				+ (pixB - blue()) * (pixB - blue())) / 3;
	}

}
