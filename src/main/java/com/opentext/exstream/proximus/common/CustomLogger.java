package com.opentext.exstream.proximus.common;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import streamserve.connector.StrsConfigVals;
import streamserve.connector.StrsServiceable;
import streamserve.context.Context;
import streamserve.context.ContextFactory;
import streamserve.context.LogLevel;
import streamserve.context.LogService;

/**
 * This is custom implementation of the slf4j Logger, which
 * contains support for standard slf4j, strs filter logservice,
 * and strs connector log service.
 * @author tbannon
 */
public class CustomLogger implements org.slf4j.Logger {
	private Logger logger = null;
	private LogService logservice = null;
	private StrsServiceable strsservice = null;
	private String name;

	public CustomLogger(Class<?> clazz){
		logger = LoggerFactory.getLogger(clazz);
		this.name = clazz.getSimpleName();
		//initialiseLogService();
	}

	public CustomLogger(String name){
		logger = LoggerFactory.getLogger(name);
		this.name = name;
		//initialiseLogService();
	}

	@Override
	public String getName() {
		return logger.getName();
	}

	//############################################################################
	//## Initialise StreamServe logging services
	//############################################################################

	public synchronized void setService(StrsServiceable strsservice) {
		if (this.strsservice == null){
			logger.trace("setting StrsService");
			this.strsservice = strsservice;
		}
	}
	
	public synchronized void setService(StrsConfigVals configVals) {
		// XXX - Temporary removal (GA) - I think this may be causing application to crash in CommServer
		//if (this.strsservice == null){
			//logger.trace("getting StrsService from config");
			this.strsservice = configVals.getStrsService();
		//}
	}

	public synchronized void setService(LogService logservice) {
		if (this.logservice == null){
			logger.trace("setting LogService");
			this.logservice = logservice;
		}
	}

	@SuppressWarnings("unused")
	private void initialiseLogService(){
		Context context = null;
		logger.trace("initialising LogService");
		try{
			context = ContextFactory.acquireContext( ContextFactory.ServiceContextType );
			logger.trace("initialising LogService - context: {}", context);
            logservice = context.getInterface( LogService.class );
			logger.trace("initialising LogService - logservice: {}", logservice);
        }
		catch(UnsatisfiedLinkError e) {
			this.warn("Unable to initialise LogService: {}", e.getMessage());
		}
		catch(NoClassDefFoundError e) {
			this.warn("Unable to initialise LogService: {}", e.getMessage());
		}
		catch(Exception e){
			this.warn("Unable to initialise LogService: {}", e.getMessage());
		}
		finally {
			if ( context != null ) 
				ContextFactory.releaseContext( context );
		}
	}

	//############################################################################
	//## StreamServe LogService Native Log Call
	//############################################################################

	private void nativeLogService(LogLevel level, String msg){
		if (logservice != null){
			try{
				logservice.log(name, msg, level);
			}catch(Exception e){
				logger.error("Exception with LogService: {}", e.getMessage());
				logservice=null;
			}
		}
	}

	//############################################################################
	//## StreamServe StrServiceable Native Log Call
	//############################################################################

	private void nativeStrsService(LogLevel loglevel, String msg){
		if (strsservice != null){
			int level = StrsServiceable.MSG_DEBUG;
			int type = 10;
			try {
				switch(loglevel){
				case severe	 : level = 0;  type = StrsServiceable.MSG_CRITICAL; break; // 0
				case error   : level = 1;  type = StrsServiceable.MSG_ERROR;    break; // 1
				case warning : level = 2;  type = StrsServiceable.MSG_WARNING;  break; // 2
				case info    : level = 4;  type = StrsServiceable.MSG_INFO;     break; // 3
				case debug   : level = 9;  type = StrsServiceable.MSG_DEBUG;    break; // 4
				//case trace   : level = 9;  type = StrsServiceable.MSG_DEBUG;    break; // 4
				case trace   : level = 10; type = StrsServiceable.MSG_DEBUG;    break; // 99
				}
				strsservice.writeMsg(type, level, String.format("%s: %s", name, msg));
			} catch (RemoteException e) {
				logger.error("Exception with StrsService: {}", e.getMessage());
				strsservice=null;
			}
		}
	}
	
	//############################################################################
	//## StreamServe format and call native log calls
	//############################################################################
	
	private void logService(LogLevel level, String format,Object arg){
		if (logservice != null || strsservice != null){
			String msg = String.format(format.replaceAll("\\{\\}", "%s"), arg);
			nativeLogService(level, msg);
			nativeStrsService(level, msg);
		}
	}
	
	private void logService(LogLevel level, String format,Object arg1, Object arg2){
		if (logservice != null || strsservice != null){
			String msg = String.format(format.replaceAll("\\{\\}", "%s"), arg1, arg2);
			nativeLogService(level, msg);
			nativeStrsService(level, msg);
		}
	}

	private void logService(LogLevel level, String format, Object...args){
		if (logservice != null || strsservice != null){
			String msg = String.format(format.replaceAll("\\{\\}", "%s"), args);
			nativeLogService(level, msg);
			nativeStrsService(level, msg);
		}
	}
	
	
	//############################################################################
	// * SLF4J Implementation
	//############################################################################
	
	@Override
	public void trace(String msg) {
		logger.trace(msg);
		logService(LogLevel.trace, msg);
	}

	@Override
	public void trace(String format, Object arg) {
		logger.trace(format, arg);
		logService(LogLevel.trace, format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		logger.trace(format, arg1, arg2);
		logService(LogLevel.trace, format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... args) {
		logger.trace(format, args);
		logService(LogLevel.trace, format, args);
	}

	@Override
	public void trace(String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.trace, msg);
		logger.trace(msg,t);
	}

	@Override
	public void trace(Marker marker, String msg) {
		logger.trace(marker,msg);
		logService(LogLevel.trace, msg);
		
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		logger.trace(marker, format, arg);
		logService(LogLevel.trace, format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		logger.trace(marker, format, arg1, arg2);
		logService(LogLevel.trace, format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... args) {
		logger.trace(marker, format, args);
		logService(LogLevel.trace, format, args);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.trace, msg);
		logger.trace(marker, msg,t);
	}

	//############################################################################
	
	@Override
	public void debug(String msg) {
		logger.debug(msg);
		logService(LogLevel.debug, msg);
	}

	@Override
	public void debug(String format, Object arg) {
		logger.debug(format, arg);
		logService(LogLevel.debug, format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		logger.debug(format, arg1, arg2);
		logService(LogLevel.debug, format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... args) {
		logger.debug(format, args);
		logService(LogLevel.debug, format, args);
	}

	@Override
	public void debug(String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.debug, msg);
		logger.debug(msg,t);
	}

	@Override
	public void debug(Marker marker, String msg) {
		logger.debug(marker,msg);
		logService(LogLevel.debug, msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		logger.debug(marker, format, arg);
		logService(LogLevel.debug, format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(marker, format, arg1, arg2);
		logService(LogLevel.debug, format, arg1, arg2);
		
	}

	@Override
	public void debug(Marker marker, String format, Object... args) {
		logger.debug(marker, format, args);
		logService(LogLevel.debug, format, args);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.debug, msg);
		logger.debug(marker, msg,t);
	}

	//############################################################################
	
	@Override
	public void info(String msg) {
		logger.info(msg);
		logService(LogLevel.info, msg);
	}

	@Override
	public void info(String format, Object arg) {
		logger.info(format, arg);
		logService(LogLevel.info, format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		logger.info(format, arg1, arg2);
		logService(LogLevel.info, format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... args) {
		logger.info(format, args);
		logService(LogLevel.info, format, args);
	}

	@Override
	public void info(String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.info, msg);
		logger.info(msg,t);
	}

	@Override
	public void info(Marker marker, String msg) {
		logger.info(marker,msg);
		logService(LogLevel.info, msg);
		
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		logger.info(marker, format, arg);
		logService(LogLevel.info, format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		logger.info(marker, format, arg1, arg2);
		logService(LogLevel.info, format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... args) {
		logger.info(marker, format, args);
		logService(LogLevel.info, format, args);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.info, msg);
		logger.info(marker, msg,t);
	}

	//############################################################################
	
	@Override
	public void warn(String msg) {
		logger.warn(msg);
		logService(LogLevel.warning, msg);
	}

	@Override
	public void warn(String format, Object arg) {
		logger.warn(format, arg);
		logService(LogLevel.warning, format, arg);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		logger.warn(format, arg1, arg2);
		logService(LogLevel.warning, format, arg1, arg2);
	}

	@Override
	public void warn(String format, Object... args) {
		logger.warn(format, args);
		logService(LogLevel.warning, format, args);
	}

	@Override
	public void warn(String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.warning, msg);
		logger.warn(msg,t);
	}

	@Override
	public void warn(Marker marker, String msg) {
		logger.warn(marker,msg);
		logService(LogLevel.warning, msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		logger.warn(marker, format, arg);
		logService(LogLevel.warning, format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		logger.warn(marker, format, arg1, arg2);
		logService(LogLevel.warning, format, arg1, arg2);
		
	}

	@Override
	public void warn(Marker marker, String format, Object... args) {
		logger.warn(marker, format, args);
		logService(LogLevel.warning, format, args);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.warning, msg);
		logger.warn(marker, msg,t);
	}

	//############################################################################
	
	@Override
	public void error(String msg) {
		logger.error(msg);
		logService(LogLevel.error, msg);
	}

	@Override
	public void error(String format, Object arg) {
		logger.error(format, arg);
		logService(LogLevel.error, format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		logger.error(format, arg1, arg2);
		logService(LogLevel.error, format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... args) {
		logger.error(format, args);
		logService(LogLevel.error, format, args);
	}

	@Override
	public void error(String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.error, msg);
		logger.error(msg,t);
	}

	@Override
	public void error(Marker marker, String msg) {
		logger.error(marker,msg);
		logService(LogLevel.error, msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		logger.error(marker, format, arg);
		logService(LogLevel.error, format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		logger.error(marker, format, arg1, arg2);
		logService(LogLevel.error, format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... args) {
		logger.error(marker, format, args);
		logService(LogLevel.error, format, args);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.error, msg);
		logger.error(marker, msg,t);
	}


	//############################################################################
	
	public void severe(String msg) {
		logger.error(msg);
		logService(LogLevel.severe, msg);
	}

	public void severe(String format, Object arg) {
		logger.error(format, arg);
		logService(LogLevel.severe, format, arg);
	}

	public void severe(String format, Object arg1, Object arg2) {
		logger.error(format, arg1, arg2);
		logService(LogLevel.severe, format, arg1, arg2);
	}

	public void severe(String format, Object... args) {
		logger.error(format, args);
		logService(LogLevel.severe, format, args);
	}

	public void severe(String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.severe, msg);
		logger.error(msg,t);
	}

	public void severe(Marker marker, String msg) {
		logger.error(marker,msg);
		logService(LogLevel.severe, msg);
	}

	public void severe(Marker marker, String format, Object arg) {
		logger.error(marker, format, arg);
		logService(LogLevel.severe, format, arg);
	}

	public void severe(Marker marker, String format, Object arg1, Object arg2) {
		logger.error(marker, format, arg1, arg2);
		logService(LogLevel.severe, format, arg1, arg2);
	}

	public void severe(Marker marker, String format, Object... args) {
		logger.error(marker, format, args);
		logService(LogLevel.severe, format, args);
	}

	public void severe(Marker marker, String msg, Throwable t) {
		//logger goes last for throwable
		logService(LogLevel.severe, msg);
		logger.error(marker, msg,t);
	}

	//############################################################################
	
	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}
	@Override
	public boolean isTraceEnabled(Marker marker) {
		return logger.isTraceEnabled(marker);
	}
	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	@Override
	public boolean isDebugEnabled(Marker marker) {
		return logger.isDebugEnabled(marker);
	}
	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}
	@Override
	public boolean isInfoEnabled(Marker marker) {
		return logger.isInfoEnabled(marker);
	}
	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}
	@Override
	public boolean isWarnEnabled(Marker marker) {
		return logger.isWarnEnabled(marker);
	}
	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}
	@Override
	public boolean isErrorEnabled(Marker marker) {
		return logger.isErrorEnabled(marker);
	}

}
