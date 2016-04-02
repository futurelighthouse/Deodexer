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
package deodex.ui.advancedSettings;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import deodex.Cfg;
import deodex.controlers.ProcessHandler;
import deodex.ui.FileDrop;

public class CustomizeAdbPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fieldText = "Drag drop , type full path or click browse";
	JCheckBox doUseCustom = new JCheckBox ();
	
	JTextField adbPathField = new JTextField();
	JButton BrowseBtn = new JButton();
	JLabel statusLab = new JLabel();
	
	public CustomizeAdbPanel(){
		this.setFocusable(true);
		this.setLayout(null);
		this.setSize(770, 570);
		this.setBounds(5, 5, 770, 570);
		// init comp. Titles
		this.setComponentsTitles();
		setComponentsBounds();
		addComponentsToThis();
		setActionsListeners();
		setDefaultStat();
		
	}
	
	private void setDefaultStat(){
		doUseCustom.setSelected(Cfg.doUseCustomAdb());
		this.adbPathField.setEnabled(doUseCustom.isSelected());
		this.BrowseBtn.setEnabled(doUseCustom.isSelected());
	}
	private void setComponentsBounds(){
		doUseCustom.setBounds(20, 20, 730, 40);
		adbPathField.setBounds(20, 80, 510, 40);
		BrowseBtn.setBounds(550, 80, 180, 40);
		statusLab.setBounds(20, 140, 730, 40);
	}
	
	private void setComponentsTitles(){
		doUseCustom.setText("Use custom adb binary");
		adbPathField.setText(Cfg.getCustomAdbBinary().equals("default") ? this.fieldText : Cfg.getCustomAdbBinary());
		BrowseBtn.setText("Browse");	
		statusLab.setText("");
	}
	
	private void addComponentsToThis(){
		this.add(doUseCustom);
		this.add(BrowseBtn);
		this.add(adbPathField);
		this.add(statusLab);
	}
	
	private CustomizeAdbPanel getThis(){
		return this;
	}
	
	private void setActionsListeners(){
		this.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
	
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				getThis().requestFocusInWindow();
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			
			}
			
		});
		BrowseBtn.addActionListener(new Listener());
		@SuppressWarnings("unused")
		FileDrop fd = new FileDrop(this.adbPathField,new FileDrop.Listener() {
			
			@Override
			public void filesDropped(File[] files) {
				String Os = Cfg.getOs();
				String adbFile = "adb";
				if(Os.equals("windows")){
					adbFile = "adb.exe";
				}
				if(files[0].getName().equals(adbFile)){
					adbPathField.setText(files[0].getAbsolutePath());
					getThis().validateNewBinary();
				} else {
					statusLab.setText("only adb binaries are accepted !");
					statusLab.setForeground(Color.red);
				}
			}
		});
		adbPathField.addFocusListener(new FocusListener(){

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				if(adbPathField.getText().equals(fieldText)){
					adbPathField.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				validateNewBinary();
			}
			
		});
		this.doUseCustom.addActionListener(new Listener());
	}
	
	public boolean validateNewBinary(){
		String adb = adbPathField.getText();
		Exception ex = new Exception();
		String[] cmd = {adb ,"version"};
		int exitCode = -999;
		ProcessHandler p = new ProcessHandler(cmd);
		try {
			exitCode = p.excute();
		} catch (IOException | InterruptedException e) {
			ex = e;
		}
		String output = "";
		for(String str : p.getGlobalProcessOutput()){
			output = output + str;
		}
		boolean isAdb = output.contains("Android Debug Bridge");
		if(exitCode != 0){
			if(exitCode == 13  || ex.getClass().equals(IOException.class)){
				statusLab.setText("The chosen adb is missing the excute permission or is not a valid exutable");
			} else if (exitCode == 2){
				statusLab.setText("The chosen adb does not exist");
			} else {
				statusLab.setText("the chosen adb is not valid pelease check again");
			}
			statusLab.setForeground(Color.red);
			adbPathField.setText(Cfg.getCustomAdbBinary().equals("default") ? this.fieldText : Cfg.getCustomAdbBinary());
			Cfg.setUseCustomAdb(false);
			
		} else if(isAdb){
			statusLab.setText("The chosen adb binary/command is valid ");
			statusLab.setForeground(Color.GREEN);
			Cfg.setCustomAdbBinary(adbPathField.getText());
			Cfg.setUseCustomAdb(true);
		} else if(!isAdb){
			statusLab.setText("the chosen adb is not an adb binary please double check before trying again");
			statusLab.setForeground(Color.red);
			adbPathField.setText(Cfg.getCustomAdbBinary().equals("default") ? this.fieldText : Cfg.getCustomAdbBinary());
			Cfg.setUseCustomAdb(false);
		}
		return exitCode == 0 && isAdb;
	}
	
	private void browseAction(){
		JFileChooser fs = new JFileChooser();
		fs.setFileSelectionMode(JFileChooser.FILES_ONLY);
		javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter(){

			@Override
			public boolean accept(File file) {
				String Os = Cfg.getOs();
				String adbFile = "adb";
				if(Os.equals("windows")){
					adbFile = "adb.exe";
				}
				return file.getName().equals(adbFile);
			}

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return "adb binary";
			}
			
		};
		fs.removeChoosableFileFilter(fs.getFileFilter());
		fs.addChoosableFileFilter(filter);
		
		if(fs.showOpenDialog(this) == 0){
			this.adbPathField.setText(fs.getSelectedFile().getAbsolutePath());
		};
		validateNewBinary();
	}
	
	private void doUseAction(){
		if(this.validateNewBinary()){
			Cfg.setUseCustomAdb(this.doUseCustom.isSelected());
		}
		this.BrowseBtn.setEnabled(doUseCustom.isSelected());
		this.adbPathField.setEnabled(doUseCustom.isSelected());
		if(!this.doUseCustom.isSelected()){
			statusLab.setText("");
			Cfg.setUseCustomAdb(false);
		}
		//this.setDefaultStat();
	}
	
	class Listener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object source = arg0.getSource();
			if(source.equals(BrowseBtn)){
				browseAction();
			} else if (source.equals(doUseCustom)){
				doUseAction();
			}
			
		}
		
	}
}
