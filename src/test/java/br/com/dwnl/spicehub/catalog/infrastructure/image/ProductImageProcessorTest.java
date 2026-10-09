
package br.com.dwnl.spicehub.catalog.infrastructure.image;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ProductImageProcessorTest {

    private final ProductImageProcessor processor = new ProductImageProcessor();

    @Test
    void shouldConvertPngToWebP() throws Exception {
        BufferedImage original = new BufferedImage(1600, 900, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = original.createGraphics();

        try {
            graphics.setColor(Color.GREEN);
            graphics.fillRect(0, 0, 1600, 900);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream input = new ByteArrayOutputStream();

        assertTrue(ImageIO.write(original, "png", input));

        byte[] result = processor.process(input.toByteArray());

        assertNotNull(result);
        assertTrue(result.length > 0);

        assertEquals("RIFF", new String(result, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));

        assertEquals("WEBP", new String(result, 8, 4, java.nio.charset.StandardCharsets.US_ASCII));

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(result));

        assertNotNull(decoded);
        assertEquals(1200, decoded.getWidth());
        assertEquals(675, decoded.getHeight());
    }
}
