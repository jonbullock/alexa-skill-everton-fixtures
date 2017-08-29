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
            URLConnection conn = new URL("http://api.football-data.org/v1/teams/62/fixtures").openConnection();
            conn.setRequestProperty("X-Auth-Token", "d29e398f0af8477a9bcc1349adc235fe");
            inputStream = new InputStreamReader(conn.getInputStream(), Charset.forName("US-ASCII"));

//            URL url = new URL("http://api.football-data.org/alpha/teams/62/fixtures");
//            inputStream = new InputStreamReader(url.openStream(), Charset.forName("US-ASCII"));

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
                    List<Map<String, Object>> fixtures = (List<Map<String, Object>>) response.get("fixtures");
                    for (Map<String, Object> fixture : fixtures) {
                        String dateString = fixture.get("date").toString();
                        DateTime date = DateTime.parse(dateString);
                        if (date != null) {
//                            DateTime future = new DateTime().plusMonths(1);
//                            if (date.isAfter(future)) {
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
                //log.error("Exception occurred while parsing web service response.", e);
            }
        }
        System.out.println(speechOutput);
    }
}
