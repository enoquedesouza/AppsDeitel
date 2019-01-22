package deitel.exemplos.flagquiz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Set;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

/*
* A classe principal carrega o fragmento QuizFragment, onde a interação principal com o aplicativo é realizada.
* A configuração inicial do aplicativo é feito com características padrão que são salvas em um arquivo SharedPreferences
* do android. Os valores para o número de regiões e quantidade de palpites para a resposta são carregados deste arquivo,
* um variável booleana é utilizada para armazenar o estado das informações gravadas no arquivo, se essas informações forem
* alterada então a interface gráfica é atualizada com as novas preferências. Para monitorar essas alterações um
* SharedPreferencesChangedListener é registrado para o arquivo criado.
* A atividade também monitora se o aplicativo está sendo executado em um telefone no modo paisagem ou em um outro dispositivo
* de tela grande, como um tablet. Se está em um telefone no modo paisagem o layout é forçado a ficar no modo retrato. Um layout
* Extra é criado para a execução no modo paisagem de um tablet.
*
* */
public class MainActivity extends AppCompatActivity {

    // Chaves para o arquivo Sharedpreferences, onde serão aramenadas as preferências de
    // configuração do app para o usuário.
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean telefone = true; // moitora o tipo de dispositivo (telefone ou outro, como tablet)
    private boolean mudou_preferencias = true; // Monitora se as preferências do usuário são alteradas



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configura o arquivo SharedPreferences com os valores prefences.xml
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //Registra o listener para as alterações no SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(preferenceChangedListener);

        //Tamanho da tela
        int tamanhoTela = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;


        // Pelo tamamnho da tela determina se o dispositivo é um tablet.
        if(tamanhoTela == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                tamanhoTela == Configuration.SCREENLAYOUT_SIZE_XLARGE ){

                telefone = false; // Não é um telefone, mas um tablet.

        }


        // Força o aparelho a ficar na orientação retrato.
        if(telefone){

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }

    }






    /*
      *Ao iniciar o aplicativo a configurações gravadas no SharedPreferences são carregadas, pois mudou_preferencias
      * está setado inicialmento como true. Sempre que a atividade principal sair de foco, para por exemplo, o usuário
      * modificar as configurações ao retornar para a atividade principal onStart será chamado novamente e as configrações
      * serão recarregadas.
     */
    @Override
    protected void onStart(){

        super.onStart();


        // Se o arquivo SharedPreferences foi alterado.
        if(mudou_preferencias){

            QuizFragment quizFragment = (QuizFragment)getFragmentManager() // Recupera uma referência do fragment onde o quiz
                    .findFragmentById(R.id.quizFragment); // está programado.

            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this)); // Oom a referência de WuizFragment
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this)); // Os metoddos para a sua configuração
            quizFragment.resetQuiz(); // são acessados.
            mudou_preferencias = false;
        }
    }





    // Cria o Menu de options por onde o usuario pode acessar a atividade para realizar as configurações no quiz que sejam
    // do seu interesse. NO entanto o menu é inflado somenta paa a orientação retrato. Na oritação paisagem os opções são
    // carregadas em um fragmento a parte no layout para esta configuração.
    @Override
    public boolean onCreateOptionsMenu( Menu menu){

        Display tela = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay(); //Recupera uma referência para
                                                                                                // para a tela do dispositivo.
        Point tamanhoDaTela = new Point(); // O objeto point para armezenar as dimensões da tela.
        tela.getRealSize(tamanhoDaTela);

        // Se largura menor que altura, então está o no modo retrado e o menu é inflado.
        if(tamanhoDaTela.x < tamanhoDaTela.y){

            //Então é retrato. O menu deve ser inflado.
            getMenuInflater().inflate(R.menu.menu, menu); // layout do menu.
            return true;
        }else {
            // Está no modo paisagem então o menu não é inflado.
            return false;
        }
    }





    // Este método é chamado quando um item do menu é selecionado.
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        // A atividade para a seleção das configurações é iniciada.
        Intent intenteDePreferencias = new Intent(this, SettingsActivity.class);
        startActivity(intenteDePreferencias);
        return super.onOptionsItemSelected(item);
    }



    /*
    *  Classe interna para monitorar quando alterações no arquivo SharedPreferences é realizada.
    * */

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangedListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    mudou_preferencias = true; // mudou_preferencias é setado para true

                    QuizFragment quizFragment = (QuizFragment)getFragmentManager() // recupera uma referência de QuizFragment
                            .findFragmentById(R.id.quizFragment); // para acessar o mpetodos od quiz.

                    //Key indentifica que conjunto de dados foi alterado no arquivo
                    // Se foi CHOICES, então o número de palpites foi alterado,
                    if(key.equals(CHOICES)){

                        // Se número de palpites foi alterado o número de opçoes mostrado ao usuário é atualizado.
                        quizFragment.updateGuessRows(sharedPreferences);
                        quizFragment.resetQuiz();
                    }

                    // Se foi REGIONS então o número de regions considerada no quiz é atualizado
                    else if(key.equals(REGIONS)){

                        Set<String> regions = sharedPreferences.getStringSet(REGIONS,null);

                        if(regions != null && regions.size() > 0){

                            quizFragment.updateRegions(sharedPreferences);
                            quizFragment.resetQuiz();

                        }else{

                            // Se o usuário não escolheu nenhuma região america do norte é setado como padrão.
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            regions.add(getResources().getString(R.string.default_region));
                            editor.putStringSet(REGIONS, regions);
                            editor.commit();

                            // Exibe uma mensagem infromativa para o usuário.
                            Toast.makeText(MainActivity.this, R.string.default_region_message,
                                    Toast.LENGTH_SHORT).show();

                        }
                    }

                    Toast.makeText(MainActivity.this, R.string.restarting_quiz,
                            Toast.LENGTH_SHORT).show();
                }

    };


}
