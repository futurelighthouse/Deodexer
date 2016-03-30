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
package deodex.controlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import deodex.tools.CmdLogger;

public class ProcessHandler {
	private final String[] cmd;
	private static final Runtime rt = Runtime.getRuntime();
	
	private int exitValue = -999;
	private ArrayList<String> sdtInput = new ArrayList<String>();
	private ArrayList<String> errInput = new ArrayList<String>();
	private ArrayList<String> globalIn = new ArrayList<String>();
	/**
	 * @return the exitValue
	 */
	public int getProcessExitValue() {
		return exitValue;
	}


	/**
	 * @return the sdtInput
	 */
	public ArrayList<String> getProcessSdtOutput() {
		return sdtInput;
	}


	/**
	 * returns an array list with all the lines of the 
	 * output of the command (Error input)
	 * @return the errInput
	 */
	public ArrayList<String> getProcessErrOutput() {
		return errInput;
	}


	/**
	 * @return the globalIn
	 */
	public ArrayList<String> getGlobalProcessOutput() {
		return globalIn;
	}

	@SuppressWarnings("rawtypes")
	private ArrayList[] inputsLists = {sdtInput,errInput};
	
	/**
	 * 
	 * @param cmd
	 * @param logger
	 */
	public ProcessHandler(String[] cmd ) {
		this.cmd = cmd;
	}
	
	/**
	 * 
	 * @return the process exit value 
	 * @throws IOException 
	 * @throws InterruptedException
	 */
	public int excute() throws IOException, InterruptedException {

			Process p = rt.exec(cmd);
			new StreamToArray(p.getInputStream(),0).start();
			new StreamToArray(p.getErrorStream(),1).start();
		
			exitValue = p.waitFor();
		return exitValue;
	}

	/**
	 * @return the logger
	 */
	public LoggerPan getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(LoggerPan logger) {
		this.logger = logger;
	}

	/**
	 * @return cmd : the command 
	 */
	public String[] getCommand() {
		return cmd;
	}

	private synchronized void addGlobaleInput(String str ){
		synchronized(globalIn){
			globalIn.add(str);
		}
	}
	class StreamToArray implements Runnable {
		int type ;
		InputStream in ;
		public StreamToArray(InputStream in ,int type){
			this.type = type;
			this.in = in;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);
				String s;
				while ((s = br.readLine()) != null) {
					inputsLists[type].add(s);
					addGlobaleInput(s);
				}
				//in.close();
			} catch (Exception ex) {
				// TODO : do something on fail
			}
		}
		
		public void start(){
			new Thread(this).start();
		}

	}
}
