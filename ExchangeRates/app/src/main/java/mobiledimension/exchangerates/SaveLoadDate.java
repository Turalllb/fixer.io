package mobiledimension.exchangerates;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Турал on 01.12.2017.
 */
class SaveLoadDate {

    private Context mContext;

    SaveLoadDate(Context context) {
        this.mContext = context;
    }

    void SaveDate(String answer, String date, String CurrentCurrency) {
        final String FILENAME_SD = CurrentCurrency + "_" + date;
        final String DIR_SD = "ExchangeRates";
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(mContext, "SD-карта не доступна ", Toast.LENGTH_LONG).show();
            return;
        }

        File SdPath = Environment.getExternalStorageDirectory();
        SdPath = new File(SdPath.getAbsoluteFile() + "/" + DIR_SD);
        SdPath.mkdirs();
        File SdFile = new File(SdPath, FILENAME_SD);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(SdFile));
            bw.write(answer);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(mContext, "СОХРАНЕНО ", Toast.LENGTH_LONG).show();
    }


    String LoadDate(String FilePath) {
        String answer = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(FilePath));
            String line = null;
            while ((line = br.readLine()) != null) {
                answer += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }


}
