package com.opentext.exstream.proximus.connector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.opentext.exstream.proximus.common.CustomLogger;

import streamserve.connector.StrsConfigVals;
import streamserve.connector.StrsConnectable;
import streamserve.constants.MetaDataConstants;
import streamserve.context.Context;
import streamserve.context.ContextFactory;
import streamserve.context.MetaData;
import streamserve.context.MetaDataCollection;

public class WorkflowOutputConnector implements StrsConnectable {

	private static final String SERVICE_NAME = "Workflow Output Connector";
	private static CustomLogger logger = new CustomLogger(WorkflowOutputConnector.class);
	//private static boolean initialisedStatic = false;
	//private boolean initialisedInstance = false;

	private static final String PROC_JOB_ID_UID = "1EB03803-1E96-4F4E-BD56-26A9BCE32415";


	// Properties in O and OR connector files
	private static final String PROPERTY_EXTERNAL_JOB_COMPLETION = "External Job Completion";
	private static final String PROPERTY_WORKFLOW_PENDING_PATH = "Workflow Pending Folder";

	private boolean externalJobCompletion = false;
	private String pendingFolder;

	// Internal variables
	private String jobId;
	private String customerId;
	private ByteArrayOutputStream outputStream;

	static {
		logVersion();
	}

	private static void logVersion() {
		Package pkg = WorkflowOutputConnector.class.getPackage();
		String version = (pkg != null ? pkg.getImplementationVersion() : "");
		logger.info("{} (version {})", SERVICE_NAME, version);
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
		logger.setService(configVals);

		readConfigurationValues(configVals);
		
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
		
		tmp = configVals.getValue(PROPERTY_EXTERNAL_JOB_COMPLETION);
		if (tmp != null && tmp.length() > 0 && tmp.equalsIgnoreCase("true")) {
			externalJobCompletion = true;
		}

		tmp = configVals.getValue(PROPERTY_WORKFLOW_PENDING_PATH);
		if (tmp != null && tmp.length() > 0) {
			if (!tmp.equals(pendingFolder)){
				pendingFolder = tmp;
				try{
					File p = new File(pendingFolder);
					if(!p.exists()) p.mkdirs();
				}catch(Exception e){
					logger.error("Exception creating pending folder: {}; {} ", pendingFolder, ExceptionUtils.getRootCauseMessage(e));
					throw new RemoteException("Exception creating pending folder: " + pendingFolder, e);
				}
			}
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

			if (!externalJobCompletion){
				logger.warn("External job completion not set to true, unexpected results could occur");
			}
			
			String partid = getMetadata(PROC_JOB_ID_UID);
			if (partid == null) {
				throw new Exception("Unable to get PartID to correlate event to document");
			}
		    File file = new File(pendingFolder, partid + ".xml");
		    try{
		    	FileOutputStream fs = new FileOutputStream(file);
		    	outputStream.writeTo(fs);
		    	fs.close();
		    }
		    catch (IOException e){
		    	logger.error("Exception writing output to file: " + ExceptionUtils.getRootCauseMessage(e));
		    	return false;
		    }
			logger.info("Job {}, Customer {} - Output written to pending folder", jobId, customerId);

		}
		catch (Exception e) {
			logger.error("ERROR: Unable to process message: {}", ExceptionUtils.getRootCauseMessage(e));
			logger.error(ExceptionUtils.getFullStackTrace(e));
			return false;
		}

		return true;
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

}
