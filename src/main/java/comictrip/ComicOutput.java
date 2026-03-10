package comictrip;

public record ComicOutput(
    String tripId,
    Image image,
    Details details,
    String pointsOfInterest
) {
    public record Image(
        String name,
        byte[] imageBytes,
        String mimeType
    ) {
    }

    public record Details(
        String description,
        String location
    ) {
    }
}
