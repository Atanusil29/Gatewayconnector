package com.opentext.exstream.proximus.jobtracking;

import com.opentext.exstream.proximus.common.CustomLogger;

public class FileLockTest extends CommandBase {
	private static CustomLogger logger = new CustomLogger(RetryTracker.class);

	protected void execute(int sleeptime) throws Exception {
		long startTime = System.currentTimeMillis();
		
		try{
			Thread.sleep(sleeptime * 1000);
		}catch(Exception e){}
		
		long endTime = System.currentTimeMillis();
		logger.info("FileLock test completed in {} secs", String.format("%.1f", (endTime - startTime) / 1000.0f));
	}

	@Override
	protected void initialize() throws Exception {
		super.initialize();
	}

	public static void main(String[] args) {
		int sleeptime = (args.length == 0? 30 : Integer.parseInt(args[0]));
		try {
			FileLockTest test = new FileLockTest();
			test.initialize();
			test.execute(sleeptime);
			test.close();
			
		} catch (Exception e) {
			logger.error("Unable to complete FileLock", e);
		}
	}

}
