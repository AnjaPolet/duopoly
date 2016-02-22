package eu.atbk.duopoly.java7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import static eu.atbk.duopoly.logging.Constants.METHOD;

public class HelloMain {

	/**
	 * Always initialize the logger as private static final
	 */
	private static final Logger log = LoggerFactory.getLogger(HelloMain.class);

	public static void main(String[] args) {
		// start with binding the method name
		// so we can easiliy corelate logging statements
		// use a "try with resources" so we don't have to bother
		// with closing the MCD entry
		try (MDCCloseable mdc = MDC.putCloseable(METHOD, "main")) {
			log.info("Hello main");
			other();
			log.info("The end");
		}
	}

	@SuppressWarnings("all")
	public static void other() {
		try (MDCCloseable mdc = MDC.putCloseable(METHOD, "other")) {
			log.info("Hello other");
			try {
				Object x = null;
				x.toString();
			} catch (NullPointerException e) {
				// always log exception (even when unexpected)
				log.error("aj", e);
			}

			// log levels are trace, debug, info, warn, error
			// in the config file src/main/resources/logback.xml
			// defining a level there will, lower level statements
			// will not be logged
			log.trace("secret");
		}
	}
}
