package com.grammatek.simaromur.ml;

import com.grammatek.simaromur.App;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

public class PyTorchTTS {
    private Module mModuleEncoder;

    public PyTorchTTS(String modelFilename) {
        // load the model
        if (mModuleEncoder == null) {
            mModuleEncoder = LiteModuleLoader.load(App.getDataPath() + "/" + modelFilename);
        }

    }
    public void Speak(String sampa) {
        // sampa needs to be pure and we need to map the sampa symbols to the correct labels
        // used for training the model

        // TODO: this is of course too naive, but demonstrates the principle. Replace with
        //       appropriate method
        String[] sampaList = sampa.split(" ");
        long wav2vecinput[] = new long[sampaList.length];
        for (int n = 0; n < sampaList.length; n++)
            wav2vecinput[n] = MapSampaToLong(sampaList[n]);
        LongBuffer inTensorBuffer = Tensor.allocateLongBuffer(sampa.length());
        for (long val : wav2vecinput)
            inTensorBuffer.put((long) val);

        Tensor inTensor = Tensor.fromBlob(inTensorBuffer, new long[]{1, sampa.length()});
        final String result = mModuleEncoder.forward(IValue.from(inTensor)).toStr();

        // TODO: now do sth with the result and return it as audio or use a callback to feed
        //       the audio into
    }

    // Maps a sampa symbol to a label used in the model
    private long MapSampaToLong(String sampaSymbol) {
        // TODO: implement
        return 0;
    }
}
