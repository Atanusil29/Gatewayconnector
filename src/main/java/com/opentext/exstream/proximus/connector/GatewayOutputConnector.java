package com.opentext.exstream.proximus.connector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.validator.routines.EmailValidator;

import com.opentext.exstream.proximus.actions.SendMessageAction;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.common.PropertyReader;
import com.opentext.exstream.proximus.common.SystemProperties;
import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.struct.BlackoutPeriodType;
import com.opentext.exstream.proximus.struct.MessageFileType;
import com.opentext.exstream.proximus.struct.MetaDataType;
import com.opentext.exstream.proximus.struct.Notification;
import com.opentext.exstream.proximus.struct.RecipientType;
import com.opentext.exstream.proximus.struct.SendMessageType;
import com.opentext.exstream.proximus.struct.TagType;
import com.opentext.exstream.proximus.struct.ToRecipientType;

import streamserve.connector.StrsConfigVals;
import streamserve.connector.StrsConnectable;
import streamserve.constants.MetaDataConstants;
import streamserve.context.Context;
import streamserve.context.ContextFactory;
import streamserve.context.MetaData;
import streamserve.context.MetaDataCollection;

public class GatewayOutputConnector implements StrsConnectable {

	private static final String SERVICE_NAME = "Gateway Output Connector";
	private static CustomLogger logger = new CustomLogger(GatewayOutputConnector.class);
	//private static boolean initialisedStatic = false;
	//private boolean initialisedInstance = false;

	private static final String PROC_JOB_ID_UID = "1EB03803-1E96-4F4E-BD56-26A9BCE32415";

	// Configuration files
	private static final String JAVA_PROPERTIES_FILE = "java.properties";
	private static final String GATEWAY_CONFIG_FILE = "gateway.properties";
	private static final String FILE_ENCODING = "ISO-8859-1";
	
	// Properties coming from configuration file on disk
	private static Properties properties;
	private static String notificationPath;
	private static File messagePath;

	// Session to interface with Gateway
	private static GatewaySession session;

	// Properties in O and OR connector files
	protected static final String PROPERTY_CLIENT_NAME = "Client name";
	protected static final String PROPERTY_CHANNEL_NAME = "Channel name";
	protected static final String PROPERTY_CONSUMER_NAME = "Consumer name";

	protected static final String PROPERTY_BODY_PATHS = "Body paths";
	protected static final String PROPERTY_BODY_ENCODING = "Body encoding";
	protected static final String PROPERTY_BODY_LANGUAGE = "Body language";
	protected static final String PROPERTY_BODY_FORCE_UTF8 = "Body UTF-16 to UTF-8";
	protected static final String PROPERTY_ATTACHMENT_PATHS = "Attachment paths";
	protected static final String PROPERTY_ATTACHMENT_ALIAS = "Attachment alias";
	protected static final String PROPERTY_ATTACHMENT_ENCODING = "Attachment encoding";
	protected static final String PROPERTY_ATTACHMENT_LANGUAGE = "Attachment language";

	protected static final String PROPERTY_FROM_SENDER = "From";
	protected static final String PROPERTY_FROM_ALIAS = "From alias";
	protected static final String PROPERTY_REPLY_TO = "Reply-To";
	protected static final String PROPERTY_TO_RECIPIENTS = "To";
	protected static final String PROPERTY_TO_RECIPIENTS_KIND = "To kind";
	protected static final String PROPERTY_TO_RECIPIENTS_REFERENCE = "To reference";
	protected static final String PROPERTY_CC_RECIPIENTS = "Cc";
	protected static final String PROPERTY_BCC_RECIPIENTS = "Bcc";
	protected static final String PROPERTY_SUBJECT = "Subject";

	protected static final String PROPERTY_PRIORITY = "Priority";
	protected static final String PROPERTY_TIME_TO_LIVE = "Time to live";
	protected static final String PROPERTY_ACCEPT_REPLY = "Accept reply";
	protected static final String PROPERTY_BLACKOUT_START = "Blackout period start hour";
	protected static final String PROPERTY_BLACKOUT_END = "Blackout period end hour";
	protected static final String PROPERTY_CALENDAR_NAME = "Blackout period calendar name";
	protected static final String PROPERTY_TRACKING = "Tracking on";
	protected static final String PROPERTY_MONITORING = "Monitoring on";
	protected static final String PROPERTY_TAGS = "Tags";
	protected static final String PROPERTY_SHORTENING = "Shortening on";

	protected static final String PROPERTY_EXTERNAL_JOB_COMPLETION = "External Job Completion";
	protected static final String PROPERTY_PARTID_TAG_NAME = "PartID Tag Name";

	//notification step values
	private static final String NOTIFY_SENT = "SENT";
	private static final String NOTIFY_UNSENT = "UNSENT";
	private static final String NOTIFY_NUM = "04";

	// Actual value of properties coming from platform/runtime configuration
	private String clientName;
	private String channelName;
	private String consumerName;
	private String EventTime;

	private List<String> bodyPaths = new ArrayList<String>();
	private List<String> bodyEncoding = new ArrayList<String>();
	private List<String> bodyLanguage = new ArrayList<String>();
	private boolean bodyForceUtf8 = true;

	private List<String> attachmentPaths = new ArrayList<String>();
	private List<String> attachmentAlias = new ArrayList<String>();
	private List<String> attachmentEncoding = new ArrayList<String>();
	private List<String> attachmentLanguage = new ArrayList<String>();

	private String fromSender;
	private String fromAlias;
	private String replyTo;
	private List<String> toRecipients = new ArrayList<String>();
	private List<String> toRecipientsKind = new ArrayList<String>();
	private List<String> toRecipientsReference = new ArrayList<String>();
	private List<String> ccRecipients = new ArrayList<String>();
	private List<String> bccRecipients = new ArrayList<String>();
	private String subject;

	private String priority;
	private Integer timeToLive;
	private Boolean acceptReply;
	private Integer blackoutStart;
	private Integer blackoutEnd;
	private String blackoutCalendarName;
	private Boolean tracking;
	private Boolean monitoring;
	private Map<String, String> tags = new HashMap<String, String>();
	private Boolean shortening;

	private boolean externalJobCompletion = false;
	private String partIdTagName;

	// Internal variables
	private String jobId;
	private String customerId;
	private ByteArrayOutputStream outputStream;

	static {
		try {
			logVersion();

			Properties javaProperties = loadProperties(JAVA_PROPERTIES_FILE);
			SystemProperties.loadSystemProperties(javaProperties);

			properties = loadProperties(GATEWAY_CONFIG_FILE);

			notificationPath = properties.getProperty("notification.folder");
			if (notificationPath != null && !notificationPath.equals("")){
				File p = new File(notificationPath);
				p.mkdirs();
			}
			
			messagePath = null;
			String tmp = properties.getProperty("message.folder");
			if (tmp != null && !tmp.isEmpty()){
				messagePath = new File(tmp);
				messagePath.mkdirs();
			}
			
			initializeSession();
		}
		catch (IOException e) {
			logger.error("ERROR: Unable to initialize: " + ExceptionUtils.getRootCauseMessage(e));
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e));
		}
	}

	private static Properties loadProperties(String file) throws IOException {
		logger.info("Loading properties file '{}' from classpath", file);
		InputStream is = GatewayOutputConnector.class.getClassLoader().getResourceAsStream(file);
		if (is == null) {
			throw new FileNotFoundException("Could not find file: " + file);
		}
		String text = IOUtils.toString(is, FILE_ENCODING);
		return PropertyReader.loadProperties(text);
	}

	private static void logVersion() {
		Package pkg = GatewayOutputConnector.class.getPackage();
		String version = (pkg != null ? pkg.getImplementationVersion() : "");
		logger.info("{} (version {})", SERVICE_NAME, version);
	}

	private static void initializeSession() {
		logger.info("Initializing Gateway session object");

		String url = properties.getProperty("gateway.url");
		String username = properties.getProperty("gateway.username");
		String password = properties.getProperty("gateway.password");
		session = new GatewaySession(url, username, password);

		int maxRetries = Integer.parseInt(properties.getProperty("max.retries", "5"));
		session.setMaxRetries(maxRetries);

		int sleepInterval = Integer.parseInt(properties.getProperty("sleep.interval", "1000"));
		session.setSleepInterval(sleepInterval);

		double sleepFactor = Double.parseDouble(properties.getProperty("sleep.factor", "2.0"));
		session.setSleepFactor(sleepFactor);
	}

	/**
	 * Initialises the Connector by processing the StrsConfigVals passed from Communication Server
	 * 
	 * @param configVals Configuration values defined in Communication Builder
	 * @throws RemoteException 
	 */
	private void initialise(StrsConfigVals configVals) throws RemoteException {
		/*
		if (!initialisedStatic) {
			logger.setService(configVals);
			logVersion();
		}
		logger.trace("Initialised variables: static={} instance={}", initialisedStatic, initialisedInstance);
		initialisedStatic = true;
		initialisedInstance = true;
		 */
		//logger.setService(configVals);

		readConfigurationValues(configVals);
		validateConfiguration();
	}

	private void readConfigurationValues(StrsConfigVals configVals) throws RemoteException {
		// All configuration properties are always coming as parameters
		// But sometimes they are empty, and we must use earlier values (i.e. from open)

		// Job ID doesn't match to ID displayed in Supervisor
		jobId = configVals.getSystemValue("jobid");

		// But we cannot get parent job ID through metadata
		//jobId = getMetadata(MetaDataConstants.MD_PROPERTY_PARENTJOBID);

		customerId = getMetadata(MetaDataConstants.MD_PROPERTY_CUSTOMERINRUN);

		String tmp;

		tmp = configVals.getValue(PROPERTY_CLIENT_NAME);
		if (tmp != null && tmp.length() > 0) {
			clientName = tmp;
		}

		tmp = configVals.getValue(PROPERTY_CHANNEL_NAME);
		if (tmp != null && tmp.length() > 0) {
			channelName = tmp;
		}

		tmp = configVals.getValue(PROPERTY_CONSUMER_NAME);
		if (tmp != null && tmp.length() > 0) {
			consumerName = tmp;
		}

		tmp = configVals.getValue(PROPERTY_BODY_PATHS);
		if (tmp != null && tmp.length() > 0) {
			bodyPaths.clear();
			for (String path : tmp.split(";")) {
				bodyPaths.add(path.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_BODY_ENCODING);
		if (tmp != null && tmp.length() > 0) {
			bodyEncoding.clear();
			for (String path : tmp.split(";")) {
				bodyEncoding.add(path.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_BODY_LANGUAGE);
		if (tmp != null && tmp.length() > 0) {
			bodyLanguage.clear();
			for (String path : tmp.split(";")) {
				bodyLanguage.add(path.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_BODY_FORCE_UTF8);
		if (tmp != null && tmp.length() > 0) {
			bodyForceUtf8 = Boolean.parseBoolean(tmp);
		}

		tmp = configVals.getValue(PROPERTY_ATTACHMENT_PATHS);
		if (tmp != null && tmp.length() > 0) {
			attachmentPaths.clear();
			for (String path : tmp.split(";")) {
				attachmentPaths.add(path.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_ATTACHMENT_ALIAS);
		if (tmp != null && tmp.length() > 0) {
			attachmentAlias.clear();
			for (String name : tmp.split(";")) {
				attachmentAlias.add(name.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_ATTACHMENT_ENCODING);
		if (tmp != null && tmp.length() > 0) {
			attachmentEncoding.clear();
			for (String name : tmp.split(";")) {
				attachmentEncoding.add(name.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_ATTACHMENT_LANGUAGE);
		if (tmp != null && tmp.length() > 0) {
			attachmentLanguage.clear();
			for (String name : tmp.split(";")) {
				attachmentLanguage.add(name.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_FROM_SENDER);
		if (tmp != null && tmp.length() > 0) {
			fromSender = tmp;
		}
		
		tmp = configVals.getValue(PROPERTY_FROM_ALIAS);
		if (tmp != null && tmp.length() > 0) {
			fromAlias = tmp;
		}

		tmp = configVals.getValue(PROPERTY_REPLY_TO);
		if (tmp != null && tmp.length() > 0) {
			replyTo = tmp;
		}

		tmp = configVals.getValue(PROPERTY_TO_RECIPIENTS);
		if (tmp != null && tmp.length() > 0) {
			toRecipients.clear();
			for (String recipient : tmp.split(";")) {
				toRecipients.add(recipient.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_TO_RECIPIENTS_KIND);
		if (tmp != null && tmp.length() > 0) {
			toRecipientsKind.clear();
			for (String recipient : tmp.split(";")) {
				toRecipientsKind.add(recipient.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_TO_RECIPIENTS_REFERENCE);
		if (tmp != null && tmp.length() > 0) {
			toRecipientsReference.clear();
			for (String recipient : tmp.split(";")) {
				toRecipientsReference.add(recipient.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_CC_RECIPIENTS);
		if (tmp != null && tmp.length() > 0) {
			ccRecipients.clear();
			for (String recipient : tmp.split(";")) {
				ccRecipients.add(recipient.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_BCC_RECIPIENTS);
		if (tmp != null && tmp.length() > 0) {
			bccRecipients.clear();
			for (String recipient : tmp.split(";")) {
				bccRecipients.add(recipient.trim());
			}
		}

		tmp = configVals.getValue(PROPERTY_SUBJECT);
		if (tmp != null && tmp.length() > 0) {
			subject = tmp;
		}

		tmp = configVals.getValue(PROPERTY_PRIORITY);
		if (tmp != null && tmp.length() > 0) {
			priority = tmp;
		}

		tmp = configVals.getValue(PROPERTY_TIME_TO_LIVE);
		if (tmp != null && tmp.length() > 0) {
			timeToLive = Integer.parseInt(tmp);
		}

		tmp = configVals.getValue(PROPERTY_ACCEPT_REPLY);
		if (tmp != null && tmp.length() > 0) {
			acceptReply = Boolean.parseBoolean(tmp);
		}

		tmp = configVals.getValue(PROPERTY_BLACKOUT_START);
		if (tmp != null && tmp.length() > 0) {
			blackoutStart = Integer.parseInt(tmp);
		}

		tmp = configVals.getValue(PROPERTY_BLACKOUT_END);
		if (tmp != null && tmp.length() > 0) {
			blackoutEnd = Integer.parseInt(tmp);
		}
		
		tmp = configVals.getValue(PROPERTY_CALENDAR_NAME);
		if (tmp != null && tmp.length() > 0) {
			blackoutCalendarName = tmp;
		}

		tmp = configVals.getValue(PROPERTY_TRACKING);
		if (tmp != null && tmp.length() > 0) {
			tracking = Boolean.parseBoolean(tmp);
		}
		
		tmp = configVals.getValue(PROPERTY_MONITORING);
		if (tmp != null && tmp.length() > 0) {
			monitoring = Boolean.parseBoolean(tmp);
		}

		tmp = configVals.getValue(PROPERTY_TAGS);
		if (tmp != null && tmp.length() > 0) {
			tags.clear();
			for (String pair : tmp.split(";")) {
				String[] split = pair.split("=");
				if (split.length == 2) {
					tags.put(split[0], split[1]);
				} else {
					logger.error("ERROR: Invalid name-value pair: {}", pair);
				}
			}
		}

		tmp = configVals.getValue(PROPERTY_SHORTENING);
		if (tmp != null && tmp.length() > 0) {
			shortening = Boolean.parseBoolean(tmp);
		}

		tmp = configVals.getValue(PROPERTY_EXTERNAL_JOB_COMPLETION);
		if (tmp != null && tmp.length() > 0 && tmp.equalsIgnoreCase("true")) {
			externalJobCompletion = true;
		}

		tmp = configVals.getValue(PROPERTY_PARTID_TAG_NAME);
		if (tmp != null && tmp.length() > 0) {
			partIdTagName = tmp;
		}
	}

	private String getMetadata(String uuid) {
		StringBuilder result = new StringBuilder();
		Context context = null;
		try {
			context = ContextFactory.acquireContext(ContextFactory.OutputConnectorContextType);
			MetaDataCollection metaDataCollectionInterface = context.getInterface(MetaDataCollection.class);
			MetaData currentMetaData = metaDataCollectionInterface.getMetaData(uuid);
			if (null != currentMetaData.getValues()) {
				assert null != currentMetaData.getType();
				assert null != currentMetaData.getValues()[0];
				result.append(currentMetaData.getType().cast(currentMetaData.getValues()[0]));
			}
			return result.toString();
		}
        catch(UnsatisfiedLinkError e){
            //Exception most likely due to Native Library not being loaded.
            //should occur in Unit Test only
			logger.error("ERROR: Unable to get metadata: {}", ExceptionUtils.getRootCauseMessage(e));
        }
        catch(NoClassDefFoundError e){
            //Exception most likely due to Native Library not being loaded.
            //should occur in Unit Test only
			logger.error("ERROR: Unable to get metadata: {}", ExceptionUtils.getRootCauseMessage(e));
        }
		catch (Exception e) {
			logger.error("ERROR: Unable to get metadata: {}", ExceptionUtils.getRootCauseMessage(e));
		}
		// XXX - Temporary removal (TB) - I think this may be causing application to crash in CommServer
		/* 
		finally {
			if (null != context) {
				ContextFactory.releaseContext(context);
			}
		}
		 */
		return null;
	}

	private void validateConfiguration() {
		for (String path : bodyPaths) {
			validateFilePath(path);
		}

		for (String path : attachmentPaths) {
			validateFilePath(path);
		}

		if ("EMAIL".equals(channelName)) {
			validateEmail(fromSender);
			validateEmail(replyTo);

			for (String email : toRecipients) {
				validateEmail(email);
			}

			for (String email : ccRecipients) {
				validateEmail(email);
			}

			for (String email : bccRecipients) {
				validateEmail(email);
			}
		}
	}

	private void validateFilePath(String path) {
		if (path != null && path.length() > 0) {
			File file = new File(path);
			if (file.isAbsolute() && !file.isFile()) {
				logger.error("WARN: File doesn't seem to exist: {}", path);
			}
		}
	}

	private void validateEmail(String email) {
		if (email != null && email.length() > 0 && !EmailValidator.getInstance().isValid(email)) {
			logger.warn("WARN: Email doesn't seem to be valid: {}", email);
		}
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method directly after the connector has been created.
	 * Use this method to initialize resources according to the connector properties set in Design Center.
	 * The properties are passed in the ConfigVals object and can be accessed with getValue method.
	 *  
	 * @param configVals Configuration values from Communication Server
	 * @throws RemoteException
	 */
	public boolean strsoStartJob(StrsConfigVals configVals) throws RemoteException {
		initialise(configVals);
		logger.debug("strsoStartJob called");
		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method when all data has been delivered by the output connector and before
	 * the connector is removed. Use this method to release the resources used by the connector.
	 * 
	 * @throws RemoteException
	 */
	public boolean strsoEndJob() throws RemoteException {
		logger.debug("strsoEndJob called");
		outputStream = null;
		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method each time it starts processing output data.
	 * can be used to initialize resources according to connector properties set in Design Center.
	 * The properties are passed in the ConfigVals object and can be accessed with getValue method.
	 * 
	 * @param configVals Configuration values from Communication Server
	 * @throws RemoteException
	 */
	public boolean strsoOpen(StrsConfigVals configVals) throws RemoteException {
		initialise(configVals);
		logger.debug("strsoOpen called");

		if (outputStream == null) {
			outputStream = new ByteArrayOutputStream();
		}
		outputStream.reset();

		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * This method is called between a pair of strsoOpen() and strsoClose() calls. It can be called several times or only once,
	 * depending on the amount of data to be written. Each strsoWrite() call provides buffered output data.
	 *  
	 * @param bytes The bytes sent to the connector
	 * @throws RemoteException
	 */
	public boolean strsoWrite(byte[] bytes) throws RemoteException {
		logger.debug("strsoWrite called ({} bytes)", bytes.length);

		try {
			if (bytes != null) {
				outputStream.write(bytes);
			}
		}
		catch (IOException e) {
			logger.error("ERROR: Unable to write bytes: {}", ExceptionUtils.getRootCauseMessage(e));
			return false;
		}

		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method at the end of the Process, Document or Job. 
	 * use this method to performed the final delivery.
	 * If the connector supports runtime properties, these are passed in the ConfigVals object. 
	 * 
	 * @throws RemoteException
	 */
	public boolean strsoClose(StrsConfigVals configVals) throws RemoteException {
		initialise(configVals);
		logger.debug("strsoClose called");

		try {
			outputStream.close();
			logger.debug("Output file is completed ({} bytes)", outputStream.size());

			SendMessageType message = createMessage(outputStream.toByteArray());
			addAttachmentsFromParams(message);
			sendMessage(message);
			sendNotification(NOTIFY_SENT, "Message sent to CDCG", NOTIFY_NUM);
		}
		catch (Exception e) {
			logger.error("ERROR: Unable to send message: {}", ExceptionUtils.getRootCauseMessage(e));
			logger.error(ExceptionUtils.getFullStackTrace(e));
			sendNotification(NOTIFY_UNSENT, ExceptionUtils.getRootCauseMessage(e), NOTIFY_NUM);
			return false;
		}

		return true;
	}

	private SendMessageType createMessage(byte[] content) throws Exception {
		SendMessageType message = new SendMessageType();
		message.setClientName(clientName);
		message.setChannelName(channelName);

		for (int i = 0; i < bodyPaths.size(); i++) {
			byte[] loopContent = i == 0 ? content : null;

			String encoding = null;
			if (i < bodyEncoding.size()) {
				encoding = bodyEncoding.get(i);
			} else if (bodyEncoding.size() == 1) {
				encoding = bodyEncoding.get(0);
			}

			String language = null;
			if (i < bodyLanguage.size()) {
				language = bodyLanguage.get(i);
			} else if (bodyLanguage.size() == 1) {
				language = bodyLanguage.get(0);
			}

			MessageFileType body = MessageFileUtil.createMessageFile(bodyPaths.get(i), "BODY", loopContent, null, encoding, language, bodyForceUtf8);
			addMessageFile(message, body);
		}

		MetaDataType metaData = createMetaData();
		message.setMetaData(metaData);

		//add part id, even if not external completion
		addPartId(message);

		return message;
	}

	private void addPartId(SendMessageType message) throws Exception {
		if (partIdTagName == null || partIdTagName.length() == 0) {
			if (externalJobCompletion)
				throw new Exception("PartID tag name must be populated");
			else{
				logger.debug("PartID tag not defined");
				return;
			}
		}

		String value = getMetadata(PROC_JOB_ID_UID);
		if (value == null) {
			if (externalJobCompletion)
				throw new Exception("Unable to get PartID to correlate event to document");
			else{
				logger.error("Unable to get PartID to correlate event to document");
				return;
			}
		}

		int tags = message.getMetaData().getTag().length;
		TagType[] tagArray = new TagType[tags + 1];
		System.arraycopy(message.getMetaData().getTag(), 0, tagArray, 0, tags);
		tagArray[tags] = new TagType();
		tagArray[tags].setName(partIdTagName);
		tagArray[tags].setValue(value);
		message.getMetaData().setTag(tagArray);
	}

	private void addMessageFile(SendMessageType message, MessageFileType messageFile) {
		int newIndex;
		MessageFileType[] messageFiles;
		if (message.getMessageFile() == null) {
			messageFiles = new MessageFileType[1];
			newIndex = 0;
		} else {
			messageFiles = new MessageFileType[message.getMessageFile().length + 1];
			System.arraycopy(message.getMessageFile(), 0, messageFiles, 0, message.getMessageFile().length);
			newIndex = message.getMessageFile().length;
		}
		messageFiles[newIndex] = messageFile;
		message.setMessageFile(messageFiles);
	}

	private void addAttachmentsFromParams(SendMessageType message) throws Exception {
		int i = 0;
		for (String path : attachmentPaths) {
			String alias = null;
			if (attachmentAlias.size() == attachmentPaths.size()) {
				alias = attachmentAlias.get(i);
			} else {
				String extension = FilenameUtils.getExtension(path);
				if (extension.length() > 0) {
					extension = "." + extension;
				}
				alias = "attachment-" + (i + 1) + extension;
			}

			String encoding = null;
			if (attachmentEncoding.size() == attachmentPaths.size()) {
				encoding = attachmentEncoding.get(i);
			} else if (attachmentEncoding.size() == 1) {
				encoding = attachmentEncoding.get(0);
			}

			String language = null;
			if (attachmentLanguage.size() == attachmentPaths.size()) {
				language = attachmentLanguage.get(i);
			} else if (attachmentEncoding.size() == 1) {
				language = attachmentLanguage.get(0);
			}

			logger.debug("Adding attachment '{}' with alias '{}'", path, alias);

			MessageFileType attachment = MessageFileUtil.createMessageFile(path, "ATTACHMENT", null, alias, encoding, language);
			addMessageFile(message, attachment);

			i++;
		}
	}

	private MetaDataType createMetaData() {
		MetaDataType metaData = new MetaDataType();
		metaData.setFromSender(fromSender);
		metaData.setReplyTo(replyTo);
		metaData.setFromAlais(fromAlias);

		ToRecipientType[] toRecipient = new ToRecipientType[toRecipients.size()];
		for (int i = 0; i < toRecipients.size(); i++) {
			toRecipient[i] = new ToRecipientType();
			toRecipient[i].setContact(toRecipients.get(i));
			toRecipient[i].setPrimary(i == 0);
		}
		if (toRecipientsKind.size() == toRecipients.size()) {
			for (int i = 0; i < toRecipients.size(); i++) {
				toRecipient[i].setKind(toRecipientsKind.get(i));
			}
		} else if (toRecipientsKind.size() > 0) {
			logger.warn("Number of entries in 'To kind' seems incorrect");
		}
		if (toRecipientsReference.size() == toRecipients.size()) {
			for (int i = 0; i < toRecipients.size(); i++) {
				toRecipient[i].setReference(toRecipientsReference.get(i));
			}
		} else if (toRecipientsReference.size() > 0) {
			logger.warn("Number of entries in 'To reference' seems incorrect");
		}
		metaData.setToRecipient(toRecipient);

		RecipientType[] ccRecipient = new RecipientType[ccRecipients.size()];
		for (int i = 0; i < ccRecipients.size(); i++) {
			ccRecipient[i] = new RecipientType();
			ccRecipient[i].setContact(ccRecipients.get(i));
		}
		metaData.setCcRecipient(ccRecipient);

		RecipientType[] bccRecipient = new RecipientType[bccRecipients.size()];
		for (int i = 0; i < bccRecipients.size(); i++) {
			bccRecipient[i] = new RecipientType();
			bccRecipient[i].setContact(bccRecipients.get(i));
		}
		metaData.setBccRecipient(bccRecipient);

		if (subject != null) metaData.setSubject(subject);
		if (priority != null) metaData.setPriority(priority);
		if (timeToLive != null) metaData.setTimeToLive(timeToLive);
		if (acceptReply != null) metaData.setAcceptReply(acceptReply);

		if ((blackoutStart != null && blackoutEnd != null) || blackoutCalendarName != null ){
			BlackoutPeriodType blackoutPeriod = new BlackoutPeriodType();
					blackoutPeriod.setStartHour(blackoutStart);
					blackoutPeriod.setEndHour(blackoutEnd);
					blackoutPeriod.setCalendarName(blackoutCalendarName);
			metaData.setBlackoutPeriod(blackoutPeriod);
		}

		if (tracking != null) metaData.setTrackingOn(tracking);
		if (monitoring != null) metaData.setMonitoringOn(monitoring);

		TagType[] tagArray = new TagType[tags.keySet().size()];
		int i = 0;
		for (Map.Entry<String, String> set : tags.entrySet()) {
			tagArray[i] = new TagType();
			tagArray[i].setName(set.getKey());
			tagArray[i].setValue(set.getValue());
			i++;
		}
		metaData.setTag(tagArray);

		if (shortening != null) metaData.setShorteningOn(shortening);
		
		return metaData;
	}

	private void sendMessage(SendMessageType message) throws Exception {

		if (session != null) {
			logger.info("Job {}, Customer {} - Sending message to Gateway...", jobId, customerId);
			long startTime = System.currentTimeMillis();

			try{
				EventTime = SendMessageAction.sendMessage(session, message);
				logMessage(message, "success");
			}catch(Exception e){
				logMessage(message, "failed");
				throw e;
			}			

			long endTime = System.currentTimeMillis();
			String result = String.format("Send message completed (%.1f secs)", (endTime - startTime) / 1000.0f);
			logger.info("Job {}, Customer {} - {}", jobId, customerId, result);
		} else {
			logger.error("ERROR: Unable to send message due to missing session");
			logMessage(message, "error");
		}
	}

	private void logMessage(SendMessageType message, String status) {
		if (messagePath == null || messagePath.equals("")){
			if (logger.isDebugEnabled()) logger.debug(message.toPrettyString());
			return;
		}

		File consumerPath = messagePath;
		if (consumerName != null && !consumerName.isEmpty()){
			consumerPath = new File(messagePath,consumerName);
			consumerPath.mkdirs();
		}

		String dateformat = "yyyyMMdd_HHmmssSSS";
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		String datetime = sdf.format(new Date());

		String filename = "sendmessage_" + tags.get("requestId") + "_" + tags.get("sequenceNr") + "_" + channelName + "_" + status + "_" + datetime + ".json";

		logger.debug("writing message to file: {}", filename);
		File outputFile = new File(consumerPath, filename);
		try{
			message.writeValue(outputFile);
		}catch(Exception e){
			logger.error("ERROR: Unable to write message to file: {}", ExceptionUtils.getRootCauseMessage(e));
		}

	}

	private void sendNotification(String step, String qualifier, String stepNum) {
		logger.debug("preparing to notification - {}", step);
		Notification notification = generateNotification(step, qualifier);

		String dateformat = "yyyyMMdd_HHmmss";
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		String datetime = sdf.format(new Date());
		
		String filename = "notificationxml_input_file_" +stepNum + "_" + step + "_" + tags.get("requestId") + "_" + tags.get("sequenceNr") + "_" + datetime + ".xml";

		logger.debug("preparing to write output notification: {}", filename);
		File outputFile = new File(notificationPath, filename);
		try{
			notification.marshal(outputFile);
		}catch(Exception e){
			logger.error("ERROR: Unable to send notification: {}", ExceptionUtils.getRootCauseMessage(e));
		}

	}
	
	private Notification generateNotification(String step, String qualifier){
		String dateformat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String datetime = sdf.format(new Date());

		Notification notification = new Notification();
		notification.setUUID(tags.get("requestId"));
		notification.setEventTime(EventTime);
		notification.setWorkflowID(tags.get("workflowId"));
		notification.setTrackerid(tags.get("trackerId"));
		notification.setStep(step);
		notification.setStepTime(datetime);
		notification.setQualifier(qualifier);
		notification.setChannelName(channelName);
		notification.setConsumer_Name((consumerName!=null && !"".equals(consumerName) ? consumerName : "CVM"));  //default to CVM if not defined
		notification.setEvent_Type("Message");
		notification.setSquenceNumber(tags.get("sequenceNr"));
		return notification;
	}


}
