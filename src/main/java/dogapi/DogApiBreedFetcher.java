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
import java.util.*;


/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) {
        if (breed == null || breed.isBlank()) {
            throw new BreedNotFoundException(String.valueOf(breed));
        }

        String normalized = breed.trim().toLowerCase(Locale.ROOT);
        String url = "https://dog.ceo/api/breed/" + normalized + "/list";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            if (!response.isSuccessful()) {
                throw new BreedNotFoundException(breed);
            }

            String body = response.body().string();
            JSONObject json = new JSONObject(body);
            String status = json.optString("status", "error");

            if (!"success".equalsIgnoreCase(status)) {
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