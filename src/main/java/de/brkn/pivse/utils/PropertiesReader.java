package de.brkn.pivse.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public enum PropertiesReader {
	INSTANCE;

	private PropertiesConfiguration conf;

	private PropertiesReader() {
		try {
			conf = new PropertiesConfiguration("parameters.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String get(String parameter) {
		return conf.getString(parameter);
	}
}
