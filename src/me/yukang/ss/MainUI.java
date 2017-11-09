package me.yukang.ss;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.exception.DecodingFailedException;

public class MainUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<String> model;
	private JTextField textField;
	private JTextArea textArea;
	private Map<String, String> historyMap;
	private String decoded;
	private String copy;
	private JList<String> historyList;
	private JButton btnGo;
	private JButton btnOpen;
	private JButton btnClear;
	private JButton btnCopy;
	private String qrCodePath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUI frame = new MainUI();
					frame.setLocationRelativeTo(null);
					frame.setTitle("Java解析二维码 Demo");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 533, 307);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		textField = new JTextField();
		textField.setToolTipText("二维码图片地址");
		textField.setText("http://51simple.com/imgs/us01.png");
		textField.setColumns(10);

		// 用来存储历史记录
		historyMap = new HashMap<>();

		btnGo = new JButton("Go");
		btnGo.addActionListener(this);
		btnOpen = new JButton("打开本地");
		btnOpen.addActionListener(this);

		historyList = new JList<>();
		// 历史记录选择事件
		historyList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				textArea.setText("解析结果：" + historyMap.get(historyList.getSelectedValue()));
			}
		});
		model = new DefaultListModel<>();
		historyList.setModel(model);

		// 解析结果显示区域
		textArea = new JTextArea();
		textArea.setLineWrap(true);

		btnClear = new JButton("清空历史");
		btnClear.addActionListener(this);
		btnCopy = new JButton("复制解析结果");
		btnCopy.addActionListener(this);

		// 二维码图片保存目录
		qrCodePath = System.getProperty("user.home") + File.separator + "QRCode";
		File qrCodeDir = new File(qrCodePath);
		if (!qrCodeDir.exists())
			qrCodeDir.mkdir();

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addGroup(gl_contentPane
								.createParallelGroup(Alignment.LEADING).addComponent(btnClear)
								.addComponent(textField, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
								.addGroup(gl_contentPane
										.createSequentialGroup().addGap(7)
										.addComponent(historyList, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane
								.createSequentialGroup()
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_contentPane.createSequentialGroup()
												.addComponent(btnGo, GroupLayout.PREFERRED_SIZE, 89,
														GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnOpen,
														GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE))
										.addComponent(textArea, GroupLayout.PREFERRED_SIZE, 198, Short.MAX_VALUE))
								.addGap(12))
								.addGroup(gl_contentPane.createSequentialGroup().addComponent(btnCopy)
										.addContainerGap()))));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(btnOpen, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnGo))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(textArea, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
								.addComponent(historyList, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnClear, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnCopy))
						.addGap(0)));
		contentPane.setLayout(gl_contentPane);
	}

	// 按钮事件监听
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnGo) {
			// 启动一个线程来获取远程图片
			new Thread() {
				public void run() {
					try {
						String content = textField.getText();
						URL url = new URL(content); // 构造 URL
						URLConnection conn = url.openConnection(); // 打开连接
						byte[] buffer = new byte[4096]; // 4k 的数据缓冲
						int len; // 读取到数据长度
						InputStream is = conn.getInputStream(); // 获取输入流
						String file = qrCodePath + File.separator + content.substring(content.lastIndexOf("/") + 1); // 文件保存位置
						OutputStream os = new FileOutputStream(file); // 文件输出流
						// 开始读取
						while ((len = is.read(buffer)) != -1)
							os.write(buffer, 0, len);
						// 完毕，关闭所有连接
						os.close();
						is.close();
						// 添加到历史记录
						if (!model.contains(content)) {
							model.addElement(content);
						}
						decodeImage(file);
						// 解析成功后选中该条目
						historyList.setSelectedIndex(model.size() - 1);
					} catch (MalformedURLException e1) {
						JOptionPane.showMessageDialog(null, "URL地址不合规范");
						e1.printStackTrace();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "远程图片未找到");
						e1.printStackTrace();
					}
				};
			}.start();
		}

		// 打开文件
		if (e.getSource() == btnOpen) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("图片", "png", "jpg", "jpeg"));
			if (chooser.showOpenDialog(MainUI.this) == JFileChooser.APPROVE_OPTION) {
				String filepath = chooser.getSelectedFile().getAbsolutePath();
				model.addElement(filepath);
				String out = decodeImage(filepath);
				if (out != null)
					historyMap.put(filepath, out);
				historyList.setSelectedIndex(model.size() - 1);
			}
		}

		// 清空历史记录
		if (e.getSource() == btnClear) {
			model.removeAllElements();
			textArea.setText("");
		}
		if (e.getSource() == btnCopy) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (copy != null && copy != "") {
				Transferable tf = new StringSelection(copy);
				clipboard.setContents(tf, null);
				JOptionPane.showMessageDialog(null, "成功复制到粘贴板");
			}
		}
	}

	// 解析二维码图片
	private String decodeImage(String file) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new File(file));
			QRCodeDecoder decoder = new QRCodeDecoder();
			decoded = new String(decoder.decode(new MyQRCodeImage(bufferedImage)));
			copy = decoded; // 将解析结果赋给copy
			// 如果检测到是 shadowsocks 的二维码，再对 decoded 做 base64 解码
			if (decoded.startsWith("ss://")) {
				String result = new String(Base64.getDecoder().decode(decoded.substring(5)));
				int i = result.indexOf("@");
				int j = result.indexOf(":");
				int k = result.lastIndexOf(":");
				String server = result.substring(i + 1, result.indexOf(":", i));
				String password = result.substring(j + 1, i);
				String port = result.substring(k + 1);
				String method = result.substring(0, j);
				StringBuilder builder = new StringBuilder();
				builder.append(decoded + "\n\n").append("服务器: " + server + "\n\n").append("远程端口: " + port + "\n")
						.append("密码: " + password + "\n\n").append("加密方法: " + method);
				decoded = builder.toString();
				historyMap.put(textField.getText(), decoded);
			}
			textArea.setText("解析结果：" + decoded);
			JOptionPane.showMessageDialog(null, "获取成功");
			return decoded;
		} catch (DecodingFailedException e) {
			JOptionPane.showMessageDialog(null, "您选择的不是二维码图片");
			e.printStackTrace();
			return "不是二维码图片";
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "失败";
	}
}
