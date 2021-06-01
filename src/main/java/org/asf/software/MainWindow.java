package org.asf.software;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;

import org.asf.cyan.api.packet.PacketBuilder;
import org.asf.cyan.api.packet.PacketParser;

import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.SwingConstants;
import javax.swing.ScrollPaneConstants;

public class MainWindow {

	private JPanel panel_2;
	private JPanel panel_3;
	private JPanel panel_1;
	private JFrame frmDigitalRoadmapV;
	private File file;

	public void read() throws IOException {
		if (file.exists()) {
			FileInputStream strm = new FileInputStream(file);
			PacketParser parser = new PacketParser();
			parser.importStream(strm);
			read(parser);
			strm.close();
		}
	}

	public void read(PacketParser reader) {
		read(TODO, reader);
		read(IN_PROGRESS, reader);
		read(DONE, reader);
	}

	public void read(int location, PacketParser reader) {
		int count = reader.<Integer>nextEntry().get();
		for (int i = 0; i < count; i++) {
			add(reader.<String>nextEntry().get(), location);
		}
	}

	public void write() throws IOException {
		file.getParentFile().mkdirs();
		FileOutputStream outputStream = new FileOutputStream(file);
		PacketBuilder builder = new PacketBuilder();
		write(builder);
		builder.build(outputStream);
		outputStream.close();
	}

	public void writeSafe() {
		try {
			write();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frmDigitalRoadmapV, "Error occured while writing file:\n" + e, "Write Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void write(PacketBuilder builder) {
		write(TODO, builder);
		write(IN_PROGRESS, builder);
		write(DONE, builder);
	}

	public void write(int location, PacketBuilder builder) {
		JPanel panel;
		if (location == TODO) {
			panel = panel_3;
		} else if (location == IN_PROGRESS) {
			panel = panel_2;
		} else {
			panel = panel_1;
		}

		ArrayList<String> entries = new ArrayList<String>();
		for (Component comp : panel.getComponents()) {
			if (!(comp instanceof JPanel))
				continue;
			JPanel entry = (JPanel) comp;
			InfoHolderComponent holder = (InfoHolderComponent) entry.getComponents()[entry.getComponents().length - 1];
			entries.add(holder.getData());
		}

		builder.add(entries.size());
		for (String entry : entries) {
			builder.add(entry);
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 1 && args[0].equals("@install-windows")) {
			System.out.println("Preparing to install...");
			File programFiles = new File("/Program Files (x86)");
			if (!programFiles.exists()) {
				programFiles = new File("/Program Files");
			}
			if (!programFiles.exists() || !new File("/Windows/System32/reg.exe").exists()) {
				System.out.println(
						"Cannot install in non-windows systems using this method, please see our git repository.");
				System.exit(1);
			}
			if (!programFiles.canWrite()) {
				System.out.println("Cannot install without administrator rights.");
				System.exit(1);
			}

			System.out.println("Are you sure you want to install this program?");
			System.out.println("Installing will add .rmf and rmf to the HKEY_CLASSES_ROOT registry key.");
			System.out.println(
					"In order to uninstall, you will need to delete both keys and the default destop shortcut.");
			System.out.print("Continue? [Y/n] ");

			char ch = (char) System.in.read();
			if (ch == 'Y' || ch == 'y') {
				File destination = new File(programFiles, "DigitalRoadmap");
				destination.mkdirs();
				if (!destination.exists()) {
					System.out.println("Cannot install without administrator rights.");
					System.exit(1);
				}

				System.out.println("Installing jar file...");
				File outp = new File(destination, "digitalroadmap.jar");

				if (outp.exists())
					outp.delete();
				if (!outp.getParentFile().exists())
					outp.getParentFile().mkdirs();

				try {
					Files.copy(new File(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI())
							.toPath(), outp.toPath());
				} catch (IOException | URISyntaxException e) {
					throw new IOException(e);
				}

				File reg = new File(destination, "digitalroadmap.reg");
				File linkscript = new File(destination, "createlink.vbs");
				if (!reg.exists()) {
					InputStream strm = MainWindow.class.getClassLoader().getResourceAsStream("digitalroadmap.reg");
					String cont = new String(strm.readAllBytes())
							.replace("%exec%", destination.getCanonicalPath().replace("\\", "\\\\"))
							.replace("%java%", ProcessHandle.current().info().command().get().replace("\\", "\\\\"));
					Files.writeString(reg.toPath(), cont);
					strm.close();
				}

				System.out.println("Installing registry entries...");
				ProcessBuilder builder = new ProcessBuilder();
				builder.inheritIO();
				builder.command("reg", "import", reg.getCanonicalPath());
				Process proc = builder.start();
				try {
					proc.waitFor();
				} catch (InterruptedException e) {
				}

				if (proc.exitValue() != 0)
					throw new IOException("Registry command exited with non-zero exit code");

				File desktopLink = new File("/Users/Public/Desktop/Digital Roadmap.lnk");
				if (desktopLink.exists())
					desktopLink.delete();
				if (!desktopLink.getParentFile().exists())
					desktopLink.getParentFile().mkdirs();

				File desktopLink2 = new File("/ProgramData/Start Menu/Programs/Accessories/Digital Roadmap.lnk");
				if (desktopLink2.exists())
					desktopLink2.delete();
				if (!desktopLink2.getParentFile().exists())
					desktopLink2.getParentFile().mkdirs();

				if (!linkscript.exists()) {
					InputStream strm = MainWindow.class.getClassLoader().getResourceAsStream("link.vbs");
					String cont = new String(strm.readAllBytes()).replace("%exec%", destination.getCanonicalPath())
							.replace("%java%",
									ProcessHandle.current().info().command().get()
											.replace("%link1%", desktopLink.getCanonicalPath())
											.replace("%link2%", desktopLink2.getCanonicalPath()));
					Files.writeString(reg.toPath(), cont);
					strm.close();
				}
				builder = new ProcessBuilder();
				builder.command("cscript", linkscript.getCanonicalPath());
				builder.inheritIO();
				proc = builder.start();
				try {
					proc.waitFor();
				} catch (InterruptedException e) {
				}

				if (proc.exitValue() != 0)
					throw new IOException("Link creation command exited with non-zero exit code");
			}
			return;
		}
		final File output;
		if (args.length == 1) {
			output = new File(args[0]);
		} else {
			output = SelectionWindow.showWindow();
			if (output == null)
				return;
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.file = output;
					window.load();
					window.frmDigitalRoadmapV.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e2) {
			}
		}
		frmDigitalRoadmapV = new JFrame();
		frmDigitalRoadmapV.setTitle("Digital Roadmap V1.0");
		frmDigitalRoadmapV.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				try {
					write();
					System.exit(0);
				} catch (IOException e) {
					if (JOptionPane.showConfirmDialog(frmDigitalRoadmapV,
							"Error occured while writing file:\n" + e + "\n\nDo you want to cancel?", "Write Error",
							JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				}
			}

		});
		frmDigitalRoadmapV.setBounds(100, 100, 1533, 752);
		frmDigitalRoadmapV.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel_3 = new JPanel();
		JScrollPane scrollPane = new JScrollPane(panel_3, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		scrollPane.setPreferredSize(new Dimension(350, 10));
		panel.add(scrollPane);
		frmDigitalRoadmapV.getContentPane().add(panel, BorderLayout.WEST);
		panel_3.setLayout(new WrapLayout(WrapLayout.CENTER, 5, 5));

		JLabel lblNewLabel = new JLabel("To-do");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNewLabel, BorderLayout.NORTH);

		JButton btnNewButton_3 = new JButton("Add");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				add("", TODO);
			}
		});
		panel.add(btnNewButton_3, BorderLayout.SOUTH);

		JPanel panel_1_1 = new JPanel();
		panel_1_1.setPreferredSize(new Dimension(350, 10));
		panel_1_1.setLayout(new BorderLayout(0, 0));

		panel_1 = new JPanel();
		JScrollPane scrollPane_1 = new JScrollPane(panel_1, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel_1.setLayout(new WrapLayout(WrapLayout.CENTER, 5, 5));
		panel_1_1.add(scrollPane_1);
		panel_1_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JLabel lblDone = new JLabel("Done");
		lblDone.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1_1.add(lblDone, BorderLayout.NORTH);

		JButton btnNewButton_4 = new JButton("Manual Save (use after edit)");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				writeSafe();
			}
		});
		panel_1_1.add(btnNewButton_4, BorderLayout.SOUTH);
		frmDigitalRoadmapV.getContentPane().add(panel_1_1, BorderLayout.EAST);

		JPanel panel_2_1 = new JPanel();
		panel_2_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_2_1.setLayout(new BorderLayout(0, 0));

		panel_2 = new JPanel();
		JScrollPane scrollPane_2 = new JScrollPane(panel_2, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel_2.setLayout(new WrapLayout(WrapLayout.CENTER, 5, 5));
		panel_2_1.add(scrollPane_2);

		JLabel lblInprogress = new JLabel("In-progress");
		lblInprogress.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2_1.add(lblInprogress, BorderLayout.NORTH);
		frmDigitalRoadmapV.getContentPane().add(panel_2_1, BorderLayout.CENTER);

		frmDigitalRoadmapV.setLocationRelativeTo(null);
	}

	public void load() {
		try {
			read();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frmDigitalRoadmapV, "Error occured while reading input file:\n" + e,
					"Read Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static final int TODO = 1 << 1;
	private static final int IN_PROGRESS = 1 << 2;
	private static final int DONE = 1 << 3;

	private void add(String message, int location) {
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_4.setPreferredSize(new Dimension(310, 200));
		panel_4.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_4.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel.add(panel_6, BorderLayout.EAST);
		panel_6.setLayout(new BorderLayout(0, 0));

		JButton btnNewButton_1 = new JButton("<");
		panel_6.add(btnNewButton_1, BorderLayout.WEST);

		JButton btnNewButton = new JButton(">");
		panel_6.add(btnNewButton, BorderLayout.EAST);

		JButton btnNewButton_2 = new JButton("Delete");
		btnNewButton_2.setFocusable(false);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (location == TODO) {
					panel_3.remove(panel_4);
					panel_3.updateUI();
				} else if (location == IN_PROGRESS) {
					panel_2.remove(panel_4);
					panel_2.updateUI();
				} else {
					panel_1.remove(panel_4);
					panel_1.updateUI();
				}
				frmDigitalRoadmapV.invalidate();
				frmDigitalRoadmapV.validate();
				writeSafe();
			}
		});
		panel.add(btnNewButton_2, BorderLayout.WEST);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_4.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();
		textArea.setText(message);
		JScrollPane scrollPane_3 = new JScrollPane(textArea);
		panel_5.add(scrollPane_3, BorderLayout.CENTER);

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnNewButton_2.getActionListeners()[0].actionPerformed(null);
				if (location == IN_PROGRESS) {
					add(textArea.getText(), DONE);
					panel_1.updateUI();
				} else {
					add(textArea.getText(), IN_PROGRESS);
					panel_3.updateUI();
				}
				frmDigitalRoadmapV.repaint();
			}
		});
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnNewButton_2.getActionListeners()[0].actionPerformed(null);
				if (location == IN_PROGRESS) {
					add(textArea.getText(), TODO);
					panel_2.updateUI();
				} else {
					add(textArea.getText(), IN_PROGRESS);
					panel_1.updateUI();
				}
				frmDigitalRoadmapV.repaint();
			}
		});

		panel_4.add(new InfoHolderComponent(() -> textArea.getText()), BorderLayout.SOUTH);
		if (location == TODO) {
			btnNewButton_1.setVisible(false);
			panel_3.add(panel_4);
			panel_3.updateUI();
		} else if (location == IN_PROGRESS) {
			panel_2.add(panel_4);
			panel_2.updateUI();
		} else {
			btnNewButton.setVisible(false);
			panel_1.add(panel_4);
			panel_1.updateUI();
		}
		writeSafe();
	}

}
