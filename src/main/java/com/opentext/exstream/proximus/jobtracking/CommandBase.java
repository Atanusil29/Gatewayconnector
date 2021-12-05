package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.common.FileLock;
import com.opentext.exstream.proximus.common.PropertyReader;
import com.opentext.exstream.proximus.common.SystemProperties;
import com.opentext.exstream.proximus.server.ExstreamSession;

public class CommandBase {

	private static CustomLogger logger = new CustomLogger(CommandBase.class);

	protected static final String SERVICE_NAME = "Gateway Job Tracker";

	private static final String SDF_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String FILE_ENCODING = "ISO-8859-1";

	private static final String JAVA_PROPERTIES_FILE = "java.properties";

	protected JobTrackerConfiguration jobTrackerConfig;
	protected JobTrackerList jobTrackerList;
	protected ExstreamSession exstreamSession;

	static {
		logVersion();
	}

	private static void logVersion() {
		Package pkg = CommandBase.class.getPackage();
		String version = (pkg != null ? pkg.getImplementationVersion() : "");
		logger.info("{} (version {})", SERVICE_NAME, version);
	}
	
	protected void setSession(ExstreamSession exstreamSession){
		this.exstreamSession = exstreamSession;
	}

	protected void initialize() throws Exception {
		Properties javaProperties = PropertyReader.loadPropertiesFileFromClasspath(JAVA_PROPERTIES_FILE);
		SystemProperties.loadSystemProperties(javaProperties);

		jobTrackerConfig = new JobTrackerConfiguration();
		jobTrackerList = new JobTrackerList(jobTrackerConfig.getJobTrackerFolder());
		
		if (!FileLock.lock(jobTrackerConfig.getJobTrackerFolder(), jobTrackerConfig.fileLockType)) {
			logger.error("Unable to acquire file lock. Other commands may be running");
			System.exit(-2);
		}
		
		exstreamSession = new ExstreamSession(jobTrackerConfig.getOtdsMtaUrl(), jobTrackerConfig.getOtdsTenantUrl(),
				jobTrackerConfig.getSgwUrl(), jobTrackerConfig.getSgwUsername(), jobTrackerConfig.getSgwPassword());
	}
	
	protected void initializeWithoutLock() throws Exception {
		jobTrackerConfig = new JobTrackerConfiguration();
		jobTrackerList = new JobTrackerList(jobTrackerConfig.getJobTrackerFolder());
			
		exstreamSession = new ExstreamSession(jobTrackerConfig.getOtdsMtaUrl(), jobTrackerConfig.getOtdsTenantUrl(),
				jobTrackerConfig.getSgwUrl(), jobTrackerConfig.getSgwUsername(), jobTrackerConfig.getSgwPassword());
	}

	protected void close() throws Exception {
		FileLock.release();
	}

	protected Date readLastExecution(String lastExecutionFile) throws IOException, ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(SDF_TIMESTAMP);
		File file = new File(jobTrackerConfig.getJobTrackerFolder(), lastExecutionFile);
		if (file.isFile()) {
			String date = FileUtils.readFileToString(file, FILE_ENCODING);
			return sdf.parse(date);
		} else {
			logger.info("Last execution file does not exist: {}", lastExecutionFile);
			Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
			logger.info("Setting last execution to: {}", sdf.format(date));
			return date;
		}
	}

	protected void writeLastExecution(String lastExecutionFile, Date date) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat(SDF_TIMESTAMP);
		String data = sdf.format(date);
		File file = new File(jobTrackerConfig.getJobTrackerFolder(), lastExecutionFile);
		FileUtils.writeStringToFile(file, data, FILE_ENCODING, false);
	}

}
