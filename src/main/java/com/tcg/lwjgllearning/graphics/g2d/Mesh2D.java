package com.tcg.lwjgllearning.graphics.g2d;

import com.tcg.lwjgllearning.graphics.Drawable;
import com.tcg.lwjgllearning.graphics.ShaderProgram;
import com.tcg.lwjgllearning.math.Transform2D;
import com.tcg.lwjgllearning.math.Vector2;
import com.tcg.lwjgllearning.utils.Disposable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public abstract class Mesh2D extends Transform2D implements Disposable, Drawable {

    protected final ShaderProgram shaderProgram;
    protected final float[] positionArray;
    protected final int[] indexArray;

    protected int vaoId;
    protected int positionAttribLocation;
    protected int mWorldUniformLocation;
    private int positionVboId;
    private int indexVboId;

    public Mesh2D(ShaderProgram shaderProgram, float[] positionArray, int[] indexArray, Vector2 position, float rotation, Vector2 scale) {
        super(position, rotation, scale);
        this.shaderProgram = shaderProgram;
        this.positionArray = Arrays.copyOf(positionArray, positionArray.length);
        this.indexArray = Arrays.copyOf(indexArray, indexArray.length);
        this.createBuffers();
    }

    public Mesh2D(ShaderProgram shaderProgram, float[] positionArray, int[] indexArray) {
        this(shaderProgram, positionArray, indexArray, new Vector2(), 0f, new Vector2(1, 1));
    }

    private void createBuffers() {
        FloatBuffer positionBuffer = null;
        IntBuffer indexBuffer = null;

        try {
            this.mWorldUniformLocation = this.shaderProgram.getUniformLocation("mWorld");

            this.vaoId = glGenVertexArrays();
            glBindVertexArray(this.vaoId);

            positionBuffer = MemoryUtil.memAllocFloat(this.positionArray.length);
            positionBuffer.put(this.positionArray).flip();

            this.positionVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, this.positionVboId);
            glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
            this.positionAttribLocation = this.shaderProgram.getAttribLocation("vertPosition");
            glEnableVertexAttribArray(this.positionAttribLocation);
            glVertexAttribPointer(this.positionAttribLocation, 2, GL_FLOAT, false, 0, 0);

            indexBuffer = MemoryUtil.memAllocInt(this.indexArray.length);
            indexBuffer.put(this.indexArray).flip();

            this.indexVboId = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

            glBindVertexArray(0);


        } finally {
            if (positionBuffer != null) {
                MemoryUtil.memFree(positionBuffer);
            }
            if (indexBuffer != null) {
                MemoryUtil.memFree(indexBuffer);
            }
        }
    }

    public void activate() {
        this.update();

        this.shaderProgram.bind();

        glBindVertexArray(this.vaoId);

        glUniformMatrix3fv(this.mWorldUniformLocation, false, this.worldMatrix());


    }

    public void draw() {
        this.activate();
        glDrawElements(GL_TRIANGLES, this.indexArray.length, GL_UNSIGNED_INT, 0);
        this.deactivate();
    }

    public void deactivate() {
        glBindVertexArray(0);
        this.shaderProgram.unbind();
    }

    @Override
    public void dispose() {
        glDeleteBuffers(this.positionVboId);
        glDeleteBuffers(this.indexVboId);
        glDeleteVertexArrays(this.vaoId);
    }
}
