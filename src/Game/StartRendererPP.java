package Game;

/*
 * Copyright 2012-2013 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

import ComputerVision.Vision;
import GUI.Main;
import GUI.StartController;
import Game.Beauty.LightSource;
import Game.Beauty.ShaderProgram;
import Game.Interactions.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.PMVMatrix;
import de.hshl.obj.loader.OBJLoader;
import de.hshl.obj.loader.Resource;
import de.hshl.obj.loader.materials.Material;
import de.hshl.obj.loader.materials.Texture;
import de.hshl.obj.loader.objects.MeshObject;
import de.hshl.obj.loader.objects.Surface;
import de.hshl.obj.loader.objects.SurfaceObject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.jogamp.opengl.GL.*;

/**
 * Performs the OpenGL rendering
 * Uses the programmable pipeline commands in the core profile only.
 * As multiple Objs with different materials and textures are loaded
 * multiple vertex and fragment shaders are used.
 *
 * The game is based on the children's game "Perplexus".
 * So a labyrinth, a ball and a background (for aesthetic purposes only) are loaded.
 *
 * Initially the Background is scaled and translated but all further transformations ocurring during runtime are limited
 * to the labyrinth and the ball.
 * The labyrinth is rotated along the X and Z axis, which the ball follows respectively. Plus, the ball translates in
 * X and Z direction depending on the the labyrinth's current angle.
 * The ball's area of free movement is limited to the labyrinth's dimension to prevent it from going through the side walls
 * like ghosts from Ghostbusters.
 *
 * Currently the labyrinth is moved using the arrow keys(left/right/up/down)
 * As this is only for temporary testing purposes, mouse movements are not considered.
 *
 * The by Prof. Dr. Merijam Gotzes and Prof. Dr.-Ing. Karsten Lehn provided "JoglModelLoadGeometryPP" - startcode was based on:
 * Based on a tutorial by Chua Hock-Chuan
 * http://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL2.0.html
 *
 * and on an example by Xerxes Rånby
 * http://jogamp.org/git/?p=jogl-demos.git;a=blob;f=src/demos/es2/RawGL2ES2demo.java;hb=HEAD
 *
 * @author Karsten Lehn
 * @version 3.9.2015, 15.9.2015, 18.9.2015, 10.9.2017, 2.10.2018, 7.10.2018
 *
 * Further changes and adaptions for the game done by
 * @author Hanna Langenberg
 * @version 30.12.2020
 */
public class StartRendererPP extends GLCanvas implements GLEventListener {

    private static final long serialVersionUID = 1L;

    // -----------------------------------------------------------------------------------------------------------------
    // Defining later needed file paths

    final static String resourcePath = "./resources/";              // Subfolder substring needed for objs and shaders
    final static String objSub = resourcePath + "models/";          // Folder string needed for the objs

    final String vsKarl = "Karl.vert";                              // Filenames for the vertex and fragment shaders
    final String fsKarl = "Karl.frag";
    final String vsTexture = "Texture.vert";
    final String fsTexture = "Texture.frag";

    /*
    * Labyrinth_lvl0 is a basic board with walls and a hole in the groundplane. Used to start low and advance
    * from thereon.
    * Labyrinth lvl 1 contains walls and 2-3 contain further bumps on the ground.
    * The Axis Aligned Bounding Boxes are constructed correctly and the collision detection is itself is working
    * properly. But Karl's position is not corrected nicely. Karl glitches somehow around.
    * */
    private String objFilePath;                                     // Labyrinth (variable path)
    private String bbFilePath;                                      // Labyrinth BB (variable path)
    final static String objKarl = "Karl";                           // Karl
    final static String objGraveyard = "Island2";                   // Island

    // final paths to the Obj-Files for Karl and Island (labyrnths are explicitly constructed when needed)
    private final Path objFileKarl = Paths.get(objSub + objKarl+ ".obj");
    private final Path objFileIsland = Paths.get(objSub + objGraveyard + ".obj");

    int[] vaoLabyrinth, vaoKarl, vaoIsland, vboLabyrinth, vboKarl, vboIsland;           //Arrays for VAO / VBO
    float[] vertLabyrinth, vertKarl, vertBoat, vertPalmColors, vertOcean;               //Arrays for geometry data
    int counter = 0;                                                                    //counter -> accomplished levels
    float xHolePosition;                                                        //to set the position of the hole in the
    float zHolePosition;                                                        //labyrinth -> different per level

    private ShaderProgram spKarl, spTexture;                     //to load shaders & create a shader program
    private LightSource light;                                               //Create lights
    StartController     startController     = new StartController();         //to access the class - specific methods
    AABB                aabbInstance        = new AABB();
    CollisionDetection  collisionDetection  = new CollisionDetection();
    Loader              loader              = new Loader();
    Stoppuhr            su                  = new Stoppuhr();
    Movement            movement;                                            //to handle everything movement - related

    PMVMatrix pmvMatrix;                                   //Declaration for using the projection-model-view matrix tool

    com.jogamp.opengl.util.texture.Texture
                textureLab, textureBoat, texturePalmColors, textureOcean = null; //individual materials and textures for
    Material    matLab,     matBoat,     matPalmColors,     matOcean = null;     //each object. Texture(jogamp),
    Texture     texLab,     texBoat,     texPalmColors,     texOcean = null;     //Material (hshl) and Texture (hshl)

    ArrayList<float[]>  alWallVertArr = new ArrayList<>();     //contains float[] storing vertex data for each AABB wall
    ArrayList<AABB>     alAABB        = new ArrayList<>();     //contains the AABBs constructed from the AABB walls


    /**
     * Standard constructor for object creation.
     */
    public StartRendererPP() {
        super();                                // Create the OpenGL canvas with default capabilities
        this.addGLEventListener(this);          // Add this object as OpenGL event listener to the canvas
        createAndRegisterMovementHandler();
    }

    /**
     * Create the canvas with the requested OpenGL capabilities
     * @param capabilities The capabilities of the canvas, including the OpenGL profile
     */
    public StartRendererPP(GLCapabilities capabilities) {
        super(capabilities);                    // Create the OpenGL canvas with the requested OpenGL capabilities
        this.addGLEventListener(this);          // Add this object as an OpenGL event listener to the canvas
        createAndRegisterMovementHandler();
    }

    /**
     * Helper method for creating a movement handler object and registering it.
     * Currently for key press call backs.
     */
    private void createAndRegisterMovementHandler() {
        movement = new Movement();
        this.addKeyListener(movement);
    }

    /**
     * Transfers specific values for ObjFilePath, BbFilePath, xHolePosition and zHolePosition, based
     * on the counter, to a method that sets them to the global variables.
     * @param counter represents the level to be loaded.
     * */
    public void setVariables(int counter) {
        switch (counter) {
            case 0:
                setNewLvlValues("Labyrinth_0", "Labyrinth_0_BB", -10f, -5f);
                break;
            case 1:
                setNewLvlValues("Labyrinth_1", "Labyrinth_1_BB", 5f, -10.5f);
                break;
            case 2:
                setNewLvlValues("Labyrinth_2", "Labyrinth_2_BB", 6f, -10.5f);
                break;
            case 3:
                setNewLvlValues("Labyrinth_3", "Labyrinth_3_BB", -10.5f, -5.3f);
                break;
        }
    }

    /**
     * Continuative method of setVariables. This method actually sets the new parameters.
     * These four would have to be called in every case with different parameters so I put them into an exra method to
     * avoid duplicates.
     * @param obj The name of the Obj - File
     * @param bb  The name of the BB - File
     * @param xPos The x - Position of the hole in the ground
     * @param zPos The z - Position of the hole in the ground
     * */
    private void setNewLvlValues(String obj, String bb, float xPos, float zPos) {
        setObjFilePath(obj);
        setBbFilePath(bb);
        setxHolePosition(xPos);
        setzHolePosition(zPos);
    }

    /**
     * Called every time a new level has to be loaded. It resets the board and Karl to their initial situations.
     * In this case the values can be hard - coded as they will always be the same, though the style is not as nice.
     * */
    public void resetMovementValues() {
        movement.setxKarl(-10.5f);
        movement.setzKarl(10.5f);
        movement.setyKarl(0f);
        movement.setHomerun(false);
        movement.setAngleXaxis(0);
        movement.setAngleZaxis(0);
    }

    //------------------------------------------------------------------------------------------------------------------
    // INITIALIZATION
    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * that is called when the OpenGL renderer is started for the first time.
     * It prepares the variables needed for the initialization of the individual objects and prepares them for
     * later portrayal.
     * @param drawable The OpenGL drawable
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();             //Retrieve the OpenGL graphics context

        // Outputs information about the available and chosen profile
            /*System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
            System.err.println("INIT GL IS: " + gl.getClass().getName());
            System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
            System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
            System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));*/

        /*
        * The initializations of the single objects are done in their own methods as every object is constructed
        * via Obj-File parsing. And each Obj-File is parsed differently to retrieve different information.
        * */
        spTexture = new ShaderProgram(gl);                          // create a ShaderProgram with the Labyrinth's
        spTexture.loadShaderAndCreateProgram(resourcePath, vsTexture, fsTexture);          // vertex and fragment shader
        spKarl = new ShaderProgram(gl);
        spKarl.loadShaderAndCreateProgram(resourcePath, vsKarl, fsKarl);

        setVariables(counter);
        initLabyrinth(gl, objFilePath);
        initLabyrinthBB(gl, bbFilePath);
        initKarl(gl);
        initIsland(gl);

        pmvMatrix = new PMVMatrix();                     //Create an object for projection-model-view matrix calculation

        // Specify light parameters
        float[] lightPosition = {0.0f, 300.0f, 300.0f, 100.0f};
        float[] lightAmbientColor = {0.2f, 0.2f, 0.2f, 1.0f};
        float[] lightDiffuseColor = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightSpecularColor = {1.0f, 1.0f, 1.0f, 1.0f};
        light = new LightSource(lightPosition, lightAmbientColor,
                                 lightDiffuseColor, lightSpecularColor);

        gl.glEnable(GL.GL_CULL_FACE);  gl.glCullFace(GL.GL_BACK);           //Switch on back face culling

        gl.glEnable(GL.GL_DEPTH_TEST);                                      //Switch on depth test.

        gl.glClearColor(0.54f, 0.71f, 0.79f, 1.0f);         //BG color of GLCanvas -> pacific blue

        su.starten();
    }
    /**
     * Initializes the labyrinths
     * A Labyrinth is loaded using the Obj-Loader and loadSurfaces.
     * Loaded: Vertices, Normals, Texture Coordinates and Materials per surface
     * @param gl OpenGL graphics context
     * */
    private void initLabyrinth(GL3 gl, String objFilePath) {
        try
        {
            Surface labyrinth = new OBJLoader()
                    .setLoadNormals(true)                           // load normal data
                    .setLoadTextureCoordinates(true)                // load the texture coordinates but ignore any third
                    .setIgnoreThirdTextureCoordinate(true)          // coordinates if present
                    .loadSurfaces(Resource.file(Paths.get(objSub + objFilePath + ".obj")))    // load the file
                    .get(0);
            vertLabyrinth = labyrinth.getMesh().getVertices();      // take vertices from loaded mesh
            matLab = labyrinth.getMaterial();                       // get the labyrinth's material
            texLab = matLab.getDiffuseMap();                        // extract the DiffuseMap -> texture file
        }
        catch (IOException e) {     throw new RuntimeException(e);      }

        ArrayList<float[]> vertObjects = new ArrayList<>();         // store vertices in an ArrayList which is passed to
        vertObjects.add(vertLabyrinth);                             // handleInitData

        loader.handleInitData(gl, vertObjects.size(), vertObjects, true); // initializes the VAO and VBO, checks if
                                                                    // loaded correctly and maps the interleaved input
        vaoLabyrinth = loader.getVao();                             //retrieve VAO
        vboLabyrinth = loader.getVbo();                             //retrieve VBO

        textureLab = loader.loadTexture(gl, objSub, texLab.getFileName());        // Load and prepare texture
        textureLab.enable(gl);                                        // enable the texture in order to make it possible
                                                                      // to sent it to the fragment shader later
        movement.setxHolePosition(xHolePosition);                     // pass the hole's position to movement
        movement.setzHolePosition(zHolePosition);
    }
    /**
     * Initializes the vertices for labyrinth's bounding boxes.
     * The BBs are loaded using the Obj-Loader and loadMeshObjects as indexed meshes. The indices are ignored as the BBs
     * will not be displayed.
     * Loaded: only indexed Vertices
     * @param gl OpenGL graphics context
     * */
    private void initLabyrinthBB(GL3 gl, String bbFilePath){
        try
        {
            List<MeshObject> objects = new OBJLoader()
                    .setGenerateIndexedMeshes(true)     // Only loads each vertex once -> duplicates and indices omitted
                    .loadMeshObjects(Resource.file(Paths.get(objSub + bbFilePath + ".obj")));      // load the file
            for (MeshObject object : objects) {
                alWallVertArr.add(object.getMesh().getVertices()); // All float[] -> vertices are placed in an ArrayList
            }
        }
        catch (IOException e) {     throw new RuntimeException(e);      }

        alAABB = aabbInstance.calcWallAABBs(alWallVertArr); // AABBs are constructed for each wall / float[] in the
                                                            // ArrayList (vertices) and placed in a new ArrayList<AABB>
    }
    /**
     * Initializes Karl
     * Karl is loaded using the Obj-Loader and loadMesh.
     * Loaded: Vertices, Normals
     * It does not have a specific texture but rather a plain color so only Normals are loaded.
     * @param gl OpenGL graphics context
     * */
    private void initKarl(GL3 gl) {
        try
        {
            vertKarl = new OBJLoader()
                    .setLoadNormals(true)                    // load normal data
                    .loadMesh(Resource.file(objFileKarl))    // load the file
                    .getVertices();                          // take vertices from loaded mesh
        }
        catch (IOException e) {     throw new RuntimeException(e);      }

        ArrayList<float[]> vertObjects = new ArrayList<>();
        vertObjects.add(vertKarl);

        loader.handleInitData(gl, vertObjects.size(), vertObjects, false);

        vaoKarl = loader.getVao();
        vboKarl = loader.getVbo();

        movement.setxKarl(-10.5f); // pass Karl's starting position to movement
        movement.setzKarl(10.5f);
    }
    /**
     * Initializes the Island
     * The Island's Obj - File contains multiple objects with individual textures.
     * Thus, it is loaded using the Obj-Loader and loadSurfaceObjects.
     * Loaded: Vertices, Normals, Texture Coordinates and Materials per surface
     * @param gl OpenGL graphics context
     * */
    private void initIsland(GL3 gl) {
        try
        {
            List<SurfaceObject> objects = new OBJLoader()
                    .setLoadNormals(true)
                    .setLoadTextureCoordinates(true)
                    .setIgnoreThirdTextureCoordinate(true)
                    .loadSurfaceObjects(Resource.file(objFileIsland));

            Surface boat = objects.get(0).getSurfaces().get(0);            // load the single surfaces
            Surface palmColors = objects.get(1).getSurfaces().get(0);
            Surface ocean = objects.get(2).getSurfaces().get(0);

            matBoat = boat.getMaterial();                       // load the corresponding materials
            matPalmColors = palmColors.getMaterial();
            matOcean = ocean.getMaterial();

            texBoat = matBoat.getDiffuseMap();                 // extract the corresponding DiffuseMaps -> texture files
            texPalmColors = matPalmColors.getDiffuseMap();
            texOcean = matOcean.getDiffuseMap();

            vertBoat = boat.getMesh().getVertices();           // take vertices from loaded mesh
            vertPalmColors = palmColors.getMesh().getVertices();
            vertOcean = ocean.getMesh().getVertices();
        }
        catch (IOException e) {     throw new RuntimeException(e);      }

        ArrayList<float[]> vertObjects = new ArrayList<>();
        vertObjects.add(vertBoat);
        vertObjects.add(vertPalmColors);
        vertObjects.add(vertOcean);

        loader.handleInitData(gl, vertObjects.size(), vertObjects, true);
        vaoIsland = loader.getVao();
        vboIsland = loader.getVbo();

        textureBoat = loader.loadTexture(gl, objSub, texBoat.getFileName());
            textureBoat.enable(gl);
        texturePalmColors = loader.loadTexture(gl, objSub, texPalmColors.getFileName());
            texturePalmColors.enable(gl);
        textureOcean = loader.loadTexture(gl, objSub, texOcean.getFileName());
        textureOcean.setTexParameteri(gl, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);
        textureOcean.setTexParameteri(gl, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
            textureOcean.enable(gl);
        }

    //------------------------------------------------------------------------------------------------------------------
    // DISPLAY
    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * called by the OpenGL animator for every frame.
     * It calls sub - display() methods and listens to changes of variable values to enter different
     * display loops.
     * @param drawable The OpenGL drawable
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();                                   // Retrieve the OpenGL graphics context
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);     // Clear color and depth buffer

        pmvMatrix.glLoadIdentity();                                           // Apply view transform using the PMV-Tool
        pmvMatrix.gluLookAt(25f, 30f, 0f,
                0f, 0f, 0f,
                0f, 1.0f, 0f);

        // display Labyrinth and Karl ----------------------------------------------------------------------------------
        // Labyrinth____________________________________________________________________________________________________
            pmvMatrix.glPushMatrix();
//            movement.setValues(Vision.getAngleXaxis(), Vision.getAngleZaxis()); // ←If you would like to use the arrow
                                                                                  // keys, please comment out this
            pmvMatrix.glRotatef(movement.getAngleXaxis(), 0f, 0f, 1f);     //Rotation is steered by movement
            pmvMatrix.glRotatef(movement.getAngleZaxis(), 1f, 0f, 0f);     // and affects Labyrinth and Karl
            displayLabyrinth(gl);
                               // Labyrinth is displayed -> further changes of the pmvMatrix have no effect on Labyrinth
        // Karl_________________________________________________________________________________________________________
            AABB karlAABB = new AABB(movement.getMinKarlPos(), movement.getMaxKarlPos());
            IntersectionData intersectionData = collisionDetection.checkDetection(alAABB, karlAABB);
            movement.basicRollingAround();        // erkennt Kollision und gibt es in der Konsole aus. Keine Korrektur
//          movement.rollingAround(intersectionData); // ↑ + versucht die Position zu korrigieren

            pmvMatrix.glTranslatef(movement.getxKarl(), 0f, 0f);
            pmvMatrix.glTranslatef(0f, 0f, movement.getzKarl());

            if (movement.isHomerun())                 // homerun is true if Karl hits the HitCircle of the ground's hole
            {
                       // Karl falls ↓ the hole due to the position correction done via delta -x and -z in rollingAround
                pmvMatrix.glTranslatef(0f, movement.getyKarl(), 0f);
                if (movement.getyKarl() < -3.5f) {                   // wait before loading new level → nicer transition
                    System.out.println("Hit!");
                    counter++;                                          // level counter one up
                    setVariables(counter);                              // set different variables for new level
                    initLabyrinth(gl, objFilePath);                     // load new level
                    initLabyrinthBB(gl, bbFilePath);                    // load corresponding BB
                    resetMovementValues();                              // reset movement values
                }
                if(counter == 4) {
                    System.out.println("Game Over!");

                    //Berechnung der Punkte anhand der Zeit
                    su.stoppen();
                    int punkte = 1000 - (int) (su.getLaufzeit() / 100);

                    Main.db.eintraghinzufügen("Highscore",StartController.getSpieler(),punkte);

                    dispose(drawable);

                }
            }
            displayKarl(gl);
            pmvMatrix.glPopMatrix();

        // Island_______________________________________________________________________________________________________
            pmvMatrix.glPushMatrix();
                pmvMatrix.glRotatef(90,0,1,0);
//                pmvMatrix.glRotatef(-15,1,0,0);
//                pmvMatrix.glTranslatef(0,-60,-70); // For Graveyard
                pmvMatrix.glTranslatef(12,-18,-20); // For Island
                displayIsland(gl);
            pmvMatrix.glPopMatrix();
    }
    /**
     * Displays the labyrinths.
     * Several values are transferred to the GPU, afterwards
     * the labyrinth is drawn.
     * @param gl OpenGL graphics context
     * */
    private void displayLabyrinth(GL3 gl) {
        gl.glBindVertexArray(vaoLabyrinth[0]);
        gl.glUseProgram(spTexture.getShaderProgramID());

        // Transfer several values to the GPU
        // PVM-Matrix (model-view and projection matrix) ---------------------------------------------------------------
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glUniformMatrix4fv(2, 1, false, pmvMatrix.glGetMvitMatrixf());
        // light source ------------------------------------------------------------------------------------------------
        gl.glUniform4fv(3, 1, light.getPosition(), 0);
        gl.glUniform4fv(4, 1, light.getAmbient(), 0);
        gl.glUniform4fv(5, 1, light.getDiffuse(), 0);
        gl.glUniform4fv(6, 1, light.getSpecular(), 0);
        // material parameters -----------------------------------------------------------------------------------------
        gl.glUniform4fv(7, 1, matLab.getAmbientColor().toFloatArray(), 0);
        gl.glUniform4fv(8, 1, matLab.getDiffuseColor().toFloatArray(), 0);
        gl.glUniform4fv(9, 1, matLab.getSpecularColor().toFloatArray(), 0);
        // Activate texture in slot 0 and send it as 2D texture to the fragment shader ---------------------------------
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, textureLab.getTextureObject(gl));

        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertLabyrinth.length);      // Use the vertices in VBO to draw labyrinth
    }
    /**
     * Displays Karl.
     * Several values are transferred to the GPU, afterwards
     * Karl is drawn.
     * @param gl OpenGL graphics context
     * */
    private void displayKarl(GL3 gl) {
        gl.glBindVertexArray(vaoKarl[0]);
        gl.glUseProgram(spKarl.getShaderProgramID());

        // PVM-Matrix (model-view and projection matrix) ---------------------------------------------------------------
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        // color values for Karl's plain color -------------------------------------------------------------------------
        float[] vColor = new float[]{0.4f, 0.9f, 0.93f};                        // turqouise color to match our GUI Karl
        gl.glUniform3fv(7, 1, vColor, 0);

        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertKarl.length);                // Use the vertices in VBO to draw Karl
    }
    /**
     * Displays the island.
     * Several values are transferred to the GPU, afterwards
     * the island is drawn.
     * @param gl OpenGL graphics context
     * */
    private void displayIsland(GL3 gl) {
        gl.glUseProgram(spTexture.getShaderProgramID());
        // Transfer several values to the GPU
        // PVM-Matrix (model-view and projection matrix) ------------------------------
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
        gl.glUniformMatrix4fv(2, 1, false, pmvMatrix.glGetMvitMatrixf());
        // light source ---------------------------------------------------------------
        gl.glUniform4fv(3, 1, light.getPosition(), 0);
        gl.glUniform4fv(4, 1, light.getAmbient(), 0);
        gl.glUniform4fv(5, 1, light.getDiffuse(), 0);
        gl.glUniform4fv(6, 1, light.getSpecular(), 0);
        //--------------------------------------------------------------------------------------------------------------
        // BOAT MATERIAL
        gl.glBindVertexArray(vaoIsland[0]);
        // material parameters -----------------------------------------------------------------------------------------
        gl.glUniform4fv(7, 1, matBoat.getAmbientColor().toFloatArray(), 0);
        gl.glUniform4fv(8, 1, matBoat.getDiffuseColor().toFloatArray(), 0);
        gl.glUniform4fv(9, 1, matBoat.getSpecularColor().toFloatArray(), 0);
        // Activate texture in slot 0 and send it as 2D texture to the fragment shader ---------------------------------
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, textureBoat.getTextureObject(gl));
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertBoat.length);
        //--------------------------------------------------------------------------------------------------------------
        // PALM MATERIAL
        gl.glBindVertexArray(vaoIsland[1]);
        // material parameters -----------------------------------------------------------------------------------------
        gl.glUniform4fv(7, 1, matPalmColors.getAmbientColor().toFloatArray(), 0);
        gl.glUniform4fv(8, 1, matPalmColors.getDiffuseColor().toFloatArray(), 0);
        gl.glUniform4fv(9, 1, matPalmColors.getSpecularColor().toFloatArray(), 0);
        // texture slot 0
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, texturePalmColors.getTextureObject(gl));
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertPalmColors.length);
        //--------------------------------------------------------------------------------------------------------------
        // OCEAN MATERIAL
        gl.glBindVertexArray(vaoIsland[2]);
        // material parameters -----------------------------------------------------------------------------------------
        gl.glUniform4fv(7, 1, matOcean.getAmbientColor().toFloatArray(), 0);
        gl.glUniform4fv(8, 1, matOcean.getDiffuseColor().toFloatArray(), 0);
        gl.glUniform4fv(9, 1, matOcean.getSpecularColor().toFloatArray(), 0);
        // texture slot 0
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, textureOcean.getTextureObject(gl));
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertOcean.length);
    }

    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * called when the OpenGL window is resized.
     * @param drawable The OpenGL drawable
     * @param x x-coordinate of the viewport
     * @param y y-coordinate of the viewport
     * @param width width of the viewport
     * @param height height of the viewport
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();                                      // Retrieve the OpenGL graphics context
        gl.glViewport(0, 0, width, height);                              // Set the viewport to the entire window
        pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);                // Switch the pmv-tool to perspective projection
        pmvMatrix.glLoadIdentity();                                               // Reset projection matrix to identity

                // Calculate projection matrix
                //      Parameters:
                //          fovy (field of view), aspect ratio,
                //          zNear (near clipping plane), zFar (far clipping plane)
                            // near: 25, far: 100 → surround scene as tight as possible
        pmvMatrix.gluPerspective(45f, (float) width/ (float) height, 25f, 100f);
        pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);                                // Switch to model-view transform
    }

    /**
     * Implementation of the OpenGL EventListener (GLEventListener) method
     * called when OpenGL canvas ist destroyed.
     * @param drawable the drawable
     */
    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Retrieve the OpenGL graphics context
        GL3 gl = drawable.getGL().getGL3();
        System.out.println("Deleting allocated objects, incl. the shader program.");

        // Detach and delete shader program
        gl.glUseProgram(spKarl.getShaderProgramID());
        spKarl.deleteShaderProgram();
        gl.glUseProgram(spTexture.getShaderProgramID());
        spTexture.deleteShaderProgram();

        // deactivate VAO and VBO
        gl.glBindVertexArray(0);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(2);
        gl.glDeleteVertexArrays(1, vaoLabyrinth,0);
        gl.glDeleteBuffers(1, vboLabyrinth, 0);

        gl.glBindVertexArray(1);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
        gl.glDeleteVertexArrays(1, vaoKarl,0);
        gl.glDeleteBuffers(1, vboKarl, 0);

        gl.glBindVertexArray(2);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(2);
        gl.glDeleteVertexArrays(1, vaoIsland,0);
        gl.glDeleteBuffers(1, vboIsland, 0);

        System.exit(0);
    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTERS & SETTERS
    public void setObjFilePath(String objFilePath) {
        this.objFilePath = objFilePath;
    }
    public void setBbFilePath(String bbFilePath) {
        this.bbFilePath = bbFilePath;
    }

    public float getxHolePosition() {
        return xHolePosition;
    }
    public void setxHolePosition(float xHolePosition) {
        this.xHolePosition = xHolePosition;
    }

    public float getzHolePosition() {
        return zHolePosition;
    }
    public void setzHolePosition(float zHolePosition) {
        this.zHolePosition = zHolePosition;
    }
}