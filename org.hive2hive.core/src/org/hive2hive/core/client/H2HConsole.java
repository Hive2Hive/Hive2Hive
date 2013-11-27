package org.hive2hive.core.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class H2HConsole extends WindowAdapter implements WindowListener {

	private JFrame frame;
	private JTextArea textArea;
	
	public H2HConsole() {
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = new Dimension(screenSize.width/2, screenSize.height/2);

		textArea = new JTextArea();
		textArea.setBackground(Color.black);
		textArea.setForeground(Color.green);
		textArea.setCaretColor(textArea.getForeground());
		textArea.setFont(new Font("Lucida Sans", Font.PLAIN, 14));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(true);
		
		frame = new JFrame("Hive2Hive Console");
		frame.setBounds(frameSize.width/2, frameSize.height/2, frameSize.width, frameSize.height);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		frame.setVisible(true);
		frame.addWindowListener(this);
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
