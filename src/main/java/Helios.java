import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Helios {

    final OkHttpClient client = new OkHttpClient();

    String run(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()){
            assert response.body() != null;
            return response.body().string();
        }
    }

    public static void main(String[] args) throws IOException {
        Helios helios = new Helios();
        String response = helios.run("https://helios.utcluj.ro/LEARN2CODE/login.php?SID");
        System.out.println(response);

    }
}
