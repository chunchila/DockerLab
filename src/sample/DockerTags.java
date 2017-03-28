package sample;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Dirt on 2/21/2017.
 */
public class DockerTags {


    public DockerTags() throws MalformedURLException, URISyntaxException {
    }


    public static void main(String[] args) throws IOException {
        URL url = new URL("http://www.walla.co.il");

        URLConnection urlConnection = url.openConnection();


        BufferedInputStream bufferedInputStream = new BufferedInputStream((urlConnection.getInputStream()));


        InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());

        BufferedReader reader = new BufferedReader(inputStreamReader);

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String inputLine = "";


        while ((inputLine = bufferedReader.readLine()) != null) {


            System.out.println(inputLine);


        }
        bufferedReader.close();


    }
}
