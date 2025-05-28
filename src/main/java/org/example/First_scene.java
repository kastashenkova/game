package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

public class First_scene extends SimpleApplication {

    private Node ferrisWheelNode;
    private Node rotatingWheelPart;
    private Node mohylaAcademyNode;

    public static void main(String[] args) {
        First_scene app = new First_scene();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator("assets", FileLocator.class);

        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(15);
        cam.setLocation(new Vector3f(0, 25, 70));
        cam.lookAt(new Vector3f(0, 10, 0), Vector3f.UNIT_Y);

        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1.0f, 1.0f));

        // --- Створення "Контрактової Площі" (земля) ---
        Box groundBox = new Box(100, 0.1f, 100);
        Geometry ground = new Geometry("Ground", groundBox);
        Material groundMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Texture groundTex = assetManager.loadTexture("Textures/cobblestone.jpg");
        if (groundTex != null) {
            groundTex.setWrap(Texture.WrapMode.Repeat);
            groundMat.setTexture("DiffuseMap", groundTex);
        }
        groundMat.setColor("Diffuse", ColorRGBA.LightGray);
        groundMat.setColor("Ambient", ColorRGBA.Gray);
        ground.setMaterial(groundMat);
        rootNode.attachChild(ground);

        // --- Створення "Києво-Могилянської Академії" ---
        Box box = new Box(10, 10, 10); // Розміри куба
        Geometry mohylaAcademyNode = new Geometry("MohylaAcademy", box);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Orange); // Колір куба
        mohylaAcademyNode.setMaterial(mat);
        rootNode.attachChild(mohylaAcademyNode);


        // --- Створення "Колеса Огляду" ---
        ferrisWheelNode = new Node("FerrisWheelNode");
        Material wheelWhiteMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        wheelWhiteMat.setColor("Diffuse", ColorRGBA.White);
        wheelWhiteMat.setColor("Ambient", ColorRGBA.White.mult(0.9f));
        wheelWhiteMat.setBoolean("UseMaterialColors", true);

        Box supportBox = new Box(1f, 15f, 1f);
        Geometry support1 = new Geometry("Support1", supportBox);
        support1.setMaterial(wheelWhiteMat);
        support1.setLocalTranslation(-8f, 7.5f, 0);
        ferrisWheelNode.attachChild(support1);

        Geometry support2 = new Geometry("Support2", supportBox);
        support2.setMaterial(wheelWhiteMat);
        support2.setLocalTranslation(8f, 7.5f, 0);
        ferrisWheelNode.attachChild(support2);

        Cylinder axleCylinder = new Cylinder(16, 16, 1f, 2f, true);
        Geometry axle = new Geometry("Axle", axleCylinder);
        axle.setMaterial(wheelWhiteMat);
        axle.rotate(0, (float) Math.PI / 2, 0);
        axle.setLocalTranslation(0, 15f, 0);
        ferrisWheelNode.attachChild(axle);

        rotatingWheelPart = new Node("RotatingWheelPart");

        Cylinder wheelRing = new Cylinder(2, 64, 15f, 0.5f, true);
        Geometry wheelGeom = new Geometry("WheelRing", wheelRing);
        wheelGeom.setMaterial(wheelWhiteMat);
        wheelGeom.setLocalRotation(new Quaternion().fromAngleAxis((float) Math.PI / 2, Vector3f.UNIT_X));
        wheelGeom.setLocalTranslation(0, 20f, 0);
        rotatingWheelPart.attachChild(wheelGeom);

        Cylinder spokeCylinder = new Cylinder(2, 16, 0.2f, 15f, true);
        Geometry spoke1 = new Geometry("Spoke1", spokeCylinder);
        spoke1.setMaterial(wheelWhiteMat);
        spoke1.setLocalRotation(new Quaternion().fromAngleAxis((float) Math.PI / 2, Vector3f.UNIT_X));
        spoke1.rotate(0, 0, (float) Math.PI / 4);
        spoke1.setLocalTranslation(0, 20f, 0);
        rotatingWheelPart.attachChild(spoke1);

        Geometry spoke2 = new Geometry("Spoke2", spokeCylinder);
        spoke2.setMaterial(wheelWhiteMat);
        spoke2.setLocalRotation(new Quaternion().fromAngleAxis((float) Math.PI / 2, Vector3f.UNIT_X));
        spoke2.rotate(0, 0, -(float) Math.PI / 4);
        spoke2.setLocalTranslation(0, 20f, 0);
        rotatingWheelPart.attachChild(spoke2);

        Material cabinMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        cabinMat.setColor("Diffuse", ColorRGBA.White);
        cabinMat.setColor("Ambient", ColorRGBA.Gray);
        cabinMat.setBoolean("UseMaterialColors", true);

        Box cabinBox = new Box(1.5f, 1.5f, 1.5f);
        float wheelRadius = 15f;
        int numCabins = 18;

        for (int i = 0; i < numCabins; i++) {
            Geometry cabin = new Geometry("Cabin" + i, cabinBox);
            cabin.setMaterial(cabinMat);
            cabin.setLocalRotation(new Quaternion().fromAngles(0, 0, 0));

            float angle = (float) Math.PI * 2 / numCabins * i;
            cabin.setLocalTranslation(
                    wheelRadius * (float) Math.cos(angle),
                    20f + wheelRadius * (float) Math.sin(angle),
                    0
            );
            rotatingWheelPart.attachChild(cabin);
        }

        rotatingWheelPart.setLocalTranslation(0, 0, 0);
        ferrisWheelNode.attachChild(rotatingWheelPart);

        ferrisWheelNode.setLocalTranslation(35, 0.1f, 0);
        rootNode.attachChild(ferrisWheelNode);

        // --- Налаштування освітлення ---
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(0.8f));
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.2f));
        rootNode.addLight(ambient);
    }
}