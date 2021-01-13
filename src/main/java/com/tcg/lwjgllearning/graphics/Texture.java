package com.tcg.lwjgllearning.graphics;

import com.tcg.lwjgllearning.utils.ArrayMap;
import com.tcg.lwjgllearning.utils.Disposable;
import com.tcg.lwjgllearning.utils.FileUtils;
import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL31.glActiveTexture;

public class Texture implements Disposable {

    private final static ArrayMap<Texture> boundTextures = new ArrayMap<>();

    private final static int DEFAULT_RESOLUTION = 1024;

    public final int width;
    public final int height;

    private final int textureId;
    private final int glTextureUnitIndex;

    public Texture(int width, int height, Format inputFormat, Format textureFormat, ByteBuffer buf) {
        this.textureId = glGenTextures();
        this.width = width;
        this.height = height;
        this.glTextureUnitIndex = boundTextures.add(this);
        this.createGLTexture(buf, inputFormat, textureFormat);
    }

    public Texture(int width, int height, Format format, ByteBuffer buf) {
        this(width, height, format, format, buf);
    }

    public Texture(int width, int height, Format format) {
        this(width, height, format, null);
    }

    public Texture(int res, Format format) {
        this(res, res, format);
    }

    public Texture(Format format) {
        this(DEFAULT_RESOLUTION, format);
    }

    public static Texture readImageFile(String filePath) {
        return readImageFile(filePath, false);
    }

    public static Texture readImageFile(String filePath, boolean flipped) {
        ByteBuffer buf = null;
        Texture texture;
        try {
            final PNGDecoder decoder = new PNGDecoder(FileUtils.getResourceAsStream(filePath));
            int width = decoder.getWidth();
            int height = decoder.getHeight();

            buf = MemoryUtil.memAlloc(4 * width * height);
            if (flipped) {
                decoder.decodeFlipped(buf, width * 4, PNGDecoder.Format.RGBA);
            } else {
                decoder.decode(buf, width * 4, PNGDecoder.Format.RGBA);
            }
            buf.flip();

            texture = new Texture (width, height, Format.RGBA, buf);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read image file.", e);
        } finally {
            if (buf != null) {
                MemoryUtil.memFree(buf);
            }
        }
        return texture;
    }

    public void activate(int samplerUniformLocation) {
        glUniform1i(samplerUniformLocation, this.glTextureUnitIndex);
    }

    public enum Format {
        RGBA(GL_RGBA),
        DEPTH(GL_DEPTH_COMPONENT);

        private final int value;

        private Format(int value) {
            this.value = value;
        }
    }

    private void createGLTexture(ByteBuffer buf, Format inputFormat, Format textureFormat) {
        glActiveTexture(GL_TEXTURE0 + this.glTextureUnitIndex);
        glBindTexture(GL_TEXTURE_2D, this.textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, inputFormat.value, this.width, this.height,
                0, textureFormat.value, GL_UNSIGNED_BYTE, buf);

        glActiveTexture(0);
    }

    @Override
    public void dispose() {
        boundTextures.remove(this.glTextureUnitIndex);
        glDeleteTextures(this.textureId);
    }
}
