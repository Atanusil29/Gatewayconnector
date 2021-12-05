package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opentext.exstream.proximus.actions.SgwCommunicationsActions;
import com.opentext.exstream.proximus.common.PropertyReader;
import com.opentext.exstream.proximus.common.SystemProperties;
import com.opentext.exstream.proximus.server.ExstreamSession;

public class TriggerRequest {

	private static final String JAVA_PROPERTIES_FILE = "java.properties";

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Required parameters: input-JSON-file sequence-number service-name");
			System.exit(-1);
		}

		String inputFile = args[0];
		String sequence = args[1];
		String serviceName = args[2];

		try {
			String driverFile = FileUtils.readFileToString(new File(inputFile), "UTF-8");
			driverFile = driverFile.replaceAll("!UUID!", UUID.randomUUID().toString());
			driverFile = Base64Utils.encodeToString(driverFile.getBytes("UTF-8"));

			ObjectMapper mapper = new ObjectMapper();
			ObjectNode contentNode = mapper.createObjectNode();
			contentNode.put("contentType", "text/xml");
			contentNode.put("data", driverFile);

			ObjectNode rootNode = mapper.createObjectNode();
			rootNode.set("content", contentNode);
			rootNode.put("sequence", sequence);

			String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

			Properties javaProperties = PropertyReader.loadPropertiesFileFromClasspath(JAVA_PROPERTIES_FILE);
			SystemProperties.loadSystemProperties(javaProperties);

			JobTrackerConfiguration jobTrackerConfig = new JobTrackerConfiguration();

			ExstreamSession exstreamSession = new ExstreamSession(jobTrackerConfig.getOtdsMtaUrl(),
					jobTrackerConfig.getOtdsTenantUrl(), jobTrackerConfig.getSgwUrl(),
					jobTrackerConfig.getSgwUsername(), jobTrackerConfig.getSgwPassword());

			System.out.println("Invoking Exstream...");
			String id = SgwCommunicationsActions.post(exstreamSession, serviceName, "*", "true", jsonString);
			System.out.println("Done! Tracker ID is " + id);
		} catch (Exception e) {
			System.err.println("Unable to complete request");
			e.printStackTrace();
		}
	}

}
