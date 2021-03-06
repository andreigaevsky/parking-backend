package com.parking.spring.recognition.vision.network;



import com.parking.spring.recognition.vision.result.NetworkResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LabeledNetwork<R extends NetworkResult> extends BaseNeuralNetwork<R> {
    private List<String> labels = new ArrayList<>();

    public void loadLabels(Path namesFile) {
        try {
            labels.clear();
            labels.addAll(Files.readAllLines(namesFile.toAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLabels(String... names) {
        labels.clear();
        Collections.addAll(labels, names);
    }

    protected String getLabelOrId(int classId) {
        if (labels.size() > classId) {
            return labels.get(classId);
        }

        return String.valueOf(classId);
    }

    public List<String> getLabels() {
        return labels;
    }
}
