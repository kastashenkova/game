package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node; // Все ще потрібен, оскільки hero є Node

public class Main extends SimpleApplication {

    private Node hero; // Тип Node залишається
    private float swayHeight = 0.10f; // Амплітуда покачування
    private float swaySpeed = 2.0f;
    private float rotationAngle = 0.02f; // Кут обертання
    private float rotationSpeed = 1.5f;

    private float timer = 0f;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator("assets", FileLocator.class);

        // Завантажуємо героя
        hero = (Node) assetManager.loadModel("Models/Hero/Eric2.glb");
        rootNode.attachChild(hero);
        hero.scale(4f);
        hero.setLocalTranslation(0, 0, 0);

        // Налаштування камери
        cam.setLocation(new Vector3f(0, 2, 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        // Налаштування освітлення
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -2, -3).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        // Налаштування керування камерою
        flyCam.setMoveSpeed(10);
    }

    @Override
    public void simpleUpdate(float tpf) {
        timer += tpf;

        // --- Покачування героя вгору-вниз та обертання ---
        float heroYOffset = (float) Math.sin(timer * swaySpeed) * swayHeight;
        hero.setLocalTranslation(hero.getLocalTranslation().x, heroYOffset, hero.getLocalTranslation().z);

        float heroRotation = (float) Math.cos(timer * rotationSpeed) * rotationAngle;
        Quaternion heroQ = new Quaternion();
        heroQ.fromAngleAxis(heroRotation, Vector3f.UNIT_Z); // Обертання навколо осі Z для нахилу вліво/вправо
        hero.setLocalRotation(heroQ);
    }
}