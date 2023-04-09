module rcp.logiled {
	exports dev.tim9h.rcp.logiled;

	requires transitive rcp.api;
	requires com.google.guice;
	requires org.apache.logging.log4j;
	requires logiled;
	requires org.apache.commons.lang3;
}