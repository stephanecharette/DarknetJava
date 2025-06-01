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

		// All output from Darknet/YOLO can be sent to a file.
		// Otherwise, without calling this, output will be sent to the console.
//		darknet.set_output_stream("darknet_output.txt");

		// Use a blank filename to reset it to the default console output.
//		darknet.set_output_stream("");

		// Ask Darknet/YOLO to print a few lines with version strings from CUDA, ROCm, OpenCV, and Darknet/YOLO itself.
		darknet.show_version_info();

		// The "long" version string includes the git hash, such as "v5.0-23-gfe0f6639".
		System.out.format("LONG VERSION STRING .... %s%n", darknet.version_string());

		// The "short" version string includes only the major, minor, and patch values, such as "5.0.23".
		System.out.format("SHORT VERSION STRING ... %s%n", darknet.version_short());

		// Tell Darknet/YOLO to display extended information.
		darknet.set_verbose(false);

		// Load the specified neural network.  The order in which the 3 required files is given does not matter,
		// Darknet should figure out from the file size and content which file is the .cfg, .names, and .weights.
		var ptr = darknet.load_neural_network(
				"/home/stephane/nn/LegoGears/LegoGears.cfg"			,
				"/home/stephane/nn/LegoGears/LegoGears.names"		,
				"/home/stephane/nn/LegoGears/LegoGears_best.weights");

		// Tell Darknet/YOLO to display trace information.  If set to "true", this will also turn on "verbose" output.  This is only meant for debugging.
		darknet.set_trace(true);
		darknet.set_detection_threshold(ptr, 0.2f);
		darknet.set_non_maximal_suppression_threshold(ptr, 0.45f);
		darknet.network_dimensions(ptr).forEach((key, value) -> System.out.format("-> network dimensions: %s=%d%n", key, value));

		System.out.format("NEURAL NETWORK PTR ..... %s%n", ptr);

		// ...do Darknet stuff here...

		// Once done with the network, remember to free the neural network pointer.
		System.out.format("FREE NETWORK POINTER ... %s%n", ptr);
		darknet.free_neural_network(ptr);
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
