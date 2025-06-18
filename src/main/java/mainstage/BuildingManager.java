package mainstage;

import java.awt.*;
import java.io.IOException;

public class BuildingManager {
    private Building[] buildings = new Building[10];
    private GameBoard gameBoard;

    public BuildingManager(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public Building[] getBuildings() {
        return buildings;
    }

    public void setBuildings() {
        Building university = new Building();
        university.name = "NaUKMA";
        university.worldX = 230;
        university.worldY = 0;
        university.width = 430;
        university.height = 200;
        university.collision = true;
        buildings[0] = (university);

        Building wheel = new Building();
        wheel.name = "wheel";
        wheel.worldX = 1220;
        wheel.worldY = 250;
        wheel.width = 210;
        wheel.height = 230;
        wheel.collision = true;
        buildings[1] = (wheel);

        Building cafe = new Building();
        cafe.name = "cafe";
        cafe.width = 130;
        cafe.height = 180;
        cafe.collision = true;
        cafe.worldX = 20;
        cafe.worldY = 580;
        buildings[2] = cafe;

        Building corpus1 = new Building();
        corpus1.name = "corpus1";
        corpus1.width = 350;
        corpus1.height = 200;
        corpus1.collision = true;
        corpus1.worldX = 790;
        corpus1.worldY = 0;
        buildings[3] = corpus1;

        Building shop = new Building();
        shop.name = "shop";
        shop.width = 155;
        shop.height = 125;
        shop.collision = true;
        shop.worldX = 1150;
        shop.worldY = 590;
        buildings[4] = shop;

        Building road = new Building();
        road.name = "road";
        road.width = 75;
        road.height = 460;
        road.collision = true;
        road.worldX = 0;
        road.worldY = 0;
        buildings[5] = road;

        Building light = new Building();
        light.name = "light";
        light.width = 40;
        light.height = 90;
        light.collision = true;
        light.worldX = 90;
        light.worldY = 350;
        buildings[6] = light;

        Building building = new Building();
        building.name = "building";
        building.width = 100;
        building.height = 150;
        building.collision = true;
        building.worldX = 1320;
        building.worldY = 0;
        buildings[7] = building;

    }
}

