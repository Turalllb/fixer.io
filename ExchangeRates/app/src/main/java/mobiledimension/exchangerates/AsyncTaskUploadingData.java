package mobiledimension.exchangerates;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import static mobiledimension.exchangerates.MainMenu.LOG_TAG;

/**
 * Created by Турал on 28.11.2017.
 */

class AsyncTaskUploadingData extends AsyncTask<String, Void, String> {
    private WeakReference<AsyncTaskResult> weakReferenceCallback;

    AsyncTaskUploadingData(AsyncTaskResult mCallback) {
        //Если Активити,вызвавший AsyncTask уничтожится, GC удалит ссылку на активити
        weakReferenceCallback = new WeakReference<>(mCallback);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... urls) {
        return getAnswer(urls);
    }

    @Override
    protected void onPostExecute(String answer) {
        super.onPostExecute(answer);
        try {
            weakReferenceCallback.get().getResult(answer);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Активити была пересоздана");
        }
    }

    private String getAnswer(String... urls) {
        String answer = null;
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(500);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf8"));
                answer = "";
                String line;

                while ((line = reader.readLine()) != null) {
                    answer += line;
                }
                reader.close();
            }
            connection.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return answer;
    }

    interface AsyncTaskResult {
        void getResult(String answer);
    }
}
