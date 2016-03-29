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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import deodex.controlers.LoggerPan;

public class AdbStreamReader implements Runnable{

	final InputStream is;
	final LoggerPan logger;
	final String streamType;

	/**
	 * this class is here to empty the Process InputStream 
	 * in a separate thread this way you can make sure you can read
	 * both std and err inputStream from process at the same time 
	 * @param is the input stream to read
	 * @param logger the logger null safe 
	 * @param streamType the stream name type like error or std null safe
	 */
	public AdbStreamReader(InputStream is, LoggerPan logger, String streamType) {
		this.is = is;
		this.logger = logger;
		this.streamType = streamType;
	}

	/**
	 * start a thread with this Runnable
	 */
	public void start(){
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String s;
			while ((s = br.readLine()) != null) {
				if(logger != null)
				logger.addLog((this.streamType != null ? this.streamType : "")+s);
			}
			is.close();
		} catch (Exception ex) {
			// do nothing the stream can be close at any time because the
			// proces
			// has reached his life cycle (remember we have 2 inputs streams
			// on for
			// std and one for err and we are emptying them at the same time
			// )
		}
	}

}
