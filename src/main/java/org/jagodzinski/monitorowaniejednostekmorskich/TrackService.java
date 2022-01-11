package org.jagodzinski.monitorowaniejednostekmorskich;

import com.fasterxml.jackson.databind.JsonNode;
import org.jagodzinski.monitorowaniejednostekmorskich.model.Datum;
import org.jagodzinski.monitorowaniejednostekmorskich.model.Point;
import org.jagodzinski.monitorowaniejednostekmorskich.model.Track;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TrackService {
    RestTemplate restTemplate = new RestTemplate();


    public List<Point> getTracks() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjBCM0I1NEUyRkQ5OUZCQkY5NzVERDMxNDBDREQ4OEI1QzA5RkFDRjMiLCJ0eXAiOiJhdCtqd3QiLCJ4NXQiOiJDenRVNHYyWi03LVhYZE1VRE4ySXRjQ2ZyUE0ifQ.eyJuYmYiOjE2Mzk1MDM1MjgsImV4cCI6MTYzOTUwNzEyOCwiaXNzIjoiaHR0cHM6Ly9pZC5iYXJlbnRzd2F0Y2gubm8iLCJhdWQiOiJhcGkiLCJjbGllbnRfaWQiOiJrYWNwZXIuamFnOTdAZ21haWwuY29tOmthY3Blci5qYWc5N0BnbWFpbC5jb20iLCJzY29wZSI6WyJhcGkiXX0.LEDUgyYzfaDOHCt1knLOJ1ukjImFKEui0_SeEg4DakYPzYQ15PeaKIoeURUoe0duxQxwJkRZwSP5cLzQE0kRhNyzYe5f-JXWCYvWrmP6RAQu8u67aRi2niqAosqICp7C7_XLxxiuR3Z18M4h300rgH8kFyL6TlF6Y_RHWJkTFOLsJ9nmXoJhPk14a9uSpYv9fTep_ks5FX27D3SZ0PU6HyheVJPeAWEq7YZ1IuujXEHSd2TZzHEhSAfPVEP0tkpWJsPTBECyOr8OHljkrq-uh9VoQBUGCPtNVBe1PRlJ05s6OfnchT6zH6Gd974vaPIFaldxeVUdyqA5L-vnCI5VIunsJ_6ZfDdBQm-wDtQpWUAIfmbepqv1IZMsrXyvn56pIJDe1hIqmZHjchvxzW5k8W1FpOQFywufs0Kca6CqnJDDPHjqnSfBzA7IxD7uxiEyUf3Sxme2FUGXDkZPlJBmIdIzZVxQZoFi7SzMb3GSHCYK7aX-4CoeRdMHery0kaUgf8eBrlurCYO-l4fsT9gLMLAJeN32tGgcxWB71lh3xx5PnP9N6AQTsdcUAfdZzwChKsjgffGypAkX_NCYv-FnXdAl6Jf-Ui4_Bv62t8gOcQ2uZUYfON4Nvr078SljyyA21smdoWQFYyaTX8AIEQAVJ1A6RJokv9ohFIkjYep94pk");
        HttpEntity httpEntity = new HttpEntity(httpHeaders);

        ResponseEntity<Track[]> exchange = restTemplate.exchange("https://www.barentswatch.no/bwapi/v2/geodata/ais/openpositions?Xmin=10.09094&Xmax=10.67047&Ymin=63.3989&Ymax=63.58645",
                HttpMethod.GET,
                httpEntity,
                Track[].class);

        List<Point> collect = Stream.of(exchange.getBody()).map(
                track -> new Point(
                        track.getGeometry().getCoordinates().get(0),
                        track.getGeometry().getCoordinates().get(1),
                        track.getName(),
                        getDestination(track.getDestination(), track.getGeometry().getCoordinates()).getLongitude(),
                        getDestination(track.getDestination(), track.getGeometry().getCoordinates()).getLatitude()
                )
        ).collect(Collectors.toList());
        return collect;
    }

    public Datum getDestination(String destinationName, List<Double> coordinates) {
        try {
            String url = "http://api.positionstack.com/v1/forward?access_key=f9aae45e031a1e66eac64db90ffda427&query=" + destinationName;
            JsonNode data = restTemplate.getForObject(url, JsonNode.class).get("data").get(0);
            double latitude = data.get("latitude").asDouble();
            double longitude = data.get("longitude").asDouble();
            return new Datum(latitude, longitude);

        } catch (Exception ex) {
            return new Datum(coordinates.get(1), coordinates.get(0));
        }
    }
}