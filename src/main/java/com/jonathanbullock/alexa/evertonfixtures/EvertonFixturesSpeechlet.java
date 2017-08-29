package com.jonathanbullock.alexa.evertonfixtures;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class EvertonFixturesSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(EvertonFixturesSpeechlet.class);

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("GetNextEvertonFixtureIntent".equals(intentName)) {
            return getNextFixtureResponse();

        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();

        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    public SpeechletResponse getNextFixtureResponse() {
        String speechOutput = "";

        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder builder = new StringBuilder();
        try {
            String line;
            URLConnection conn = new URL("http://api.football-data.org/v1/teams/62/fixtures").openConnection();
            conn.setRequestProperty("X-Auth-Token", BuildConfig.REST_API_TOKEN);
            inputStream = new InputStreamReader(conn.getInputStream(), Charset.forName("US-ASCII"));

            bufferedReader = new BufferedReader(inputStream);
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

            String reqsLeft = conn.getHeaderField("X-Requests-Available");
            log.info("API requests left = " + reqsLeft);
        } catch (Exception e) {
            // reset builder to a blank string
            builder.setLength(0);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }

        if (builder.length() == 0) {
            speechOutput =
                    "Sorry, the Everton Fixtures web service is experiencing a problem. "
                            + "Please try again later.";
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> response = mapper.readValue(builder.toString(), Map.class);
                if (response != null) {
                    List<Map<String, Object>> fixtures = (List<Map<String, Object>>) response.get("fixtures");
                    for (Map<String, Object> fixture : fixtures) {
                        String dateString = fixture.get("date").toString();
                        DateTime date = DateTime.parse(dateString);
                        if (date != null) {
                            if (date.isAfterNow()) {
                                String homeTeam = fixture.get("homeTeamName").toString();
                                String awayTeam = fixture.get("awayTeamName").toString();
                                DateTimeFormatter fmt = DateTimeFormat.forPattern("dd MMMM yyyy");
                                speechOutput = homeTeam + " versus " + awayTeam + " on " + fmt.print(date);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Exception occurred while parsing web service response.", e);
            }
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Everton Fixtures");
        card.setContent(speechOutput);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechOutput);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getWelcomeResponse() {
        String speechText =
                "Welcome to the Everton Fixtures skill, you can ask a question like tell me the next Everton fixture? or what is the next Everton game?.\n\nNow, what can I help you with?";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Everton Fixtures");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private SpeechletResponse getHelpResponse() {
        String speechText =
                "You can ask a question like tell me the next Everton fixture? or what is the next Everton game? or, you can say exit.\n\nNow, what can I help you with?";

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }
}
