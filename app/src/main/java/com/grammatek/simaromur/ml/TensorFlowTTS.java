package com.grammatek.simaromur.ml;

import com.grammatek.simaromur.App;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TensorFlowTTS {
    final private Interpreter interpreter;

    public TensorFlowTTS(String modelFilename) {
        File modelFile = new File(App.getDataPath() + "/" + modelFilename);
        // load the model
        interpreter = new Interpreter(modelFile);
    }

    public void Speak(String sampa) {
        // TODO: these input/output data types need to be changed according to the model
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("sampa", sampa);
        Map<String, Object> outputs = new HashMap<>();
        String[] audioData = new String[1];
        outputs.put("audio", audioData);
        try {
            interpreter.runSignature(inputs, outputs, "myTTSSignature");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
