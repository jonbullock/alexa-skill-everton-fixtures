package com.jonathanbullock.alexa.evertonfixtures;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

public class EvertonFixturesSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add(BuildConfig.APPLICATION_ID);
    }

    public EvertonFixturesSpeechletRequestStreamHandler() {
        super(new EvertonFixturesSpeechlet(), supportedApplicationIds);
    }

    public EvertonFixturesSpeechletRequestStreamHandler(Speechlet speechlet, Set<String> supportedApplicationIds) {
        super(speechlet, supportedApplicationIds);
    }
}
