package sample;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;


public class RepositoryController {

    String API_IP = "192.168.1.57";
    String API_PORT = "5000";
    String API_VER = "v2";
    String API_CATALOG = "_catalog";
    String API_TAGS = "tags";
    String API_LIST = "list";
    private String API_PROTOCOL = "http";
    private boolean validateTLSCertificates = false;
    private boolean ignoreContentType = true;
    private String FULL_URI_BRANCHES = "<API_PROTOCOL>://<API_IP>:<API_PORT>/<API_VER>/<API_CATALOG>";
    private String FULL_URI_TAG = "<API_PROTOCOL>://<API_IP>:<API_PORT>/<API_VER>/<BRANCH>/<API_TAGS>/<API_LIST>";

    public RepositoryController(String API_IP) {
        this.API_IP = API_IP;
    }

    public static void main(String[] args) throws IOException {

        RepositoryController repositoryController = new RepositoryController("192.168.1.57");

        String url = repositoryController.compileCatalogURL();

        for (String branch : repositoryController.getAllBranches()) {


            System.out.println(repositoryController.getTags(branch) + " -- tags for " + branch);


        }


    }

    private JSONObject getJSONUrl(String url) throws IOException {

        String txt = Jsoup.connect(url).ignoreContentType(ignoreContentType).validateTLSCertificates(validateTLSCertificates).get().body().toString();
        txt = txt.replace("<body>", "");
        txt = txt.replace("</body>", "");

        return new JSONObject(txt);

    }

    private String compileCatalogURL() {
        FULL_URI_BRANCHES = FULL_URI_BRANCHES.replace("<API_PROTOCOL>", API_PROTOCOL);
        FULL_URI_BRANCHES = FULL_URI_BRANCHES.replace("<API_IP>", API_IP);
        FULL_URI_BRANCHES = FULL_URI_BRANCHES.replace("<API_PORT>", API_PORT);
        FULL_URI_BRANCHES = FULL_URI_BRANCHES.replace("<API_VER>", API_VER);
        FULL_URI_BRANCHES = FULL_URI_BRANCHES.replace("<API_CATALOG>", API_CATALOG);
        return FULL_URI_BRANCHES;
    }

    private String compileTagURL(String tag) {

        String tempString = tag.replace("\"", "");

        FULL_URI_TAG = FULL_URI_TAG.replace("<API_PROTOCOL>", API_PROTOCOL);
        FULL_URI_TAG = FULL_URI_TAG.replace("<API_IP>", API_IP);
        FULL_URI_TAG = FULL_URI_TAG.replace("<API_PORT>", API_PORT);
        FULL_URI_TAG = FULL_URI_TAG.replace("<API_VER>", API_VER);
        FULL_URI_TAG = FULL_URI_TAG.replace("<BRANCH>", tempString);
        FULL_URI_TAG = FULL_URI_TAG.replace("<API_TAGS>", API_TAGS);
        FULL_URI_TAG = FULL_URI_TAG.replace("<API_LIST>", API_LIST);
        return FULL_URI_TAG;
    }

    public ArrayList<String> getAllBranches() throws IOException {

        JSONObject jsonObject = getJSONUrl(compileCatalogURL());
        ArrayList<String> stringArrayList = new ArrayList<>();

        String[] repositories = jsonObject.get("repositories").toString().replace("[", "").replace("]", "").split(",");

        for (String branch : repositories) {

            stringArrayList.add(branch);

        }

        return stringArrayList;


    }

    public String getAPI_PROTOCOL() {
        return API_PROTOCOL;
    }

    public RepositoryController setAPI_PROTOCOL(String API_PROTOCOL) {
        this.API_PROTOCOL = API_PROTOCOL;
        return this;
    }

    public boolean isValidateTLSCertificates() {
        return validateTLSCertificates;

    }

    public RepositoryController setValidateTLSCertificates(boolean validateTLSCertificates) {
        this.validateTLSCertificates = validateTLSCertificates;
        return this;
    }

    public boolean isIgnoreContentType() {
        return ignoreContentType;
    }

    public RepositoryController setIgnoreContentType(boolean ignoreContentType) {
        this.ignoreContentType = ignoreContentType;
        return this;
    }

    public String getAPI_PORT() {
        return API_PORT;
    }

    public RepositoryController setAPI_PORT(String API_PORT) {
        this.API_PORT = API_PORT;
        return this;
    }

    public ArrayList<String> getTags(String branchName) throws IOException {

        String url = compileTagURL(branchName);
        JSONObject jsonObject = getJSONUrl(url);

        JSONArray jsonArray = jsonObject.getJSONArray("tags");
        ArrayList<String> stringArrayList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            stringArrayList.add(jsonArray.get(i).toString());

        }

        return stringArrayList;

    }

}



