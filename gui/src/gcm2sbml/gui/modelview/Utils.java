package gcm2sbml.gui.modelview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JRadioButton;

public class Utils {

	/**
	 * Sets up the button passed in.
	 */
	private static void setupButton(AbstractButton button, String iconFilename, String actionCommand, String tooltip, ActionListener listener){
		button.setActionCommand(actionCommand);
		
		
		button.setToolTipText(tooltip);
		button.addActionListener(listener);
		
		
		if(iconFilename.equals("")){
			// No icon, just set the text to the tooltip
			button.setText(tooltip);
		}else{
			
			// Use an icon.
			String ENVVAR = System.getenv("BIOSIM");
			String separator;
			if (File.separator.equals("\\")) {
				separator = "\\\\";
			}
			else {
				separator = File.separator;
			}
			String path = ENVVAR + separator
			+ "gui" + separator + "icons" + separator + "modelview" + separator
			+ iconFilename;

			// set the icon
			button.setIcon(new ImageIcon(path));

			// set a selected icon if it exists
			String selectedPath = path.replaceAll(".png", "_selected.png");
			if(new File(selectedPath).exists())
				button.setSelectedIcon(new ImageIcon(selectedPath));
			
		}
	}
	
	public static JRadioButton makeRadioToolButton(String iconFilename, String actionCommand, String tooltip, ActionListener listener, final ButtonGroup buttonGroup){
		final JRadioButton button = new JRadioButton();
		buttonGroup.add(button);
		Utils.setupButton(button, iconFilename, actionCommand, tooltip, listener);
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setBorderPainted(true);
		
		return button;
	}
	
	
	public static JButton makeToolButton(String iconFilename, String actionCommand, String tooltip, ActionListener listener){
		JButton button = new JButton();
		
		Utils.setupButton(button, iconFilename, actionCommand, tooltip, listener);
		return button;
	}
	
}
