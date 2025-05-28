package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Підключення локальної папки з моделлю
        assetManager.registerLocator("assets", FileLocator.class);

        // Завантаження героя
        Spatial hero = assetManager.loadModel("Models/Hero/heroe.gltf");

        // Додати героя на сцену
        rootNode.attachChild(hero);

        hero.scale(0.05f); // або 0.1f, залежно від ситуації


        cam.setLocation(new Vector3f(0, 2, 10)); // Позиція камери
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y); // Дивимось на центр

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -2, -3).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);



    }
}