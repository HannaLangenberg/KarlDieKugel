package Game.Beauty;

/**
 * This LightSource - class was provided by Prof. Dr. Merijam Gotzes and Prof. Dr.-Ing. Karsten Lehn.
 * As it was perfectly fitting for our project the way it was, I included it "as is" and only changed the coding style
 * to match my preferred style. - Hanna Langenberg
 *
 * Class holding parameters for a light source
 *  @author Karsten Lehn
 *  @version 23.10.2017, 25.10.2017
 */
public class LightSource {
    private float[] position;
    private float[] ambient;
    private float[] diffuse;
    private float[] specular;

    public LightSource() {  }

    public LightSource(float[] position, float[] ambient, float[] diffuse, float[] specular)
    {
        this.position = position;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GETTERS & SETTERS
    public float[] getPosition() {
        return position;
    }
    public void setPosition(float[] position) {
        this.position = position;
    }

    public float[] getAmbient() {
        return ambient;
    }
    public void setAmbient(float[] ambient) {
        this.ambient = ambient;
    }

    public float[] getDiffuse() {
        return diffuse;
    }
    public void setDiffuse(float[] diffuse) {
        this.diffuse = diffuse;
    }

    public float[] getSpecular() {
        return specular;
    }
    public void setSpecular(float[] specular) {
        this.specular = specular;
    }
}
