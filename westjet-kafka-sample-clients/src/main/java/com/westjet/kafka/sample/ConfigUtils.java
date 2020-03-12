package com.westjet.kafka.sample;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigUtils {
	public static final Map<String, Class<? extends FileBasedConfiguration>> ExtToFileConfig;
	
	static {
		final Map<String, Class<? extends FileBasedConfiguration>> _extToFileConfig = new HashMap<>();
		_extToFileConfig.put(".yaml", YAMLConfiguration.class);
		_extToFileConfig.put(".yml", YAMLConfiguration.class);
		_extToFileConfig.put(".json", JSONConfiguration.class);
		_extToFileConfig.put(".ini", INIConfiguration.class);
		_extToFileConfig.put(".properties", PropertiesConfiguration.class);
		_extToFileConfig.put(".prop", PropertiesConfiguration.class);
		_extToFileConfig.put(".props", PropertiesConfiguration.class);
		
		ExtToFileConfig = Collections.unmodifiableMap(_extToFileConfig);
	}

	
	public static FileBasedConfiguration newFileConfig(String file) throws IOException {
		final File filePath = Paths.get(file).toFile();
		if(filePath.isFile()) {
			return _loadFile(filePath);
		} else {
			try {
				final URL fileUrl = ConfigUtils.class.getClassLoader().getResource(file);
				return _loadFile(Paths.get(fileUrl.toURI()).toFile());
			} catch(Exception e) {
				throw new IOException("Provided config is not a file or can't be found: "+ filePath, e);
			}
		}
	}
	
	private static FileBasedConfiguration _loadFile(File file) throws IOException {
		for(String ext: ExtToFileConfig.keySet()) {
			if(file.getName().endsWith(ext)) {
				try {
					return new Configurations()
							.fileBased(ExtToFileConfig.get(ext), file);
				} catch (ConfigurationException e) {
					throw new IOException("Failed to load provided config file: "+ file, e);
				}
			}
		}
		throw new IOException("No Config handler could be located for provided config file: "+ file);
	}
	
	public static BiConsumer<String, String> print() {
		return printWithPadding(60);
	}
	public static BiConsumer<String, String> printWithFormat(String formatStr){
		return (key,value)->System.out.println(String.format(formatStr, key, value));
	}
	public static BiConsumer<String, String> printWithPadding(int padding){
		return printWithFormat("%-"+padding+"s = %s");
	}
	
	public static void printProperties(Configuration config) {
		printProperties(config, print());
	}
	public static void printProperties(Configuration config, BiConsumer<String, String> printFunc) {
		Iterator<String> keys = config.getKeys();
		while(keys.hasNext()) {
			final String key = keys.next();
			try {
				List<Object> value = config.getList(key);
				if(value.size() > 1) {
					printFunc.accept(key, value.toString());
				} else {
					printFunc.accept(key, config.getString(key));
				}
			} catch(Throwable e) {
				printFunc.accept(key, config.getString(key));
			}
		}
	}
	
	public static void printProperties(Map<String, ?> config) {
		printProperties(config, print());
	}
	public static void printProperties(Map<String, ?> config, BiConsumer<String, String> printFunc) {
		for(Map.Entry<String, ?> e: config.entrySet()) {
			if(e.getValue() != null) {
				printFunc.accept(e.getKey(), e.getValue().toString());
			} else {
				printFunc.accept(e.getKey(), "null");
			}
		}
	}
	
	public static Map<String, Object> toMap(Configuration config){
		final Map<String, Object> mapConfig = new HashMap<>();
		
		final Iterator<String> configKeys = config.getKeys();
		while(configKeys.hasNext()) {
			final String key = configKeys.next();
			mapConfig.put(key, config.getProperty(key));
		}
		
		return mapConfig;
	}
	
	/**
	 * Reads all properties starting with {prefix} from {configToParse}. 
	 * Properties that match prefix will have the prefix removed, key lowercased, and all `_` replaced with `.`
	 * The general idea is to support Docker Containers.
	 * 
	 * @param configToParse
	 * @param prefix
	 * @return
	 */
	public static Configuration envToProp(final Configuration configToParse, final String prefix) {
		final Iterator<String> envIter = configToParse.getKeys();
		final Configuration config = new BaseConfiguration();
		while(envIter.hasNext()) {
			final String key = envIter.next();
			if(key.startsWith(prefix)) {
				final String prop = key.substring(prefix.length()).toLowerCase().replaceAll("_", ".");
				config.addProperty(prop, configToParse.getProperty(key));
			}
		}
		
		return config;
	}
}
