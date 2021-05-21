package com.parking.spring.scheduledtask;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageResizer {

   /**
    * Resizes an image to a absolute width and height (the image may not be
    * proportional)
    * @param scaledWidth absolute width in pixels
    * @param scaledHeight absolute height in pixels
    * @throws IOException
    */
   public static void resize(int scaledWidth, int scaledHeight)
         throws IOException {
      // reads input image
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      BufferedImage inputImage = ImageIO.read(classloader.getResourceAsStream("images/car.png"));

      // creates output image
      BufferedImage outputImage = new BufferedImage(scaledWidth,
            scaledHeight, inputImage.getType());

      // scales the input image to the output image
      Graphics2D g2d = outputImage.createGraphics();
      g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
      g2d.dispose();

      // writes to output file
      ImageIO.write(outputImage, "png", new File("resized.png"));
   }

   /**
    * Resizes an image by a percentage of original size (proportional).
    * @param percent a double number specifies percentage of the output image
    * over the input image.
    * @throws IOException
    */
   public static void resize(double percent) throws IOException {
      String inputImagePath = "images/car.png";
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      BufferedImage inputImage = ImageIO.read(classloader.getResourceAsStream(inputImagePath));
      int scaledWidth = (int) (inputImage.getWidth() * percent/100);
      int scaledHeight = (int) (inputImage.getHeight() * percent/100);
      resize(scaledWidth, scaledHeight);
   }

}
