package org.hive2hive.client.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.hive2hive.core.IH2HNode;

/**
 * A Java swing console that reassigns the standard channels STDIN, STDOUT and STDERR to itself.
 * 
 * @author Christian
 * 
 */
public final class Console extends WindowAdapter implements WindowListener, Runnable {

	private JFrame frame;
	private JTextArea textArea;

	private final PipedInputStream pis1;
	private final PipedInputStream pis2;

	private final PipedOutputStream pos3;

	private Thread readerThread1;
	private Thread readerThread2;

	private final AtomicBoolean quit;
	private IH2HNode h2hNode;

	public Console(String title) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = new Dimension(screenSize.width / 2, screenSize.height / 2);

		textArea = new JTextArea();
		textArea.setBackground(Color.black);
		textArea.setForeground(new Color(0xFEB12D));
		textArea.setCaretColor(textArea.getForeground());
		textArea.setFont(new Font("Lucida Sans", Font.PLAIN, 14));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(true);
		textArea.setMargin(new Insets(10, 10, 10, 10));

		frame = new JFrame(title);
		frame.setIconImage(new ImageIcon(getClass().getResource("/res/logo.png")).getImage());
		frame.setBounds(frameSize.width / 2, frameSize.height / 2, frameSize.width, frameSize.height);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		frame.setVisible(true);
		frame.addWindowListener(this);

		pis1 = new PipedInputStream();
		pis2 = new PipedInputStream();

		pos3 = new PipedOutputStream();

		redirectSystemStreams();

		configureKeyListener();

		// start 2 threads to read from pis1 and pis2
		readerThread1 = new Thread(this);
		readerThread1.setName("H2HConsole Reader 1");
		readerThread1.setDaemon(true);
		readerThread1.start();

		readerThread2 = new Thread(this);
		readerThread1.setName("H2HConsole Reader 2");
		readerThread2.setDaemon(true);
		readerThread2.start();

		quit = new AtomicBoolean(false);
	}

	private void redirectSystemStreams() {
		try {

			// reassign standard output stream (STDOUT)
			PipedOutputStream pos1 = new PipedOutputStream(pis1);
			System.setOut(new PrintStream(pos1, true));

			// reassign standard error stream (STDERR)
			PipedOutputStream pos2 = new PipedOutputStream(pis2);
			System.setErr(new PrintStream(pos2, true));

			// reassign standard input stream (STDIN)
			System.setIn(new PipedInputStream(pos3));

		} catch (IOException | SecurityException e) {
			print("Couldn't redirect one of the streams to this console\n" + e.getMessage());
		}
	}

	private void configureKeyListener() {

		textArea.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
				try {
					pos3.write(e.getKeyChar());
				} catch (IOException ex) {
				}
			}
		});
	}

	@Override
	public synchronized void run() {
		while (Thread.currentThread() == readerThread1) {
			handleInputStream(pis1);
		}

		while (Thread.currentThread() == readerThread2) {
			handleInputStream(pis2);
		}
	}

	private void handleInputStream(PipedInputStream pis) {
		try {
			this.wait(100);
		} catch (InterruptedException e) {
		}

		try {
			if (pis.available() != 0) {
				String input = readLine(pis);
				print(input);
			}
			if (quit.get())
				return;
		} catch (IOException e) {
			print("Console reports an internal error:\n" + e);
		}
	}

	private String readLine(PipedInputStream pis) throws IOException {
		String input = "";
		do {
			int available = pis.available();
			if (available == 0)
				break;
			byte buffer[] = new byte[available];
			pis.read(buffer);
			input = input + new String(buffer, 0, buffer.length);
		} while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit.get());
		return input;
	}

	public void print(String text) {
		textArea.append(text);
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	public void clear() {
		textArea.setText("");
	}

	@Override
	public void windowClosed(WindowEvent e) {

		quit.set(true);
		this.notifyAll(); // stop all threads

		try {
			readerThread1.join(1000);
			pis1.close();
		} catch (Exception ex) {
		}

		try {
			readerThread2.join(1000);
			pis2.close();
		} catch (Exception ex) {
		}

		try {
			pos3.close();
		} catch (Exception ex) {
		}

		System.exit(0);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		frame.setVisible(false);
		frame.dispose();

		// shut down network
		if (h2hNode != null) {
			h2hNode.disconnect();
		}
	}

	public void setH2HNode(IH2HNode h2hNode) {
		this.h2hNode = h2hNode;
	}
}
