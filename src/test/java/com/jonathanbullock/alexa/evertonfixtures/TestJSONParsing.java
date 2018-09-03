package com.jonathanbullock.alexa.evertonfixtures;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TestJSONParsing {

    @Test
    public void testParsing() {
        String speechOutput = "";

        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder builder = new StringBuilder();
        try {
            String line;
            URLConnection conn = new URL("http://api.football-data.org/v2/teams/62/matches").openConnection();
            conn.setRequestProperty("X-Auth-Token", BuildConfig.REST_API_TOKEN);
            inputStream = new InputStreamReader(conn.getInputStream(), Charset.forName("US-ASCII"));

            bufferedReader = new BufferedReader(inputStream);
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

            Map<String, List<String>> map = conn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                System.out.println("API response headers, key = " + entry.getKey() + ", value = " + entry.getValue());
            }

//            String reqsLeft = conn.getHeaderField("X-RequestsAvailable");
//            log.info("API requests left = " + reqsLeft);
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
                    List<Map<String, Object>> matches = (List<Map<String, Object>>) response.get("matches");
                    for (Map<String, Object> match : matches) {
                        String dateString = match.get("utcDate").toString();
                        DateTime date = DateTime.parse(dateString);
                        if (date != null) {
//                            DateTime future = new DateTime().plusMonths(1);
//                            if (date.isAfter(future)) {
                            if (date.isAfterNow()) {
                                Map<String, Object> homeTeam = (Map<String, Object>) match.get("homeTeam");
                                String homeTeamName = homeTeam.get("name").toString();
                                Map<String, Object> awayTeam = (Map<String, Object>) match.get("awayTeam");
                                String awayTeamName = awayTeam.get("name").toString();
                                DateTimeFormatter fmt = DateTimeFormat.forPattern("dd MMMM yyyy");
                                speechOutput = homeTeamName + " versus " + awayTeamName + " on " + fmt.print(date);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                //log.error("Exception occurred while parsing web service response.", e);
            }
        }
        System.out.println(speechOutput);
    }
}
