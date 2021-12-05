//*************************************************************************************************
//
// Copyright (C) 1998-2002  Exstream Software, Inc.  Lexington, KY   All Rights Reserved.
//
// No part of this document may be photocopied, reproduced, translated, or transmitted in any form
// or by any means without the prior written consent of Exstream. Any unauthorized duplication
// in whole or in part is strictly prohibited by United States federal law. Exstream will enforce
// its copyright to the full extent of the law.
//
//*************************************************************************************************

package com.opentext.exstream.proximus.common;

import java.io.*;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyReader
{
	private static final Logger log = LoggerFactory.getLogger(PropertyReader.class);

	public static Properties loadPropertiesFileFromClasspath(String configFile) throws IOException
	{
		log.info("Loading properties file '{}' from classpath", configFile);
		InputStream is = PropertyReader.class.getClassLoader().getResourceAsStream(configFile);
		if (is == null)
		{
			throw new FileNotFoundException("Could not find file: " + configFile);
		}
		String text = IOUtils.toString(is, "UTF-8");
		is.close();
		return PropertyReader.loadProperties(text);
	}

	public static Properties loadPropertiesFile(String configFile) throws IOException
	{
		FileInputStream configStream = new FileInputStream(configFile);
		byte[] bytes = new byte[configStream.available()];
		configStream.read(bytes);
		String configStr = new String(bytes);
		configStream.close();

		return PropertyReader.loadProperties(configStr);
	}

	public static Properties loadProperties(String config) throws IOException
	{
		BufferedReader reader = null;
		String         line   = null;

		Properties     properties = new Properties();

		try
		{
			reader = new BufferedReader(new StringReader(config));

			while (true)
			{
				line = reader.readLine();

				if (line == null)
				{
					break;
				}
				line = line.trim();

				if (isLineValid(line))
				{
					addLineValue(properties,line);
				}
			}
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Exception e)
				{
					// DO NOTHING
				}

				reader = null;
			}
		}
		return properties;
	}

	private static boolean isLineValid(String line){
		if (line.equals("") || line.startsWith("#") || line.startsWith("%") || line.startsWith("*") || line.startsWith("="))
			return false;
		else
			return true;
	}

	private static void addLineValue(Properties properties, String line){
		int index = line.indexOf("=");
		String key, value;

		if ((index > 0) && (index < (line.length() - 1)))
		{
			key  = line.substring(0, index);
			value = line.substring(index + 1);

			key  = key.trim();
			value = value.trim();
			
			//don't capitialise system properties contain full stops, eg javax.net.debug
			if (!key.contains("."))
				key = key.toUpperCase();

			if (!properties.containsKey(key)){
				properties.put(key, value);
			}else{
				List<String> l = getListProperty(properties, key);
				l.add(value);
			}
		}

	}
	
	@SuppressWarnings("unchecked")
	private static List<String> getListProperty(Properties properties, String key){
		if (!properties.containsKey(key)){
			return null;
		}else{
			List<String> l = null;
			Object o = properties.get(key);
			if (!(o instanceof List)){
				l = new ArrayList<String>();
				l.add(o.toString());
				properties.put(key, l);
			}else
			{
				l = (List<String>)o;
			}
			return l;
		}
	}

	public static List<String> readListProperty(Properties properties, String propertyName){
		List<String> l = getListProperty(properties, propertyName);
		if (l == null){
			l = new ArrayList<String>(); 
		}
		for (String s : l){
			if (propertyName.matches(".*[pP][aA][sS][sS][wW][oO][rR][dD].*"))
				log.info(propertyName + " = ************");
			else
				log.info(propertyName + " = " + s);
		}
		return l;
	}

	
	public static String readProperty(Properties properties, String propertyName){
		return readProperty(properties, propertyName, (String) null, (String) null);
	}

	public static String readProperty(Properties properties, String propertyName, String defaultValue){
		return readProperty(properties, propertyName, defaultValue, (String) null);
	}

	public static int readProperty(Properties properties, String propertyName, int defaultValue){
		int propertyValue = defaultValue;
		String propertyValueStr = getProperty(properties, propertyName, (String)null, (String)null);
		
		if (propertyValueStr != null){
			try{
				propertyValue = Integer.valueOf(propertyValueStr);
			}catch(NumberFormatException e){
				log.error("Error: Invalid value for "+propertyName+" ["+propertyValueStr+"]");
			}
		}

		if (propertyName.matches(".*[pP][aA][sS][sS][wW][oO][rR][dD].*"))
			log.info(propertyName + " = ************");
		else
			log.info(propertyName + " = " + propertyValue);
		return propertyValue;
	}

	public static boolean readProperty(Properties properties, String propertyName, boolean defaultValue){
		return readProperty(properties, propertyName, defaultValue, (String) null);
	}

	public static boolean readProperty(Properties properties, String propertyName, boolean defaultValue, String environmentVariableName){
		boolean propertyValue = defaultValue;
		String propertyValueStr = getProperty(properties, propertyName, (defaultValue?"T":"F"), environmentVariableName).substring(0,1);
		
		if (    propertyValueStr.equalsIgnoreCase("T") || 
				propertyValueStr.equalsIgnoreCase("Y") || 
				propertyValueStr.equalsIgnoreCase("L"))
		{
			propertyValue = true;
		}
		else if (
				propertyValueStr.equalsIgnoreCase("F") || 
				propertyValueStr.equalsIgnoreCase("N"))
		{
			propertyValue = false;
		}
		else{
			log.error("Invalid property value: " + propertyName + " = " + propertyValueStr);
		}
		log.info(propertyName + " = " + (propertyValue?"true":"false"));
		return propertyValue;
	}

	public static String readProperty(Properties properties, String propertyName, String defaultValue, String environmentVariableName){
		String propertyValue = getProperty(properties, propertyName, defaultValue, environmentVariableName);
		if (propertyName.matches(".*[pP][aA][sS][sS][wW][oO][rR][dD].*"))
			log.info(propertyName + " = ************");
		else
			log.info(propertyName + " = " + propertyValue);
		return propertyValue;
	}
	
	private static String getProperty(Properties properties, String propertyName, String defaultValue, String environmentVariableName){
		//try load value from properties
		String propertyValue = properties.getProperty(propertyName);
		
		//if the property is actually a list of property values, get the first instance instead.
		Object value = properties.get(propertyName);
		if (value instanceof List && ((List<?>) value).size() > 0){
			propertyValue = ((List<?>) value).get(0).toString();
		}

		//if failed, check to see if environment value has been specified
		if (propertyValue==null && environmentVariableName != null && !environmentVariableName.equals(""))
			propertyValue = System.getenv(environmentVariableName);

		//if failed, apply default
		if (propertyValue == null)
			propertyValue = defaultValue;

		return propertyValue;

	}

}