package com.grammatek.simaromur;

import static java.util.UUID.randomUUID;

/**
 * Utterance class to combine all attributes of a SpeakRequest and make each utterance also uniquely
 * identifiable.
 */
public class Utterance {
    public String utterance;        //< the raw utterance
    public String normalized = "";  //< normalized
    public String g2p = "";         //< after g2p
    public boolean isActive = false;

    public java.util.UUID id = randomUUID();    //< unique id for identification
    public float speed = 1.0f;
    public float pitch = 1.0f;

    public Utterance(String anUtteranceString) {
        utterance = anUtteranceString;
    }
}
