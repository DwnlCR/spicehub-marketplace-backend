
package br.com.dwnl.spicehub.catalog.infrastructure.image;

import br.com.dwnl.spicehub.catalog.domain.image.ImageProcessor;
import br.com.dwnl.spicehub.catalog.infrastructure.exception.InvalidProductImageException;
import br.com.dwnl.spicehub.catalog.infrastructure.exception.ProductImageProcessingException;
import org.springframework.stereotype.Component;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Component
public class ProductImageProcessor implements ImageProcessor {

    private static final int MAX_DIMENSION = 1200;
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000L;

    private static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "jpg", "png", "webp");

    private final static float WEBP_QUALITY = 0.80f;

    @Override
    public byte[] process(byte[] image) {
        if (image == null || image.length == 0) {
            throw new InvalidProductImageException(
                    "Image cannot be empty"
            );
        }

        if (image.length > MAX_FILE_SIZE) {
            throw new InvalidProductImageException("Image exceeds the maximum size of 5 MB");
        }

        try {
            BufferedImage original = readImage(image);

            int width = original.getWidth();
            int height = original.getHeight();

            double scale = Math.min(1.0, (double) MAX_DIMENSION / Math.max(width, height)
            );

            int targetWidth = Math.max(1, (int) Math.round(width * scale));

            int targetHeight = Math.max(1, (int) Math.round(height * scale));

            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = resized.createGraphics();

            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, targetWidth, targetHeight);

                graphics.drawImage(original, 0, 0, targetWidth, targetHeight, null);

            } finally {
                graphics.dispose();
            }

            return convertToWebP(resized);

        } catch (IOException exception) {
            throw new ProductImageProcessingException("Failed to process image", exception);
        }
    }

    private BufferedImage readImage(byte[] image) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image)
        )) {
            if (input == null) {throw new InvalidProductImageException("Unable to read image");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {throw new InvalidProductImageException("Invalid or unsupported image");}

            ImageReader reader = readers.next();

            try {
                reader.setInput(input);

                String format = reader.getFormatName().toLowerCase(Locale.ROOT);

                if (!ALLOWED_FORMATS.contains(format)){
                    throw new InvalidProductImageException(
                            "Only JPEG, PNG and WebP images are allowed"
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0) {
                    throw new InvalidProductImageException("Invalid image dimensions");
                }

                long totalPixels = (long) width * height;

                if (totalPixels > MAX_PIXELS) {
                    throw new InvalidProductImageException("Image exceeds the maximum resolution of 20 megapixels");
                }

                BufferedImage decoded = reader.read(0);

                if (decoded == null) {
                    throw new InvalidProductImageException("Unable to decode image");
                }

                return decoded;

            } finally {
                reader.dispose();
            }
        }
    }

    private byte[] convertToWebP(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");

        if (!writers.hasNext()) {
            throw new ProductImageProcessingException("WebP encoder is not available");
        }

        ImageWriter writer = writers.next();

        try (
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)
        ) {
            if (imageOutput == null) {
                throw new ProductImageProcessingException("Unable to create image output stream");
            }

            writer.setOutput(imageOutput);

            ImageWriteParam writeParam = writer.getDefaultWriteParam();

            if (writeParam.canWriteCompressed()){
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            }

            String[] compressionTypes = writeParam.getCompressionTypes();

            if (compressionTypes != null){
                for (String type : compressionTypes){
                    if ("Lossy".equalsIgnoreCase(type)){
                        writeParam.setCompressionType(type);
                        break;
                    }
                }
            }
            writeParam.setCompressionQuality(WEBP_QUALITY);

            writer.write(null, new IIOImage(image, null, null), writeParam);

            imageOutput.flush();

            return output.toByteArray();

        } finally {
            writer.dispose();
        }
    }
}
