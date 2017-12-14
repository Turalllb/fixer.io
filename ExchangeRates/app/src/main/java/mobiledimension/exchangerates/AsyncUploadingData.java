package mobiledimension.exchangerates;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Турал on 28.11.2017.
 */

public class AsyncUploadingData extends AsyncTask<String, Void, Void> {

    private String answer;
    private AsyncResult mCallback;


    interface AsyncResult {
        void getResult(String answer);
    }

    AsyncUploadingData(AsyncResult mCallback) {
        this.mCallback = mCallback;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... urls) {
        getAnswer(urls);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mCallback.getResult(answer);

    }


    private void getAnswer(String... urls) {
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);

            int code = connection.getResponseCode();

            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf8"));
                answer = "";
                String line = null;

                while ((line = reader.readLine()) != null) {
                    answer += line;
                }
                reader.close();

            }
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
