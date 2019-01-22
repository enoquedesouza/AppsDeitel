package deitel.exemplos.flagquiz;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Enoque on 17/01/2019.
 *
 * Atividade que hospeda o fragmento para configurações do usuário.
 */

public class SettingsActivity extends Activity{

    @Override
    public void onCreate(Bundle savedInsanceBundle){

        super.onCreate(savedInsanceBundle);
        setContentView(R.layout.activity_settings);

    }
}
