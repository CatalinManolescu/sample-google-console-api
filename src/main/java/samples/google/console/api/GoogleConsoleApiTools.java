package samples.google.console.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.webmasters.Webmasters;
import com.google.api.services.webmasters.WebmastersScopes;
import com.google.api.services.webmasters.model.UrlCrawlErrorsSample;
import com.google.api.services.webmasters.model.UrlCrawlErrorsSamplesListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class GoogleConsoleApiTools implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleConsoleApiTools.class);
    
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    public static void main(String[] args) {
        SpringApplication.run(GoogleConsoleApiTools.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        // args: credentials-source site-url category platform
        // https://developers.google.com/webmaster-tools/search-console-api-original/v3/urlcrawlerrorssamples/list#Parameters
        String usage = "Usage: java -jar <jar-file> <credentials-source> <site-url> <category> <platform>"
                               + System.lineSeparator()
                               + "https://developers.google.com/webmaster-tools/search-console-api-original/v3/urlcrawlerrorssamples/list#Parameters";
        if (args == null || args.length < 1) {
            LOGGER.error(usage);
            return;
        }
        
        String credentialsSource = args[0];
        // build credentials https://developers.google.com/identity/protocols/OAuth2ServiceAccount
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(credentialsSource))
                                              .createScoped(Collections.singleton(WebmastersScopes.WEBMASTERS_READONLY));
        
        String[] cmdArgs = getCmdArgs(args);
        
        // get crawl errors
        if (cmdArgs.length < 3) {
            LOGGER.error(usage);
            return;
        }
        
        this.processUrlcrawlerrorssamples(credential, cmdArgs);
    }
    
    private void processUrlcrawlerrorssamples(GoogleCredential credential, String[] cmdArgs) throws GeneralSecurityException, IOException {
        String url = cmdArgs[0];
        String category = cmdArgs[1];
        String platform = cmdArgs[2];
        
        // https://developers.google.com/api-client-library/java/google-api-java-client/dev-guide
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Webmasters webmasters = new Webmasters.Builder(httpTransport, JSON_FACTORY, credential).build();
        Webmasters.Urlcrawlerrorssamples.List sampleList = webmasters.urlcrawlerrorssamples().list(url, category, platform);
        HttpResponse httpResponse = sampleList.executeUnparsed();
        
        // save original response
        // httpResponse.download(new FileOutputStream("out.json"));
        
        //
        UrlCrawlErrorsSamplesListResponse response = httpResponse.parseAs(UrlCrawlErrorsSamplesListResponse.class);
        List<UrlCrawlErrorsSample> validErrors = this.getValidCrawlErrors(url, response.getUrlCrawlErrorSample());
        
        String validErrorsJson = JSON_FACTORY.toPrettyString(validErrors);
        OutputStream os = new FileOutputStream("out-valid.json");
        os.write(validErrorsJson.getBytes());
    }
    
    private List<UrlCrawlErrorsSample> getValidCrawlErrors(String baseURL, List<UrlCrawlErrorsSample> errList) throws MalformedURLException {
        List<UrlCrawlErrorsSample> validErrors = new ArrayList<>();
        int index = 0;
        for (UrlCrawlErrorsSample sample : errList) {
            URL url = new URL(baseURL + "/" + sample.getPageUrl());
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.connect();
                
                if (connection.getResponseCode() > 400) {
                    sample.setResponseCode(connection.getResponseCode());
                    validErrors.add(sample);
                }
                
                LOGGER.info(String.format("%4s", ++index)
                                    + " | " + connection.getResponseCode()
                                    + " | " + url.toExternalForm());
            } catch (IOException e) {
                LOGGER.error("Could not connect to : " + url.toExternalForm(), e);
            }
        }
        
        return validErrors;
    }
    
    private String[] getCmdArgs(String[] args) {
        String[] cmdArgs = new String[args.length - 1];
        if (args.length > 1) {
            System.arraycopy(args, 1, cmdArgs, 0, cmdArgs.length);
        }
        return cmdArgs;
    }
}
