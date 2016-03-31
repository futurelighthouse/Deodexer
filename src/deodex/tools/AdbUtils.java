/*
 *  Lordroid One Deodexer To Rule Them All
 * 
 *  Copyright 2016 Rachid Boudjelida <rachidboudjelida@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package deodex.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import deodex.Cfg;
import deodex.R;
import deodex.S;
import deodex.controlers.LoggerPan;
import deodex.controlers.ProcessHandler;

public class AdbUtils {
	/**
	 * the null device to be used when no device was found
	 */
	public static final String NULL_DEVICE = "null|null";

	/**
	 * extract /system from device to the given location the method uses adb
	 * host native binary to extract we test fail from the exit code of adb
	 * 
	 * @param outputFolder
	 *            the folder in which the extracted files will be stored (needs
	 *            to be a directory) the directory will be created make sure you
	 *            have write acess
	 * @param logger
	 *            : the logger who will handle the logs and show them to the
	 *            user do not send null
	 * @return boolean : true only if framework and buildprop were extracted
	 */
	public static boolean extractSystem(File outputFolder, LoggerPan logger) {
		AdbUtils.killServer();
		AdbUtils.startServer();
		String[] remoteFiles = { "/app", "/priv-app", "/framework", "/build.prop", "/vendor", "/odex.app.sqsh",
				"/odex.priv-app.sqsh", "/odex.framework.sqsh", "/vendor", "/plugin", "/data-app" };
		int[] exitStatus = new int[remoteFiles.length];
		Runtime rt = Runtime.getRuntime();
		for (int i = 0; i < remoteFiles.length; i++) {
			String remoteFile = remoteFiles[i];
			String[] cmd = { S.getAdbBin(), "pull", "/system" + remoteFile,
					new File(outputFolder.getAbsolutePath() + remoteFile).getAbsolutePath() };
			exitStatus[i] = -999;
			try {
				Process p = rt.exec(cmd);
				AdbStreamReader stdReader = new AdbStreamReader(p.getInputStream(), logger, R.getString(S.LOG_INFO));
				AdbStreamReader errReader = new AdbStreamReader(p.getErrorStream(), logger, R.getString(S.LOG_INFO));
				stdReader.start();
				errReader.start();
				exitStatus[i] = p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boolean success = exitStatus[2] == 0 && exitStatus[3] == 0;
		return success;
	}

	/**
	 * will return only the device name from the output of "adb devices"
	 * 
	 * @param out
	 *            : must be the output on "adb devices" command
	 * @return deviceName the device name parsed from the out parameter
	 */
	private static String getDeviceName(String out) {
		String tmp = "";
		for (int i = 0; i < out.length(); i++) {
			if (out.charAt(i) != '	') {
				tmp = tmp + out.charAt(i);
			} else {
				break;
			}
		}
		return tmp;
	}

	/**
	 * @param logger
	 *            : a LoggerPan to write the logging outputs (can't be null ! )
	 *            to by pass this if you wanna use a null parameter create a
	 *            blank LoggerPan implementation that does nothing on the
	 *            implemented methods
	 * @return device with status formated like this device|status
	 */
	public static String getDevices(LoggerPan logger) {
		String formatedDevice = "";
		boolean killStatus = killServer();
		if (!killStatus) {
			Logger.appendLog("[AdbUtils][E] adb server couldn't be killed aborting ...");
			logger.addLog(R.getString(S.LOG_ERROR) + R.getString("0000019"));
			return NULL_DEVICE;
		}

		boolean startStatus = startServer();
		if (!startStatus) {
			Logger.appendLog("[AdbUtils][E] adb server couldn't be started aborting ...");
			logger.addLog(R.getString(S.LOG_ERROR) + R.getString("0000020"));
			return NULL_DEVICE;
		}

		String[] cmd = { S.getAdbBin(), "devices" };

		try {
			ProcessHandler ph = new ProcessHandler(cmd);
			int exitValue = ph.excute();
			// read the output from the command
			ArrayList<String> output = ph.getGlobalProcessOutput();
			System.out.println("we are here ..");
			for (String s : output) {
				System.out.println(s);
			}
			if (exitValue != 0) {
				logger.addLog(R.getString(S.LOG_ERROR) + R.getString("0000021"));
				Logger.appendLog("[AdbUtils][E]" + "adb exited with non zero code error=" + exitValue);
				return NULL_DEVICE;
			}
			if (output.size() > 3) {
				logger.addLog(R.getString(S.LOG_ERROR + R.getString("0000022")));
				return NULL_DEVICE;
			}
			if (output.size() < 3) {
				logger.addLog(R.getString(S.LOG_ERROR) + R.getString("0000023"));
				if (Cfg.getOs().equals("windows"))
					logger.addLog(R.getString(S.LOG_INFO) + R.getString("0000024"));
				else if (Cfg.getOs().equals("linux"))
					logger.addLog(R.getString(S.LOG_INFO) + R.getString("0000025"));
				else if (Cfg.getOs().equals("osx")) {
					logger.addLog(R.getString(S.LOG_INFO) + R.getString("0000026"));
				}

				return NULL_DEVICE;
			}
			if (output.size() == 3) {
				formatedDevice = getDeviceName(output.get(1)) + "|" + AdbUtils.getDeviceStatus(output.get(1));
				logger.addLog(R.getString(S.LOG_INFO) + R.getString("0000027") + getDeviceName(output.get(1))
						+ R.getString("0000028") + AdbUtils.getDeviceStatus(output.get(1)));
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return NULL_DEVICE;
		}

		return formatedDevice;
	}

	/**
	 * will read the device status from the "adb devices" command output
	 * received as a parameter and return only the status value
	 * 
	 * @param out
	 *            : must be the output of the command adb devices as a
	 *            string @see : AdbUtils.getDevices(LoggerPan logger)
	 * @return status : the Device Status
	 */
	private static String getDeviceStatus(String out) {
		return out.substring(out.lastIndexOf("	") + 1, out.length());
	}

	/**
	 * this will run "adb kill-server" and gets it's output returns true only if
	 * the adb exit code was 0
	 * 
	 * @return serverWasKilled ?
	 */
	public static boolean killServer() {
		// if we have no adb binary dont bother
		if (Cfg.getOs().equals("null")) {
			Logger.appendLog("[AdbUtils][E]ADB is not supported by this OS");
			return false;
		}
		String[] cmd = { S.getAdbBin(), "kill-server" };
		return CmdUtils.runCommand(cmd) == 0;
	}

	/**
	 * this will run "adb start-server" and gets it's output returns true only
	 * if the adb exit code was 0
	 * 
	 * @return serverStarted ?
	 */
	private static boolean startServer() {
		// if we don't have adb binaries
		if (Cfg.getOs().equals("null")) {
			Logger.appendLog("[AdbUtils][E]ADB is not supported by this OS");
			return false;
		}
		String[] cmd = { S.getAdbBin(), "start-server" };
		return CmdUtils.runCommand(cmd) == 0;
	}
}
