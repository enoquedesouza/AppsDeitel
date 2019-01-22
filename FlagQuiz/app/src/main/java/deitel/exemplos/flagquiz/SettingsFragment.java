package deitel.exemplos.flagquiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Enoque on 15/01/2019.
 *
 * Fragmento para exibir as opções de configurações para o usuário.
 */

public class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInsanceBundle){

            super.onCreate(savedInsanceBundle);
            addPreferencesFromResource(R.xml.preferences);

        }
}
