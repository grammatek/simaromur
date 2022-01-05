package com.grammatek.simaromur.device;

import com.grammatek.simaromur.App;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TTSEngineTensorFlow implements TTSEngine {
    final private Interpreter interpreter;

    public TTSEngineTensorFlow(String modelFilename) {
        File modelFile = new File(App.getDataPath() + "/" + modelFilename);
        // load the model
        interpreter = new Interpreter(modelFile);
    }

    public byte[] SpeakToWav(String sampas, float pitch, float speed, int sampleRate) {
        // TODO: these input/output data types need to be changed according to the model
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("sampa", sampas);
        Map<String, Object> outputs = new HashMap<>();
        String[] audioData = new String[1];
        outputs.put("audio", audioData);
        try {
            interpreter.runSignature(inputs, outputs, "myTTSSignature");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[16];
    }
}
