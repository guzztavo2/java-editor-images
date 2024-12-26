import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Screen extends Canvas {

	private static final long serialVersionUID = 1L;
	static BufferedImage image;
	// int[] pixels = ((DataBufferInt)
	// (image.getRaster().getDataBuffer())).getData();

	BufferedImage importedImage;
	static JPanel imagePanel;

	public static BufferedImage scaleImage(BufferedImage originalImage, int targetWidth, int targetHeight,
			String mode) {

		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();

		double scaleX = (double) targetWidth / originalWidth;
		double scaleY = (double) targetHeight / originalHeight;

		double scale;
		int resizedWidth, resizedHeight;

		// Escolher a escala e dimensões com base no modo
		switch (mode.toLowerCase()) {
		case "contain":
			scale = Math.min(scaleX, scaleY);
			resizedWidth = (int) (originalWidth * scale);
			resizedHeight = (int) (originalHeight * scale);
			break;

		case "cover":
			scale = Math.max(scaleX, scaleY);
			resizedWidth = (int) (originalWidth * scale);
			resizedHeight = (int) (originalHeight * scale);
			break;

		case "fill":
			scale = 1.0;
			resizedWidth = targetWidth;
			resizedHeight = targetHeight;
			break;
		default:

			return originalImage;

		}

		BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

		// Centralizar a imagem redimensionada no caso de "contain" ou "cover"
		int offsetX = (targetWidth - resizedWidth) / 2;
		int offsetY = (targetHeight - resizedHeight) / 2;

		// Redimensionar pixel a pixel
		for (int y = 0; y < targetHeight; y++) {
			for (int x = 0; x < targetWidth; x++) {
				// Coordenadas na imagem original
				int srcX = (int) Math.floor((x - offsetX) / scale);
				int srcY = (int) Math.floor((y - offsetY) / scale);

				int rgb = 0x00000000; // Transparente por padrão
				if (srcX >= 0 && srcX < originalImage.getWidth() && srcY >= 0 && srcY < originalImage.getHeight()) {
					rgb = originalImage.getRGB(srcX, srcY);
				}

				// Setar o pixel na imagem de saída
				outputImage.setRGB(x, y, rgb);
			}
		}

		return outputImage;
	}

	static JPanel panel;

	static JButton btn = new JButton("Clique aqui para importar a imagem");

	static int actual_btn = 0;

	public static void addListenerButtons() {

		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				importImage(frame);

				imagePanel.repaint();
			}
		});

	}

	public static JFrame frame;

	public static void main(String[] args) {

		Screen screen = new Screen();
		frame = new JFrame("TESTE");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(900, 900));

		frame.add(screen);
		panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 20px de padding em todos os lados
		addListenerButtons();
		panel.add(btn, BorderLayout.PAGE_START);

		imagePanel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				try {
					g.setColor(Color.black);
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
					if (image != null) {

						// Centralizar a imagem no painel

						// BufferedImage convertedImage = convertToIntBufferedImage(image);

						image = changeBRGtoRGB(image);

						image = scaleImage(image, getWidth(), getHeight(), "cover");

						
						//image = zoomImage(image);
						// image = toGray(image);

						// image = Sobel(image);

						int imgWidth = image.getWidth();
						int imgHeight = image.getHeight();
						int x = (getWidth() - imgWidth) / 2;
						int y = (getHeight() - imgHeight) / 2;
						g.drawImage(image, x, y, this);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};

		panel.add(imagePanel);

		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);

		importManualImage();

	}

	private static void importManualImage() {
		try {
			image = ImageIO.read((new Screen()).getClass().getResource("/exemplo.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imagePanel.repaint();
	}

	private static BufferedImage Sobel(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];

		image.getRGB(0, 0, width, height, pixels, 0, width);

		int[][] Gx = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		int[][] Gy = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

		for (int y = 1; y < height - 1; y++)
			for (int x = 1; x < width - 1; x++) {
				int sumX = 0, sumY = 0;
				for (int j = -1; j <= 1; j++)
					for (int i = -1; i <= 1; i++) {
						int pixel = pixels[(x + i) + (y + j) * width] & 0xff;
						sumX += pixel * Gx[j + 1][i + 1];
						sumY += pixel * Gy[j + 1][i + 1];
					}
				int magnitude = (int) Math.sqrt(sumX * sumX + sumY * sumY);
				magnitude = Math.min(255, magnitude);

				pixels[x + y * width] = 255 << 24 | magnitude << 16 | magnitude << 8 | magnitude;
			}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	private static BufferedImage LaPlace(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];
		int[] result = new int[width * height];

		image.getRGB(0, 0, width, height, pixels, 0, width);
		int[][] laPlace = { { 0, -1, 0 }, { -1, 4, -1 }, { 0, -1, 0 } };

		for (int y = 1; y < height - 1; y++)
			for (int x = 1; x < width - 1; x++) {
				int sum = 0;

				for (int j = -1; j <= 1; j++)
					for (int i = -1; i <= 1; i++) {
						int pixel = pixels[(x + i) + (y + j) * width] & 0xff;

						int newJ = j + 1;
						int newI = i + 1;

						sum += pixel * laPlace[newJ][newI];
					}
				sum = Math.abs(sum);
				sum = Math.min(255, sum);

				result[x + y * width] = 255 << 24 | sum << 16 | sum << 8 | sum;

			}
		image.setRGB(0, 0, width, height, result, 0, width);
		return image;
	}

	private static BufferedImage setBlurImage(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];
		int[] blurredPixels = new int[width * height]; // Para armazenar a nova imagem

		int k = 5;
		int radius = k / 2;

		image.getRGB(0, 0, width, height, pixels, 0, width);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				float sumR = 0, sumG = 0, sumB = 0;
				int count = 0;

				for (int i = -radius; i <= radius; i++)
					for (int j = -radius; j <= radius; j++) {
						int nx = x + i;
						int ny = y + j;

						// Ignorar pixels fora dos limites
						if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
							int pixel = pixels[nx + ny * width];

							int red = (pixel >> 16) & 0xff;
							int green = (pixel >> 8) & 0xff;
							int blue = pixel & 0xff;

							sumR += red;
							sumG += green;
							sumB += blue;
							count++;
						}
					}

				int red = Math.min(255, Math.max(0, Math.round(sumR / count)));
				int green = Math.min(255, Math.max(0, Math.round(sumG / count)));
				int blue = Math.min(255, Math.max(0, Math.round(sumB / count)));

				blurredPixels[x + y * width] = (255 << 24) | (red << 16) | (green << 8) | blue;
			}
		image.setRGB(0, 0, width, height, blurredPixels, 0, width);

		return image;
	}

	public BufferedImage zoomImage(BufferedImage image, float zoom) {
		int width = Math.round(image.getWidth() * zoom);
		int height = Math.round(image.getHeight() * zoom);

		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int sourceX = Math.min(Math.round(x / zoom), image.getWidth() - 1);
				int sourceY = Math.min(Math.round(y / zoom), image.getHeight() - 1);

				int rgb = image.getRGB(sourceX, sourceY);

				newImage.setRGB(x, y, rgb);
			}

		return newImage;
	}

	private static BufferedImage addFilter(BufferedImage image, int redFilter, int greenFilter, int blueFilter) {
		float red = redFilter / 100f;
		float green = greenFilter / 100f;
		float blue = blueFilter / 100f;

		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];

		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int actual = x + y * width;
				int actualPixel = pixels[actual];

				int red_ = actualPixel >> 16 & 0xff;
				int green_ = actualPixel >> 8 & 0xff;
				int blue_ = actualPixel & 0xff;

				red_ = Math.round(Math.min(255, red_ * red));
				green_ = Math.round(Math.min(255, green_ * green));
				blue_ = Math.round(Math.min(255, blue_ * blue));

				pixels[actual] = 255 << 24 | red_ << 16 | green_ << 8 | blue_;

			}

		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	private static BufferedImage invertColor(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int actual = x + y * width;
				int actualPixel = pixels[actual];

				int red = actualPixel >> 16 & 0xff;
				int green = actualPixel >> 8 & 0xff;
				int blue = actualPixel & 0xff;

				red = Math.max(0, 255 - red);
				green = Math.max(0, 255 - green);
				blue = Math.max(0, 255 - blue);

				pixels[actual] = 255 << 24 | red << 16 | green << 8 | blue;
			}

		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	private static BufferedImage transform90(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];
		int[] invertedPixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int actual = x + y * width;

				int newX = y;
				int newY = width - 1 - x;

				int rotatedIndex = newX + newY * height;

				invertedPixels[rotatedIndex] = pixels[actual];
			}

		BufferedImage rotatedImage = new BufferedImage(height, width, image.getType());
		rotatedImage.setRGB(0, 0, height, width, invertedPixels, 0, height);
		return rotatedImage;
	}

	private static BufferedImage espelhar(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];
		int[] invertedPixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int actual = x + y * width;

				int newX = y;
				int newY = width - 1 - x;

				int rotatedIndex = newX + newY * height;

				invertedPixels[rotatedIndex] = pixels[actual];
			}

		BufferedImage rotatedImage = new BufferedImage(height, width, image.getType());
		rotatedImage.setRGB(0, 0, height, width, invertedPixels, 0, height);
		return rotatedImage;
	}

	private static BufferedImage setBrightness(BufferedImage image, int brightness) {
		float brightnessLevel = brightness / 10f;

		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];

		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int actual = x + y * width;
				int actualPixel = pixels[actual];
				int red = actualPixel >> 16 & 0xff;
				int green = actualPixel >> 8 & 0xff;
				int blue = actualPixel & 0xff;

				red = Math.min(255, Math.max(0, Math.round(red + brightnessLevel)));
				green = Math.min(255, Math.max(0, Math.round(green + brightnessLevel)));
				blue = Math.min(255, Math.max(0, Math.round(blue + brightnessLevel)));

				pixels[actual] = 255 << 24 | red << 16 | green << 8 | blue;
			}

		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	private static BufferedImage addSaturation(BufferedImage image, int factor) {
		float factor_ = factor / 100f;

		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];

		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int actual = x + y * width;
				int actualPixel = pixels[actual];

				int red = actualPixel >> 16 & 0xff;
				int green = actualPixel >> 8 & 0xff;
				int blue = actualPixel & 0xff;

				float luminosity = 0.3f * red + 0.59f * green + 0.11f * blue;

				red = Math.min(255, Math.max(0, Math.round(luminosity + (red - luminosity) * factor_)));
				green = Math.min(255, Math.max(0, Math.round(luminosity + (green - luminosity) * factor_)));
				blue = Math.min(255, Math.max(0, Math.round(luminosity + (blue - luminosity) * factor_)));

				pixels[actual] = 255 << 24 | red << 16 | green << 8 | blue;
			}

		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	private static BufferedImage grayFilter(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int actual = x + y * width;
				int actualPixel = pixels[actual];
				if (actualPixel == 0)
					continue;

				int alpha = actualPixel >> 24 & 0xff, red = actualPixel >> 16 & 0xff, green = actualPixel >> 8 & 0xff,
						blue = actualPixel & 0xff;

				int gray = Math.min(255, Math.max(0, Math.round(0.3f * red + 0.59f * green + 0.11f * blue)));

				pixels[actual] = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
			}
		image.setRGB(0, 0, width, height, pixels, 0, width);

		return image;
	}

	private static BufferedImage toGray(BufferedImage image) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++) {
				int actualPixel = pixels[x + y * image.getWidth()];

				int red = actualPixel >> 16 & 0xff;
				int green = actualPixel >> 8 & 0xff;
				int blue = actualPixel & 0xff;

				int grayColor = (red + green + blue) / 3;

				pixels[x + y * image.getWidth()] = 255 << 24 | grayColor << 16 | grayColor << 8 | grayColor;
			}

		image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		return image;
	}

	private static BufferedImage changeBRGtoRGB(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();
		BufferedImage final_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int pixel = image.getRGB(x, y);
				int alpha = pixel >> 24 & 0xff;
				int red = pixel >> 16 & 0xff;
				int green = pixel >> 8 & 0xff;
				int blue = pixel & 0xff;
				final_image.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
			}
		}

		return final_image;
	}

	private static BufferedImage changeOpacity(BufferedImage originalImage, int value) {

		BufferedImage image = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		value = Math.min(100, Math.max(0, value));
		float factor = value / 100f;
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++) {

				int actualPixel = originalImage.getRGB(x, y);

				int red = actualPixel >> 16 & 0xff;
				int green = actualPixel >> 8 & 0xff;
				int blue = actualPixel & 0xff;

				int newRed = Math.round(factor * red + (1 - factor) * 0);
				int newGreen = Math.round(factor * green + (1 - factor) * 0);
				int newBlue = Math.round(factor * blue + (1 - factor) * 0);

				image.setRGB(x, y, 255 << 24 | newRed << 16 | newGreen << 8 | newBlue);
			}

		return image;
	}

	private static BufferedImage pixelateColors(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();

		int[] pixels = new int[width * height];

		int factor = 3;

		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int y = 0; y < height; y += factor)
			for (int x = 0; x < width; x += factor) {
				int red = 0, green = 0, blue = 0, count = 0;

				for (int yy = y; yy < y + factor && yy < height; yy++)
					for (int xx = x; xx < x + factor && xx < width; xx++) {
						int actual = xx + yy * width;
						int pixel = pixels[actual];

						red += pixel >> 16 & 0xff;
						green += pixel >> 8 & 0xff;
						blue += pixel & 0xff;
						count++;
					}

				red /= count;
				green /= count;
				blue /= count;

				for (int yy = y; yy < y + factor && yy < height; yy++)
					for (int xx = x; xx < x + factor && xx < width; xx++) {
						int actual = xx + yy * width;
						int pixel = pixels[actual];

						pixels[actual] = 255 << 24 | red << 16 | green << 8 | blue;
					}
			}
		image.setRGB(0, 0, width, height, pixels, 0, width);

		return image;
	}

	private static BufferedImage separarCanais(BufferedImage originalImage, String color) {
		int originalWidth = originalImage.getWidth(), originalHeight = originalImage.getHeight();

		int[] pixels = new int[originalWidth * originalHeight];

		originalImage.getRGB(0, 0, originalWidth, originalHeight, pixels, 0, originalWidth);

		switch (color) {
		case "red":

			for (int x = 0; x < originalWidth; x++) {
				for (int y = 0; y < originalHeight; y++) {
					int rgb = pixels[x + y * originalWidth];

					int alpha = rgb >> 24 & 0xff;
					int red = rgb >> 16 & 0xff;

					pixels[x + y * originalWidth] = alpha << 24 | red << 16 | 0 << 8 | 0;
				}
			}
			break;
		case "green":

			for (int x = 0; x < originalWidth; x++) {
				for (int y = 0; y < originalHeight; y++) {
					int rgb = pixels[x + y * originalWidth];

					int alpha = rgb >> 24 & 0xff;
					int green = rgb >> 8 & 0xff;

					pixels[x + y * originalWidth] = alpha << 24 | 0 << 16 | green << 8 | 0;
				}
			}
			break;
		case "blue":
			for (int x = 0; x < originalWidth; x++) {
				for (int y = 0; y < originalHeight; y++) {
					int rgb = pixels[x + y * originalWidth];

					int alpha = rgb >> 24 & 0xff;
					int blue = rgb & 0xff;

					pixels[x + y * originalWidth] = alpha << 24 | 0 << 16 | 0 << 8 | blue;
				}
			}
			break;
		}

		originalImage.setRGB(0, 0, originalWidth, originalHeight, pixels, 0, originalWidth);
		return originalImage;
	}

	private static File file;

	private static void importImage(JFrame frame) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.resetChoosableFileFilters();
		FileFilter ff = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				else if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png"))
					return true;
				else
					return false;
			}

			public String getDescription() {
				return "Images files";
			}
		};
		fileChooser.setFileFilter(ff);
		int result = fileChooser.showOpenDialog(frame);

		if (result == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			if (!file.getName().endsWith(".jpg") && !file.getName().endsWith(".png")) {
				String actualFormat = file.getName().substring((file.getName().indexOf(".", 0) + 1)).toUpperCase();
				JOptionPane.showMessageDialog(frame,
						"A imagem tem que ser em JPG ou em PNG. O formato está em: " + actualFormat + ".");

				return;
			}
			try {
				// Tenta carregar a imagem
				image = ImageIO.read(file);
				if (image != null) {
					JOptionPane.showMessageDialog(frame, "Imagem carregada com sucesso!");
					// Processar a imagem aqui, se necessário
				} else {
					JOptionPane.showMessageDialog(frame, "Erro ao carregar a imagem.");
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "Erro ao abrir o arquivo: " + e.getMessage());
			}

		}
	}

}
