package Game;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * This Loader - class os responsible for all processes that are repeatedly used with different values.
 * It handles and and prepares the data before sending it on to the GPU. All methods are called during the
 * initialization of the objects. The methods are called in the order they are displayed here.
 * Its purpose is to avoid redundant calls.
 *
 * @author Hanna Langenberg
 * @version 02.01.2021
 * */

public class Loader {

    com.jogamp.opengl.util.texture.Texture texture = null;
    int[] vao, vbo;

    /**
     * This method creates the Vertex Array Object (VAO) and the Vertex Buffer Object (VBO) with the transferred values.
     * Afterwards, it is checked if the VAO / VBO was loaded correctly before binding them.
     * Lastly for every iteration over the for-loop {@link Game.Loader#handleBufferData(GL3, float[], boolean)} is called.
     * @param gl OpenGL graphics context
     * @param noObjects The number of objects the VAO / VBO will have to contain
     * @param verticesObjects An ArrayList that contains the individual float-Arrays
     *                        -> further explanation see {@link Game.Loader#handleBufferData(GL3, float[], boolean)}
     * @param uv A parameter later needed in {@link Game.Loader#handleBufferData(GL3, float[], boolean)}
     * */
    public void handleInitData(GL3 gl, int noObjects, ArrayList<float[]> verticesObjects, boolean uv) {
        vao = new int[noObjects];
        vbo = new int[noObjects];

        gl.glGenVertexArrays(noObjects, vao, 0);
        gl.glGenBuffers(noObjects, vbo,0);

        for (int i = 0; i< vao.length; i++)
        {
            if (vao[i] < 1)
            {   System.err.println("Error allocating vertex array object (VAO).");  }

            if (vbo[i] < 1)
            {   System.err.println("Error allocating vertex buffer object (VBO).");   }

            gl.glBindVertexArray(vao[i]);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[i]);
            handleBufferData(gl, verticesObjects.get(i), uv);
        }
    }

    /**
     * This method has to be called in every iteration after binding the VAO / VBO as every float[] containing mesh
     * vertices has to be bound and the AttributePointers activated and mapped before moving on to the next float[].
     * The code could have been left in {@link Game.Loader#handleInitData(GL3, int, ArrayList, boolean)} but it would
     * make the code a little more confusing.
     * @param gl OpenGL graphics context
     * @param vertices float[] containing all vertices for the current object
     * @param uv If a texture is present or not. Needed to determine how the AttributePointers have to point.
     * */
    public void handleBufferData(GL3 gl, float[] vertices, boolean uv) {
        // Transferring the vertex data to the VBO on GPU.
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, // (floats: 4 bytes) -> size of the buffer
                FloatBuffer.wrap(vertices), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO in an interleaved layout
        if (uv)
        {
            // POSITION - 3
            gl.glEnableVertexAttribArray(0); // Enable layout position 0
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 8* Float.BYTES, 0);
            // TEXTURE COORDINATES - 2
            gl.glEnableVertexAttribArray(1);
            gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 8*Float.BYTES, 3*Float.BYTES);
            // NORMALS - 3
            gl.glEnableVertexAttribArray(2);
            gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 8*Float.BYTES, 5*Float.BYTES);
        }
        else
        {
            // POSITION - 3
            gl.glEnableVertexAttribArray(0); // Enable layout position 0
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6* Float.BYTES, 0);
            // NORMALS - 3
            gl.glEnableVertexAttribArray(1);
            gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6*Float.BYTES, 3*Float.BYTES);
        }
    }

    /**
     * Responsible for loading the textures and already setting parameters that all loaded textures share. Checks if the
     * texture was loaded correctly. It could print some information about the texture but rather for self - debugging
     * purposes. For now it is commented out.
     * @param gl OpenGL graphics context
     * @param folderPath The subfolder - path
     * @param fileName The name of the texture
     * */
    public Texture loadTexture(GL3 gl, String folderPath, String fileName) {
        try {
            File textureFile = new File(folderPath + fileName);
            texture = TextureIO.newTexture(textureFile, true);
            texture.setTexParameteri(gl, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
            texture.setTexParameteri(gl, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
        }
        catch (IOException e) {     e.printStackTrace();    }

        if(texture != null)
        {
            System.out.println(fileName + " - texture successfully loaded from: " + folderPath + fileName);
        }
        else {  System.err.println("Error loading texture.");   }

        /*System.out.println("  Texture height: " + texture.getImageHeight());
        System.out.println("  Texture width: " + texture.getImageWidth());
        System.out.println("  Estimated memory size of texture: " + texture.getEstimatedMemorySize());*/

        return texture;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTERS for the VAO / VBO
    public int[] getVao() {
        return vao;
    }

    public int[] getVbo() {
        return vbo;
    }
}