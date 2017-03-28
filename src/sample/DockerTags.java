package sample;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created by Dirt on 2/21/2017.
 */
public class DockerTags {


    public DockerTags() throws MalformedURLException, URISyntaxException {
    }


    public static void main(String[] args) throws IOException {

        String text = Jsoup.connect("http://www.ynet.co.il").validateTLSCertificates(false).ignoreContentType(true).userAgent("Mozilla").get().text();

        String [] arr = text.split("\n");


        for (String s : arr) {
            System.out.println(s);
        }

    }
}
