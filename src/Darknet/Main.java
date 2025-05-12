package Darknet;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import Darknet.Darknet;

public class Main
{
	public static void main(String[] args) throws Throwable
	{
//		String path = args[0];														// Run from cmd
//		String path = "D:\\Users\\denis\\main\\imageLoaderAssets\\image1.png";		// Run from eclipse

//		buildFrame(path); //Build JFame to display image

		Darknet darknet = new Darknet();
		darknet.show_version_info();

		System.out.println(darknet.version_string());
		System.out.println(darknet.version_short());

		darknet.set_verbose(true);
		darknet.set_trace(false);

		var ptr = darknet.load_neural_network(
				"/home/stephane/nn/LegoGears/LegoGears.cfg"			,
				"/home/stephane/nn/LegoGears/LegoGears.names"		,
				"/home/stephane/nn/LegoGears/LegoGears_best.weights");

//		darknet.free_neural_network(ptr);
	}

	public static void buildFrame(String path) throws IOException
	{
//		"D:\\Users\\denis\\main\\imageLoaderAssets\\image1.png"

		JFrame frame = new JFrame("Image Viwer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1920, 1080);
		frame.setLocationRelativeTo(null);

		BufferedImage img = null;

			frame.getContentPane().removeAll();
			img = ImageIO.read(new File(path));

			JLabel label = new JLabel(new ImageIcon(img));
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
//			TimeUnit.SECONDS.sleep(2);

			BufferedImage image = ImageIO.read(new File(path));
			BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

			Graphics2D g = convertedImg.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();

			WritableRaster ConvertedImgRaster = convertedImg.getRaster();
			DataBufferByte dataBuffer = (DataBufferByte) ConvertedImgRaster.getDataBuffer();
			byte[] rawBytes = dataBuffer.getData();

			BufferedImage outputImage = new BufferedImage(convertedImg.getWidth(), convertedImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

			WritableRaster outputRaster = outputImage.getRaster();
			outputRaster.setDataElements(0, 0, convertedImg.getWidth(), convertedImg.getHeight(), rawBytes);

			ImageIO.write(outputImage, "png", new File("D:\\Users\\denis\\main\\imageLoaderAssets\\output.png"));

			Main imgLoad = new Main();
//			byte[] resultBytes = imgLoad.proccessImage(rawBytes, ConvertedImgRaster.getWidth(), ConvertedImgRaster.getHeight());



	}

}
