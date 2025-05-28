package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        // Створення та запуск екземпляра програми
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        assetManager.registerLocator("assets", FileLocator.class);

        Spatial hero = assetManager.loadModel("Models/Hero/Eric2.glb");

//        Текстура?
//        Texture texture = assetManager.loadTexture("Textures/claudia_texture(NOT FUNCTIONAL).png");
//
//        Material heroMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        // Призначаємо завантажену текстуру як основну карту кольорів (ColorMap) для матеріалу.
//        heroMaterial.setTexture("ColorMap", texture);
//
//
//        hero.depthFirstTraversal(spatial -> {
//            if (spatial instanceof Geometry) {
//                Geometry geom = (Geometry) spatial;
//                geom.setMaterial(heroMaterial);
//            }
//        });


        rootNode.attachChild(hero);

        hero.scale(4f);

        cam.setLocation(new Vector3f(0, 2, 10)); // Позиція камери (x, y, z)
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y); // Камера дивиться на центр сцени, "вгору" по осі Y


        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -2, -3).normalizeLocal()); // Напрямок світла
        sun.setColor(ColorRGBA.White); // Колір світла
        rootNode.addLight(sun);


        flyCam.setMoveSpeed(10);
    }
}