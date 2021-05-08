package com.parking.spring.recognition.vision;


import com.parking.spring.recognition.vision.dependency.Dependency;
import com.parking.spring.recognition.vision.dependency.Repository;
import com.parking.spring.recognition.vision.util.ProcessingUtils;
import processing.core.PApplet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeepVision {
    private PApplet sketch;
    private boolean storeNetworksInSketch = false;

    public DeepVision(PApplet sketch) {
        this.sketch = sketch;
    }

    public void storeNetworksGlobal() {
        storeNetworksInSketch = false;
    }

    public void storeNetworksInSketch() {
        storeNetworksInSketch = true;
    }

    public void clearRepository() {
        updateRepositoryPath();

        try {
            Files.list(Repository.localStorageDirectory)
                    .filter(e -> !Files.isDirectory(e))
                    .forEach(e -> {
                        try {
                            Files.delete(e);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNetworkStoragePath() {
        return Repository.localStorageDirectory.toAbsolutePath().toString();
    }

    protected void updateRepositoryPath() {
        // decide where to store
        if (storeNetworksInSketch) {
            Repository.localStorageDirectory = Paths.get(sketch.sketchPath("networks"));
        } else {
            Repository.localStorageDirectory = Paths.get(ProcessingUtils.getLibPath(this), "networks");
        }
    }

    protected void prepareDependencies(Dependency... dependencies) {
        updateRepositoryPath();

        // download
        try {
            Files.createDirectories(Repository.localStorageDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Dependency dependency : dependencies) {
            dependency.resolve();
        }
    }

    private YOLONetwork createYOLONetwork(Dependency model, Dependency weights, Dependency names, int size) {
        prepareDependencies(model, weights, names);

        YOLONetwork network = new YOLONetwork(
                model.getPath(),
                weights.getPath(),
                size, size
        );

        network.loadLabels(names.getPath());
        return network;
    }

    public YOLONetwork createYOLOv3() {
        return createYOLOv3(608);
    }

    public YOLONetwork createYOLOv3(int inputSize) {
        return createYOLONetwork(Repository.YOLOv3Model, Repository.YOLOv3Weight, Repository.COCONames, inputSize);
    }

    public YOLONetwork createYOLOv3Cars() {
        return createYOLONetwork(Repository.YOLOv3CarsModel, Repository.YOLOv3CarsWeight, Repository.COCONames, 608);
    }

    public YOLONetwork createYOLOv3Test() {
        return createYOLONetwork(Repository.YOLOv3TestModel, Repository.YOLOv3TestWeight, Repository.COCONames, 608);
    }

    public YOLONetwork createYOLOv3Test2() {
        return createYOLONetwork(Repository.YOLOv3TestModel, Repository.YOLOv3Test2Weight, Repository.COCONames, 608);
    }


    public YOLONetwork createYOLOv3Tiny() {
        return createYOLOv3Tiny(416);
    }

    public YOLONetwork createYOLOv3Tiny(int inputSize) {
        return createYOLONetwork(Repository.YOLOv3TinyModel, Repository.YOLOv3TinyWeight, Repository.COCONames, inputSize);
    }

    public YOLONetwork createYOLOv3SPP() {
        return createYOLOv3SPP(608);
    }

    public YOLONetwork createYOLOv3SPP(int inputSize) {
        return createYOLONetwork(Repository.YOLOv3SPPModel, Repository.YOLOv3SPPWeight, Repository.COCONames, inputSize);
    }


}
