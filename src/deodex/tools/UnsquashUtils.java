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
import java.nio.file.Files;
import java.util.ArrayList;

import com.thoughtworks.xstream.io.path.Path;

import deodex.S;

/**
 * 
 * @author lord-ralf-adolf
 *
 */
public class UnsquashUtils {

	/**
	 * 
	 * @param squashFile
	 *            squashfs file to get the command for it
	 * @param dest
	 *            the output destination of the extraction command
	 * @return a Sting Array with the command to extract the given file to the
	 *         given destination
	 */
	private static String[] getUnsquashCommand(File squashFile, File dest) {
		String cmd[] = { S.getUnsquashBinary(), "-no-xattrs", "-f", "-n", "-d", dest.getAbsolutePath(),
				squashFile.getAbsolutePath() };

		return cmd;
	}

	/**
	 * 
	 * @return return if the squashfs tool is available
	 */
	public static boolean haveUnsquash() {
		String cmd[] = { S.getUnsquashBinary(), "-h" };

		int exitValue = CmdUtils.runCommand(cmd);

		return exitValue == 0 || exitValue == 1;
	}

	/**
	 * unsquash the .sqsh file under the given folder
	 * 
	 * @param systemFolder
	 *            the system folder where we will search for the squash files
	 * @return true only if the extraction was successful
	 */
	public static boolean unsquash(File systemFolder) {
		File appSquash = new File(systemFolder.getAbsolutePath() + File.separator + "odex.app.sqsh");
		File privAppSquash = new File(systemFolder.getAbsolutePath() + File.separator + "odex.priv-app.sqsh");
		File framSquash = new File(systemFolder.getAbsolutePath() + File.separator + "odex.framework.sqsh");
		// this one is a tricky one :
		// it has apks and odex files so ? 
		File productSquash = new File(systemFolder.getAbsolutePath() + "/etc/product/orig.applications.sqsh");

		File[] squashFiles = { appSquash, privAppSquash, framSquash,productSquash };

		File app = new File(systemFolder.getAbsolutePath() + "/app");
		File privApp = new File(systemFolder.getAbsolutePath() + "/priv-app");
		File framework = new File(systemFolder.getAbsolutePath() + "/framework");
		File product = new File(systemFolder.getAbsolutePath() + "/etc/product/applications");
		File[] systemFolders = { app, privApp, framework,product };

		// were we will be extracting the sqh files
		File destFile = S.getUnsquash();
		// make sure the destFile is not there
		FilesUtils.deleteRecursively(destFile);

		for (int i = 0; i < squashFiles.length; i++) {
			// lets delete the dest 
			FilesUtils.deleteRecursively(destFile);
			File squash = squashFiles[i];
			if (squash.exists()) {
				String[] cmd = getUnsquashCommand(squash, destFile);
				int exitValue = CmdUtils.runCommand(cmd);
				// if the exit value is not 0 we abort
				if (exitValue != 0) {
					Logger.appendLog("[E] failed while extracting " + squash);
					return false;
				}
				// may be the command exited with 0 code but the outputfolder is
				// empty
				if (destFile.listFiles() == null || destFile.listFiles().length <= 0) {
					Logger.appendLog("[E] failed while extracting " + squash + " the extracted folder is empty");
					return false;
				}
				
				// now that we are sure the low lovel command is successfull 
				// lets copy those files 
				
				ArrayList <File>odexDest = FilesUtils.searchrecursively(systemFolders[i], ".odex");
				// some squash files have apks too 
				odexDest.addAll(FilesUtils.searchrecursively(systemFolders[i],".apk" ));
				ArrayList <File>odexInExtractedSquash = FilesUtils.searchrecursively(destFile, ".odex");
				odexInExtractedSquash.addAll(FilesUtils.searchrecursively(destFile, ".apk"));
				
				boolean sucess = true;
				for (File f : odexInExtractedSquash){
					for (File ff : odexDest){
						if(ff.getName().equals(f.getName())){
							ff.delete();
							try {
								//lets make sure the file is deleted properly 
								// doesn't hurt to double check right ? 
								Files.deleteIfExists(ff.toPath());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							sucess = sucess && FilesUtils.copyFile(f, ff);
						}
					}
				}
				// lets check if not every thing was copied 
				if(!sucess){
					return false;
				}
			}
		}

		return true;
	}
}
