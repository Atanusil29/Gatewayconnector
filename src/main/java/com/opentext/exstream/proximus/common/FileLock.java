package com.opentext.exstream.proximus.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLock {

	private static final Logger log = LoggerFactory.getLogger(FileLock.class);
	private static final int MAX_ATTEMPTS = 5;
	private static final int SLEEP_TIME = 30;

	private static File lockfileFile = null;
	private static FileOutputStream lockfileOs = null;
	private static java.nio.channels.FileLock lockfileLock = null;

	@Deprecated
	public static boolean lock() {
		return lock(null, null);
	}
	
	public static synchronized boolean lock(String lockfolder, String fileLockType) {
		File lockfile = new File(lockfolder, "file.lock");
		
		String os = System.getProperty("os.name").toLowerCase();
		boolean isLinux = os.indexOf("nux") >= 0;
		
		if (!isLinux || "channel".equalsIgnoreCase(fileLockType)) {
			log.debug("Getting file channel lock");
			return fileLock(lockfile);
		} else {
			log.debug("Getting Linux pid file lock");
			return pidLock(lockfile);
		}
	}
	private static boolean pidLock(File lockfile){

		try {
			String pid = getPid();
			if (pid == null) {
				log.error("Unable to get Process ID required to get lock.");
				return false;
			}


			int attempt=1;
			log.debug("Attempting to get file lock (pid: {})", pid);
			
			while (true){
				lockfileFile = lockfile;
				// If file does not exist, create file and write PID to file, then loop and re-check
				if (lockfile.createNewFile()) {
					FileUtils.writeStringToFile(lockfile, pid, "UTF-8");
					log.debug("Created new lock file");
					continue;
				}
				// check to make sure PID in file matches PID
				else {
					String filePid = FileUtils.readFileToString(lockfile, "UTF-8");
					// If PID in file matches PID, lock is obtained successfully
					if (pid.equals(filePid)){
						log.info("File lock obtained for process: {}", pid);
						lockfile.deleteOnExit();
						return true;
					}
					// If PID in file is no longer alive, write PID to file, then loop and re-check
					if (!isAlive(filePid)) {
						log.info("File lock exists, but process doesn't seem to be running");
						FileUtils.writeStringToFile(lockfile, pid, "UTF-8");
						continue;
					}
					// failed to get lock at this point.
					// if number of attempts has exceed maximum, then exit loop
					if (attempt >= MAX_ATTEMPTS){
						log.error("Unable to get file lock for process: {}, max attempts: {}", pid, attempt);
						return false;
					}
					//process can attempt more loops, 
					try{
						log.info("File lock held by process {}, sleeping {} seconds", filePid, SLEEP_TIME);
						Thread.sleep(SLEEP_TIME * 1000);
						log.debug("Attempting to get file lock (pid: {}), attempt {}", pid, ++attempt);
					}catch(Exception e){}
				}
			}

		} catch (Exception e) {
			log.error("Unable to check file lock", e);
			return false;
		}
	}

	private static boolean fileLock(File lockfile){
		log.debug("check lock status: lockfileLock={} / lockfileOs={}", lockfileLock==null?"null":"true", lockfileOs==null?"null":"true");
		if (lockfileOs != null){
			log.info("Process already has lock");
			return true;
		}
		try {
			int attempt=1;
			log.debug("Attempting to get file lock: {}", lockfile.getName());

			while (true){
				// If file does not exist, create file and write PID to file, then loop and re-check
				try{
					lockfileFile = lockfile;
					lockfileOs = new FileOutputStream(lockfile);
					lockfileLock = lockfileOs.getChannel().tryLock();
					if (lockfileLock==null){
						//close output stream otherwise delete will fail later.
						if (lockfileOs!=null) lockfileOs.close();
			            // File is already locked in thread or process within jvm
						throw new OverlappingFileLockException();
					}
					log.info("File lock obtained ({})", lockfileLock==null?"null":"true");
					return true;
				} catch (OverlappingFileLockException e) {
					log.debug("Unable to lock file: Overlapping lock");
		        }catch(Exception e){
					log.debug("Unable to lock file: {}", e.getMessage());
					lockfileOs= null;
					lockfileLock = null;
				}

				// failed to get lock at this point.
				// if number of attempts has exceed maximum, then exit loop
				if (attempt >= MAX_ATTEMPTS){
					log.error("Unable to get file lock, max attempts: {}", attempt);
					return false;
				}
				//process can attempt more loops, 
				try{
					log.info("File lock held, sleeping {} seconds", SLEEP_TIME);
					Thread.sleep(SLEEP_TIME * 1000);
					log.debug("Attempting to get file lock, attempt {}", ++attempt);
				}catch(Exception e){}
			}
			
		} catch (Exception e) {
			log.error("Unable to check file lock", e);
			return false;
		}
	}
	
	private static String getPid() throws IOException, InterruptedException {
		List<String> command = new ArrayList<String>();
		command.add("/bin/bash");
		command.add("-c");
		command.add("echo $PPID");
		ProcessBuilder pb = new ProcessBuilder(command);

		Process pr = pb.start();
		pr.waitFor();
		if (pr.exitValue() == 0) {
			BufferedReader outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			return outReader.readLine().trim();
		} else {
			log.error("Unable to get PID");
			return null;
		}
	}

	private static boolean isAlive(String pid) throws IOException, InterruptedException {
		List<String> command = new ArrayList<String>();
		command.add("ps");
		command.add("-p");
		command.add(pid);
		ProcessBuilder pb = new ProcessBuilder(command);

		Process pr = pb.start();
		pr.waitFor();
		if (pr.exitValue() == 0) {
			return true;
		} else {
			return false;
		}
	}


	public static synchronized void release() {
		try {
			log.debug("check lock status: lockfileLock={} / lockfileOs={}", lockfileLock==null?"null":"true", lockfileOs==null?"null":"true");

			if (lockfileFile != null || lockfileOs != null || lockfileLock!=null){
				log.debug("Releasing file lock");
				if (lockfileLock!=null) lockfileLock.release();
				if (lockfileOs != null) lockfileOs.close();
				if (lockfileFile != null){
					if (lockfileFile.delete())
						log.debug("lock file deleted");
					else
						log.debug("lock file not deleted");
				}
			}
			else{
				log.debug("file lock already released");
			}
			lockfileOs = null;
			lockfileLock = null;
			lockfileFile = null;
		} catch (Exception e) {
			log.warn("Exception while releasing lock: {}", e.getMessage());
		}
	}
	
}
