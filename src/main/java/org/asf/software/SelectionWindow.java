package org.asf.software;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class SelectionWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private File output = null;
	private boolean closed = false;

	public static File showWindow() {
		SelectionWindow frame = new SelectionWindow();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		while (!frame.closed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}
		return frame.output;
	}

	/**
	 * Create the frame.
	 */
	public SelectionWindow() {
		setResizable(false);
		setTitle("Open roadmap file...");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 228, 116);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				closed = true;
			}

		});
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnNewButton = new JButton("Open file");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser dialog = new JFileChooser(new File("."));
				dialog.setAcceptAllFileFilterUsed(false);
				dialog.addChoosableFileFilter(new FileFilter() {

					@Override
					public boolean accept(File arg0) {
						return arg0.getName().endsWith(".drf") || arg0.isDirectory();
					}

					@Override
					public String getDescription() {
						return "Digital Roadmap File (*.drf)";
					}

				});
				setVisible(false);
				int result = dialog.showOpenDialog(SelectionWindow.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					output = dialog.getSelectedFile();
					if (output.exists()) {
						closed = true;
						dispose();
						return;
					} else
						output = null;
				}
				setVisible(true);
			}
		});
		
		JLabel lblPleaseSelectYour = new JLabel("Please select your action...");
		contentPane.add(lblPleaseSelectYour);
		contentPane.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("New file");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser dialog = new JFileChooser(new File("."));
				dialog.setAcceptAllFileFilterUsed(false);
				dialog.addChoosableFileFilter(new FileFilter() {

					@Override
					public boolean accept(File arg0) {
						return arg0.getName().endsWith(".drf") || arg0.isDirectory();
					}

					@Override
					public String getDescription() {
						return "Digital Roadmap File (*.drf)";
					}

				});
				setVisible(false);
				int result = dialog.showSaveDialog(SelectionWindow.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					output = dialog.getSelectedFile();
					closed = true;
					dispose();
					return;
				}
				setVisible(true);
			}
		});
		contentPane.add(btnNewButton_1);
		setLocationRelativeTo(null);
	}
}
