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
package deodex;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.alee.laf.WebLookAndFeel;

import deodex.controlers.CommandLineWorker;
import deodex.controlers.MainWorker;
import deodex.tools.AdbUtils;
import deodex.tools.CmdLogger;
import deodex.tools.FilesUtils;
import deodex.tools.Logger;
import deodex.tools.Os;
import deodex.tools.PathUtils;
import deodex.tools.StringUtils;
import deodex.ui.LangFrame;
import deodex.ui.Window;

public class Main {
	/**
	 * the command line logger used by the program to log progress to the user
	 */
	public static CmdLogger logger = new CmdLogger();
	/**
	 * the available options
	 */
	public static final String[] OPTIONS = { "z", "s", "c" };

	/**
	 * this is where all the command line tool magic happens
	 * 
	 * @param args
	 *            the main method arguments
	 * @return this method returns on all tasks terminated if errors were faced
	 *         it will call System.exit(int code) to exit with the proper code
	 */
	private static void argsReader(String[] args) {
		R.initResources();
		S.initTempFolders();
		boolean zipalign = false;
		boolean sign = false;
		boolean createZip = false;
		boolean adbExtracted = false;
		File systemFolder;
		if (args.length == 2) {
			String source = args[0];
			if (source.equals("e")) {
				adbExtracted = true;
				systemFolder = new File(
						S.EXTRACTED_SYSTEMS.getAbsolutePath() + File.separator + S.getRomExtractionFolderName());
			} else {
				systemFolder = new File(source);
				// does the folder exist ?
				if (!systemFolder.exists()) {
					System.out.println(systemFolder.getAbsolutePath() + " : No such file or directory");
					System.exit(2);
				}
				// can we write in this folder ?
				boolean canWrite = false;
				File writeTest = new File(systemFolder.getAbsolutePath() + File.separator + "test.write");
				try {
					canWrite = writeTest.createNewFile();
					writeTest.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (!canWrite) {
					System.out.println(systemFolder.getAbsolutePath() + " : read-only file system "
							+ "\n please make sure that the system folder is read-write before trying again !");
					System.exit(3);
				}

				// obviously if we are here the systemfolder exists and is rw so
				// lets proseed
			}

			String options = args[1];
			for (int i = 0; i < options.length(); i++) {
				String str = "" + options.charAt(i);
				boolean valid = false;
				for (String s : OPTIONS) {
					valid = valid || (s.equals(str));
				}
				if (!valid) {
					System.out.println("Unkown Option  : " + str);
					printHelp();
					return;
				}
			}
			// if we didn't return every thing is ok !
			zipalign = options.contains("z");
			sign = options.contains("s");
			createZip = options.contains("c");
			Main.proseedWithNoGui(systemFolder, sign, zipalign, createZip, adbExtracted);
		} else {
			String source = args[0];
			systemFolder = new File(source);
			if (!systemFolder.exists()) {
				System.out.println(systemFolder.getAbsolutePath() + " : No such file or directory");
				System.exit(2);
			}
			// can we write in this folder ?
			boolean canWrite = false;
			File writeTest = new File(systemFolder.getAbsolutePath() + File.separator + "test.write");
			try {
				canWrite = writeTest.createNewFile();
				writeTest.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!canWrite) {
				System.out.println(systemFolder.getAbsolutePath() + " : read-only file system "
						+ "\n please make sure that the system folder is read-write before trying again !");
				System.exit(3);
			}
			Main.proseedWithNoGui(systemFolder, sign, zipalign, createZip, adbExtracted);
		}

	}

	/**
	 * Log system informations to the log file this have no effect on the
	 * software it's here for logging purpose
	 */
	private static void logOsInfo() {
		// lets log SystemInfos
		Logger.appendLog("[Main][I]User Os is " + Cfg.getOs());
		Logger.appendLog("[Main][I]Os name : " + Os.getOsName());
		Logger.appendLog("[Main][I]User Platform is : " + Os.platform());
		Logger.appendLog("[Main][I]JAVA version : " + System.getProperty("java.version"));
		Logger.appendLog("[Main][I]Available cores (cpu) = " + HostInfo.availableCpus());
		Logger.appendLog("[Main][I]Max allocated memory = " + HostInfo.getMaxMemory() + " bytes");
	}

	/**
	 * The entring point
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		if (args == null || args.length == 0) {
			WebLookAndFeel.install();
			PathUtils.logCallingProcessLocation();
			logOsInfo();
			HostInfo.logInfo();
			if (Cfg.isFirstLaunch()) {
				Cfg.setCurrentLang(S.ENGLISH);
				R.initResources();
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						@SuppressWarnings("unused")
						LangFrame win = new LangFrame();
					}
				});

			} else {
				Cfg.readCfg();
				R.initResources();
				S.initTempFolders();
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						@SuppressWarnings("unused")
						Window win = new Window();
					}
				});

			}
		} else {
			Logger.logToStd = false;
			if (Cfg.isFirstLaunch()) {
				// Init config
				Cfg.setCurrentLang(S.ENGLISH);
			} else {
				// Load config
				Cfg.readCfg();
			}
			if(args[0].toLowerCase().equals("--config")){
				configureFromCmd(args);
				return;
			}
			if (args.length > 2) {
				printHelp();
			} else if (args.length == 1 && args[0].equals("h")) {
				R.initResources();
				printHelp();
			} else {
				argsReader(args);
			}

		}
	}

	private static void configureFromCmd(String[] args){
		ArrayList <String> params = new ArrayList<String>();
		if(args.length == 1){
			System.out.println("No parameter was sent in argument !");
			System.out.println("reffer to help excuting this command :");
			System.out.println("java -jar "+PathUtils.getExcutionPath()+File.separator+"Launcher.jar h");			
		} else {
			for (int i = 1 ; i < args.length ; i++){
				params.add(args[i]);
			}
			for(String str : params ){
				if(str.contains("=")){
					if(str.toLowerCase().startsWith("j")){
						int jobs = 1;
						try {
							jobs = Integer.parseInt(StringUtils.getSubString(str, 1));
							if(jobs > 4 || jobs <= 0){
								System.out.println("Invalid jobs value (0 > j < 5)");
							} else {
								Cfg.setMaxJobs(jobs);
							}
						} catch (Exception e){
							System.out.println("Error ! Couldn't parse int from "+str);
						}
					} else if(str.toLowerCase().startsWith("zip")){
						int comp = 0;
						try {
							comp = Integer.parseInt(StringUtils.getSubString(str, 3));
							if(comp != 0 && comp != 1 && comp != 2){
								System.out.println("Invalid comp value compression method can be either 0,1 or 2");
							} else {
								Cfg.setCompresionMathod(comp);
							}
						} catch (Exception e){
							System.out.println("Error ! Couldn't parse int from "+str);
						}
					}
				} else {
					System.out.println("can't parse parameter "+str+" please reffer to help for details");
				}
			}
		}
	}
	
	/**
	 * Prints the help to the command line
	 */
	private static void printHelp() {
		System.out.println("_____________________________________________________________");
		System.out.println("|         Lordroid One Deodexer To Rule'em All              |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("|                                                           |");
		System.out.println("| USAGE :                                                   |");
		System.out.println("| java -jar Launcher.jar <source> [OPTIONS]                 |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| <source> can be either                                    |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| PATH to System Folder exemple : /path/system              |");
		System.out.println("|                   OR                                      |");
		System.out.println("| e : to extract systemFolder directlly from device         |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("|                                                           |");
		System.out.println("| Options :                                                 |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| c : create a flashabe zip  after deodexing the rom        |");
		System.out.println("| z : zipalign every apk after deodexing it                 |");
		System.out.println("| s : sign every apk after deodexing                        |");
		System.out.println("| h : print this help page                                  |");
		System.out.println("| please note that options should'nt be separated by spaces |");
		System.out.println("|                                                           |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| Exemple :                                                 |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| java -jar Launcher.jar /path/system zsc                   |");
		System.out.println("| this command will deodex   and sign and zipalign          |\n"
		           		+  "| and then creates a flashable zip file                     |");
		System.out.println("| java -jar Launcher.jar e  zsc                             |");
		System.out.println("| this command will extract and deodex                      |\n"
				         + "| from connected device                                     |\n"
			         	+  "| then sign and zipalign                                    |\n"
			         	+  "| and then creates a flashable zip file                     |");
		System.out.println("|                                                           |\n"
			          	+  "|-----------------------------------------------------------|\n"
			         	+  "| NOTE :                                                    |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("|extracted systems will be under extracted_system_folders   |");
		System.out.println("|create flashable zip will be under flashable_zips_out      |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("|  Configuring the tool                                     |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| java -jar Launcher --config <param1>=value <param2>=value |");
		System.out.println("|                                                           |");
		System.out.println("| available options :                                       |");
		System.out.println("| j : define the max number of jobs to be used max is 4     |");
		System.out.println("| zip : define the compression method possible values are   |");
		System.out.println("|       zip=0 : use aapt method (default best compatibility)|");
		System.out.println("|       zip=1 : use J4zip method (better compression)       |");
		System.out.println("|       zip=2 : use 7zip method (if nothing else works)     |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| Exemple:                                                  |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("| java -jar Launcher.jar --config j=4 zip=0                 |");
		System.out.println("| NOTE : you can't configure and deodex at the same time    |");
		System.out.println("|-----------------------------------------------------------|");
		System.out.println("|                 © Rachid Boudjelida 2016                  |");
		System.out.println("|             Software distributed under GPL V3             |");
		System.out.println("|___________________________________________________________|");

	}

	/**
	 * this is used when the program is called from the command line with
	 * arguments
	 * 
	 * @param systemFolder
	 * @param sign
	 * @param zipalign
	 * @param createZip
	 * @param fromdevice
	 */
	private static void proseedWithNoGui(File systemFolder, boolean sign, boolean zipalign, boolean createZip,
			boolean fromdevice) {
		// lets check if system folder is a valid one
		boolean valid = FilesUtils.isAValideSystemDir(systemFolder, logger);
		if (!valid) {
			System.exit(3);
		}
		if (fromdevice) {
			AdbUtils.extractSystem(systemFolder, logger);
		}
		SessionCfg.setSign(sign);
		SessionCfg.setZipalign(zipalign);
		int jobs =1;
		try {
			jobs = Cfg.getMaxJobs();
		} catch (Exception e){
			e.printStackTrace();
			jobs = (HostInfo.availableCpus() >= 4 ? 4 : (HostInfo.availableCpus() >= 2 ? 2 : 1));
		}
		logger.addLog("[INFO] About to start task :");
		if(!fromdevice)
			logger.addLog("[INFO] System Folder : "+systemFolder);
		else
			logger.addLog("[INFO] System Folder : will be extracted from device");
		logger.addLog("[INFO] Max Jobs : "+jobs);
		logger.addLog("[INFO] Will zipalign ? : "+ (zipalign ? "YES":"NO") );
		logger.addLog("[INFO] Will reSign ? : "+ (sign ? "YES":"NO") );
		logger.addLog("[INFO] Will create flashable zip ? : "+ (createZip ? "YES":"NO") );
		
		MainWorker mainWorker = new MainWorker(systemFolder, logger, jobs, new CommandLineWorker(createZip));
		Thread t = new Thread(mainWorker);
		t.start();
	}

}
