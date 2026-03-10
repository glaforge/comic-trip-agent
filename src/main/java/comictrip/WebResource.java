package comictrip;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static comictrip.ComicTripAnalyzer.COMIC_TRIP_APP_NAME;
import static comictrip.ComicTripAnalyzer.COMIC_TRIP_PICTURE_BUCKET;
import static comictrip.ComicTripAnalyzer.COMIC_TRIP_USER;

@Path("/")
public class WebResource {

    @Inject
    Template index;

    @Inject
    Template trip;

    public record TripSummary(String tripId, String imageUrl) {}

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        String prefix = COMIC_TRIP_APP_NAME + "/" + COMIC_TRIP_USER + "/";
        Page<Blob> blobs = storage.list(COMIC_TRIP_PICTURE_BUCKET, Storage.BlobListOption.prefix(prefix));

        Map<String, String> tripToFirstImage = new LinkedHashMap<>();

        for (Blob blob : blobs.iterateAll()) {
            String name = blob.getName();
            // name looks like: comic_trip_app/comic_trip_user/tripId/imageId.png/0
            if (name.endsWith("/0")) {
                String[] parts = name.split("/");
                if (parts.length >= 5) {
                    String tripId = parts[2];
                    String imageUrl = "https://storage.googleapis.com/" + COMIC_TRIP_PICTURE_BUCKET + "/" + name;
                    tripToFirstImage.putIfAbsent(tripId, imageUrl);
                }
            }
        }

        List<TripSummary> pastTrips = new ArrayList<>();
        tripToFirstImage.forEach((id, url) -> pastTrips.add(new TripSummary(id, url)));

        return index
            .data("pageTitle", "Comic Trip | Mission Control")
            .data("pastTrips", pastTrips);
    }

    @GET
    @Path("/trips/{tripId:[a-zA-Z0-9]+}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance trip(@PathParam("tripId") String tripId) {
        return trip
                .data("pageTitle", "Comic Trip | " + tripId)
                .data("tripId", tripId);
    }
}
