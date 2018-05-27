package fr.amai.youtubelist;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;


public class ListVideos {
    private static YouTube youtube;
    private static String key = "AIzaSyBQXnun7t96GU1mDCvch3JpHHM03Xy4mgg";


    public static void main(String[] args) throws IOException {
        YouTube youtube = getYouTubeService();

        YouTube.PlaylistItems.List playlistItemsListByPlaylistIdRequest = youtube.playlistItems().list("snippet");
        playlistItemsListByPlaylistIdRequest.setPlaylistId("PLTbQvx84FrARa9pUtZYK7t_UfyGMCPOBn");
        playlistItemsListByPlaylistIdRequest.setKey(key);
        playlistItemsListByPlaylistIdRequest.setMaxResults(50l);
        PlaylistItemListResponse response = playlistItemsListByPlaylistIdRequest.execute();
        List<PlaylistItem> vids=response.getItems();

        String next=response.getNextPageToken();

        while (next!=null){
            System.out.println(next);

            playlistItemsListByPlaylistIdRequest.setPageToken(next);
            response = playlistItemsListByPlaylistIdRequest.execute();
            next=response.getNextPageToken();
            System.out.println("got items : "+response.getItems().size());
            vids.addAll(response.getItems());

        }
        System.out.println("total "+vids.size());
        String SAMPLE_CSV_FILE = "./devoxx.csv";
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL
                .withHeader("ID", "title", "Description", "duration","link").withDelimiter(',').withQuoteMode(QuoteMode.ALL).withQuote('"'));
        PeriodFormatter pfin=ISOPeriodFormat.standard();
        PeriodFormatter pfout=PeriodFormat.wordBased(Locale.FRENCH);

        for (PlaylistItem it:vids) {


            YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics,contentDetails");
            String id=it.getSnippet().getResourceId().getVideoId();
            System.out.println(id);
            videoRequest.setId(id);
            videoRequest.setKey(key);
            VideoListResponse listResponse = videoRequest.execute();
            List<Video> videoList = listResponse.getItems();

            Video targetVideo = videoList.iterator().next();

            String duration= targetVideo.getContentDetails().getDuration();
            csvPrinter.printRecord(id, targetVideo.getSnippet().getTitle(),targetVideo.getSnippet().getDescription(),pfout.print(pfin.parsePeriod(duration)),"https://www.youtube.com/watch?v="+id);
            System.out.println( targetVideo.getSnippet().getTitle() +" ; "+ targetVideo.getContentDetails().getDuration()+";"+targetVideo.getSnippet().getDescription());
            csvPrinter.flush();
        }



    }
    /**
     * Build and return an authorized API client service, such as a YouTube
     * Data API client service.
     * @return an authorized API client service
     * @throws IOException
     */
    public static YouTube getYouTubeService() throws IOException {
        YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {

            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("APP_ID").build();
        return youtube;
    }



}
