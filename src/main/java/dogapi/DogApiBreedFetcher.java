package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * BreedFetcher implementation that calls the Dog CEO API.
 * All failures are surfaced as BreedNotFoundException.
 */
public class DogApiBreedFetcher implements BreedFetcher {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * Fetch the list of sub-breeds for the given breed.
     * GET https://dog.ceo/api/breed/{breed}/list
     */
    @Override
    public List<String> getSubBreeds(String breed) {
        if (breed == null || breed.isBlank()) {
            throw new BreedNotFoundException(String.valueOf(breed));
        }

        String normalized = breed.trim().toLowerCase(Locale.ROOT);
        String url = "https://dog.ceo/api/breed/" + normalized + "/list";

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "okhttp-dogapi-student-client/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            String body = response.body().string();

            if (!response.isSuccessful()) {
                throw new BreedNotFoundException(breed);
            }

            JSONObject json = new JSONObject(body);
            if (!"success".equalsIgnoreCase(json.optString("status"))) {
                throw new BreedNotFoundException(breed);
            }

            JSONArray arr = json.getJSONArray("message");
            List<String> subBreeds = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                subBreeds.add(arr.getString(i));
            }
            return subBreeds;

        } catch (IOException e) {
            throw new BreedNotFoundException(breed);
        }
    }
}
