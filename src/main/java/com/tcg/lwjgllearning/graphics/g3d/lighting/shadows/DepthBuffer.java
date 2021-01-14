package com.tcg.lwjgllearning.graphics.g3d.lighting.shadows;

import com.tcg.lwjgllearning.graphics.ShaderProgram;
import com.tcg.lwjgllearning.graphics.Texture;
import com.tcg.lwjgllearning.graphics.g3d.mesh.Mesh;
import com.tcg.lwjgllearning.math.Matrix4;
import com.tcg.lwjgllearning.math.Vector3;
import com.tcg.lwjgllearning.utils.FileUtils;

import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class DepthBuffer extends Texture {

    protected final static ShaderProgram DEPTH_SHADER = ShaderProgram.buildShader(
            FileUtils.readFile("src/main/java/com/tcg/lwjgllearning/graphics/g3d/lighting/shadows/vert.depth.glsl"),
            FileUtils.readFile("src/main/java/com/tcg/lwjgllearning/graphics/g3d/lighting/shadows/frag.depth.glsl")
    );
    protected final static int DRAW_TRANSFORM_UNIFORM_LOCATION =
            DEPTH_SHADER.getUniformLocation("modelToShadowSpace");
    protected final static int DRAW_POSITION_ATTRIB_LOCATION =
            DEPTH_SHADER.getAttribLocation("vertPosition");
    protected final static HashMap<Mesh, MeshInfo> MESH = new HashMap<>();

    private Matrix4 view;
    private Matrix4 projection;
    private Matrix4 combined;

    private final int fboId;
    private final int vaoId;

    private boolean needsUpdate;

    public DepthBuffer(Vector3 position, Vector3 forward, Vector3 up, Matrix4 projection, int width, int height) {
        super(width, height, Format.DEPTH);

        this.fboId = glGenFramebuffers();
        this.configureFramebuffer();

        this.vaoId = glGenVertexArrays();

        this.setView(position, forward, up);
        this.setProjection(projection);

        this.needsUpdate = true;
    }

    public DepthBuffer(Vector3 position, Vector3 forward, Vector3 up, Matrix4 projection) {
        this(position, forward, up, projection, DEFAULT_RESOLUTION, DEFAULT_RESOLUTION);
    }

    public DepthBuffer(int width, int height) {
        this(new Vector3(0, 0, 0), new Vector3(0, -1, 0), new Vector3(0, 0, 1),
                Matrix4.orthographic(-10, 10, -10, 10, 10, -10),
                width, height);
    }

    public DepthBuffer(int res) {
        this(new Vector3(0, 0, 0), new Vector3(0, -1, 0), new Vector3(0, 0, 1),
                Matrix4.orthographic(-10, 10, -10, 10, 10, -10),
                res, res);
    }

    // sunlight at noon
    public DepthBuffer() {
        this(new Vector3(0, 0, 0), new Vector3(0, -1, 0), new Vector3(0, 0, 1),
                Matrix4.orthographic(-10, 10, -10, 10, 10, -10));
    }

    public void setView(Vector3 position, Vector3 forward, Vector3 up) {
        this.view = Matrix4.view(position, position.addOutPlace(forward), up);
        this.needsUpdate = true;
    }

    public void setProjection(Matrix4 projection) {
        this.projection = projection;
        this.needsUpdate = true;
    }

    public void updateTransform() {
        if (this.needsUpdate) {
            this.combined = Matrix4.mul(this.projection, this.view);
            this.needsUpdate = false;
        }
    }

    private void configureFramebuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.fboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.textureId, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
    }

    public void draw(List<Mesh> meshList) {
        // assume shader bound by light manager
        this.updateTransform();
        glBindFramebuffer(GL_FRAMEBUFFER, this.fboId);
        glClear(GL_DEPTH_BUFFER_BIT);
        for (Mesh mesh : meshList) {
            this.draw(mesh);
        }
    }

    private void draw(Mesh mesh) {
        glUniformMatrix4fv(DRAW_TRANSFORM_UNIFORM_LOCATION,
                false, Matrix4.mul(this.combined, mesh.worldTransform()).asArray());
        int indexLength = bind(mesh);
        glDrawElements(GL_TRIANGLES, indexLength, GL_UNSIGNED_INT,0);
    }

    public static void bindDepthShader() {
        DEPTH_SHADER.bind();
    }

    private static int bind(Mesh mesh) {
        MeshInfo meshInfo = MESH.get(mesh);
        glBindVertexArray(meshInfo.vaoId);
        return meshInfo.indexLength;
    }

    public static void add(Mesh mesh) {
        if (MESH.get(mesh) == null) {
            MESH.put(mesh, new MeshInfo(mesh));
        }
    }

    private static class MeshInfo {
        protected final int vaoId;
        protected final int indexLength;

        protected MeshInfo(Mesh mesh) {
            this.vaoId = glGenVertexArrays();
            glBindVertexArray(this.vaoId);
            mesh.activateAttribute(Mesh.Attribute.POSITION, DRAW_POSITION_ATTRIB_LOCATION);
            this.indexLength = mesh.bindIndexBuffer();
            glBindVertexArray(GL_NONE);
        }
    }
}
