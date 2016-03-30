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
package createZip;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.alee.laf.rootpane.WebFrame;

import deodex.R;
import deodex.S;
import deodex.controlers.FlashableZipCreater;
import deodex.tools.FilesUtils;
import deodex.ui.LoggerPane;
import deodex.ui.MyWebButton;

public class CreateFlashableZip extends WebFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JTextField browseField = new JTextField(R.getString("browse.Field"));
	MyWebButton browseBtn = new MyWebButton(R.getString("browseBtn"));
	MyWebButton createZip = new MyWebButton(R.getString("create.zip.btn"));
	LoggerPane logger = new LoggerPane("");
	JPanel mainPan = new JPanel();

	
	public CreateFlashableZip() {
		this.setTitle("Zip creater");
		this.setResizable(false);
		this.setIconImage(R.icon);
		this.setDefaultCloseOperation(WebFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setSize(530, 350);
		this.setLayout(null);
		mainPan.setLayout(null);
		mainPan.setBounds(0, 0, 530, 400);
		browseField.setBounds(20, 20, 330, 40);
		browseBtn.setBounds(350, 20, 160, 40);
		createZip.setBounds(20, 70, 490, 40);
		logger.setBounds(20, 120, 490, 185);
		this.add(mainPan);
		browseField.setEditable(false);
		mainPan.add(browseBtn);
		mainPan.add(browseField);
		mainPan.add(createZip);
		mainPan.add(logger);
		// defaults
		createZip.setEnabled(false);
		// actions 
		browseBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Sellect a Rom system folder");
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.showOpenDialog(getThis());
				if(fc.getSelectedFile() != null){
					if(valid(fc.getSelectedFile())){
						logger.addLog(R.getString(S.LOG_INFO)+R.getString("0000148"));
						createZip.setEnabled(true);
						browseField.setText(fc.getSelectedFile().getAbsolutePath());
					}
				} else {
					createZip.setEnabled(false);
					logger.addLog(R.getString(S.LOG_INFO)+R.getString("0000149"));
				}
				
			}
			
		});
		
		createZip.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int agree = JOptionPane.showConfirmDialog(getThis(),
						R.getString("0000009") + "\n" + R.getString("0000010") + "\n" + R.getString("0000011") + "\n"
								+ R.getString("0000012") + "\n" + R.getString("0000013") + "\n" + R.getString("0000014")
								+ "\n" + R.getString("0000015") + "\n" + R.getString("0000016") + "\n\n"
								+ R.getString("0000017"),
						R.getString("0000018"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (agree == 0 ) {
					String name = JOptionPane.showInputDialog(getThis(),
							R.getString("0000006") + "\n" + R.getString("0000007"));
					if (name != null) {
						boolean valid = false;
						try {
							new File(System.getProperty("java.io.tmpdir") + File.separator + name).getParentFile()
									.mkdirs();
							valid = new File(System.getProperty("java.io.tmpdir") + File.separator + name)
									.createNewFile();
							new File(System.getProperty("java.io.tmpdir") + File.separator + name).delete();
						} catch (InvalidPathException | IOException ex) {
							ex.printStackTrace();
							valid = false;
						}
						System.out.println(valid);
						if (valid) {
							new File(name).delete();
							String abName = name.endsWith(".zip") ? name.substring(0, name.lastIndexOf(".")) : name;
							createZip.setEnabled(false);
							File zipFile = new File(S.ZIP_OUTPUT + File.separator + abName + ".zip");
							new FlashableZipCreater(new File(browseField.getText()), zipFile, getThis());
						} else {
							JOptionPane.showMessageDialog(getThis(), R.getString("0000008"));
						}
					}
				}
			}
			
		});
		this.setVisible(true);
		
	}
	
	private boolean valid (File folder){
		File buildProp = new File(folder+"/build.prop");
		if(!buildProp.exists()){
			logger.addLog(R.getString(S.LOG_ERROR));
			return false;
		}
		int apkCount = FilesUtils.searchrecursively(folder, ".apk").size();
		int jarCount = FilesUtils.searchrecursively(folder, ".jar").size();
		if(apkCount <= 0 || jarCount <= 0){
			logger.addLog(R.getString(S.LOG_ERROR)+R.getString("0000145"));
			return false;
		}
		int odexCount = FilesUtils.getOdexCount(folder);
		if(odexCount > 0 ){
			logger.addLog(R.getString(S.LOG_WARNING)+"["+R.getString("log.there.is")+" "+odexCount+R.getString("0000146"));
			logger.addLog(R.getString(S.LOG_WARNING)+R.getString("0000147"));
		}
		return true;
	}
	private WebFrame getThis(){
		return this;
	}
}
