import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

public class Helios {

    private static final String LAB_WORK_AREA_URL = "https://helios.utcluj.ro/LEARN2CODE/work_area.php?SID";
    private static final String HELIOS_LOGIN_URL = "https://helios.utcluj.ro/LEARN2CODE/login.php?SID=";
    private static final String HELIOS_WELCOME_URL = "https://helios.utcluj.ro/LEARN2CODE/welcome.php";
    private final String userName;
    private final String password;


    // OkHttpClient for web requests, using a cookie jar to maintain sessions
    OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new InMemoryCookieJar())
            .build();

    public Helios(String userName, String password) throws IOException {
        this.userName = userName;
        this.password = password;
    }

    // Helper method to get the HTML body of a given URL
    private String getRequestBody(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()){
            assert response.body() != null;
            return response.body().string();
        }
    }

    // Handles logging in to the Helios system
    private void logIn() throws IOException{
        // Build multipart form data for login request
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", userName)
                .addFormDataPart("password", password)
                .addFormDataPart("action_type", "login")
                .addFormDataPart("form_content_name", "student_login_form")
                .build();

        Request request = new Request.Builder()
                .url(HELIOS_LOGIN_URL)
                .post(multipartBody)
                .build();

        try (Response response = client.newCall(request).execute()){

            //Throw error if login is unsuccessful
            if(!response.request().url().toString().equals(HELIOS_WELCOME_URL))
                throw new IOException("login was unsuccessful");
        }
        return;
    }

    // Retrieves the URL for a specific lab, throws an exception if not found
    private String getLabUrl(int labNumber) throws IOException {
        // Fetch the HTML containing lab listings
        String htmlWorkArea = getRequestBody(LAB_WORK_AREA_URL);

        Document doc = Jsoup.parse(htmlWorkArea);

        // Find 'td' elements containing lab links with a descriptive CSS selector
        Elements labHrefs = doc.select("td.results_table_content_td:has(a.href2)");

        String labName = STR."Lab_\{labNumber < 10 ? STR."0\{labNumber}" : labNumber}_SE_IS";

        // Iterate and search for the matching lab link
        for (Element labHref: labHrefs){

            Element labAbove = labHref.previousElementSibling();
            assert labAbove != null;
            String[] labString  = labAbove.text().split(" ");

            if (Objects.equals(labString[0].toLowerCase(), labName.toLowerCase())){

                // Construct and return the lab URL from the 'href' attribute
                return STR."https://helios.utcluj.ro/LEARN2CODE/\{labHref.select("a.href2").attr("href")}";
            }
        }
        throw new IllegalArgumentException(STR."Lab number\{labNumber}does not exist");
    }


    public static void main(String[] args) throws IOException {
        Helios helios = new Helios("name","password");
        helios.logIn();


    }
}
