package deitel.exemplos.flagquiz;

import android.app.DialogFragment;
import android.app.Fragment;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;



/**
 * Created by Enoque on 15/01/2019.
 *
 * Fragmento onde o quiz é programado. Neste fragmento ocorrerá a interação principal do usuário, é onde ele responderá as
 * perguntas do quiz.
 */

public class QuizFragment extends Fragment{


        // Flag para o arquivo de log do android.
        private static final String TAG = "Flag Quiz Activity";


        // Define um valor para o número de adivinhações que serão solicitadas no teste.
        private int FLAGS_IN_QUIZ = 10;

        private List<String> fileNameList; // Armazena os endereços dos arquivos de imagem das bandeiras utilizadas no teste,.
        private List<String> quizCoutriesList; // Lista dos nomes das bandeira utilizadas no quiz
        private Set<String>  regionsSet; // Conjunto de regiões utilizadas no teste.
        private String correctAnswer; // Armazena responsta correta.
        private int totalGuesses; // Total de opções de respostas que o usuário deseje que apareça
        private int correctAnswers; // Armazena a resposta correta
        private int guessRows; // Variável de controle para o número de linhas, ou linear layouts, que são utilizados para
                               // armazenas os botões dos palpites.
        private SecureRandom random; // Utilizado para gerar um número aleatório.
        private Handler handler; //Utilizado para gerar um atraso de 2s entre uma resposta e a próxima bandeira.
        private Animation shakeAnimation; // Animação utilizada para tremer a bandeira quando a resposta estiver errada.

        private TextView questionNumberTextView; //TextView que exibe o prograaso do teste.
        private ImageView flagImageView; // ImageView que exibe a bandeira a ser adivinhada
        private LinearLayout[] guessLinearLayouts; //LinearLayouts que acomodam o botões com as respostas.
        private TextView answerTextView; // Exibe a resposta escolhida pelo usuário.




    /*
    *  Método é chamado quando o View que receberá o fragmento é chamado. Quando isto é feito os elementos
    *  gráficos são acessados e configurados. A inicialização das outras variáveis de instância do classe
    *  também é feita neste momento.
    */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedStateBundle){

        super.onCreateView(inflater, container, savedStateBundle);

        View view = inflater.inflate(R.layout.fragment_quiz, container, false);


        fileNameList = new ArrayList<String>(); // Define ArrayList para os endereços dos arquivos de imagem das bandeiras
        quizCoutriesList = new ArrayList<String>(); // ArrayList para os nomes das badeiras consideradas
        random = new SecureRandom(); //Crie uma objeto para a geração de números aletórios
        handler = new Handler(); // Handler para gerar um atraso.
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),//Configura a animação com a configuração
                R.anim.incorrect_shake); // difinida no arquivo xml incorrect_shake
        shakeAnimation.setRepeatCount(3); // Número de vez que a nimação deve ser repetida quando chamada.

        // Recupera os views para os elementos gráficos difnidos no layout do fragmento.
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[3];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);

        answerTextView = (TextView) view.findViewById(R.id.answerTextview);

        for( LinearLayout row : guessLinearLayouts){

            for(int i = 0; i < row.getChildCount(); i++){

                Button button = (Button) row.getChildAt(i);
                button.setOnClickListener(guessButtonListener);
            }
        }

        questionNumberTextView.setText(getResources().getString(R.string.question, 1, FLAGS_IN_QUIZ));

        return view;

    }




    /*
    *  Atualiza o número de linhas de palpites, ou seja, o número de LinearLayouts utilizados para armazenar os
    *  botões de palpite. Esta atualização é feita consultando o arquivo SharedPreferences definido.
    */

    public void updateGuessRows(SharedPreferences preferences){

            String choices = preferences.getString(MainActivity.CHOICES, null);

            // No arquivo são armazenados o números de botões de palpites escolhido pelo usuário (3, 6 ou 9)
            // O número de linhas será este número divido por três, uma vez que cada linha armazena 3 botões.
            guessRows = Integer.parseInt(choices)/3;

           // Primeiro deixa todos invisíveis
           for(LinearLayout layout : guessLinearLayouts){

                    layout.setVisibility(View.INVISIBLE);

            }

            // Depois deixa visível somente a quantidade escolhida pelo usuário
            for(int row = 0; row < guessRows; row++){

                    guessLinearLayouts[row].setVisibility(View.VISIBLE);

           }

    }



    // Recupera as regiões salvas no arquivo para definir as bandeiras que serão utilizadas no quiz.
    public void updateRegions(SharedPreferences preferences){


        regionsSet = preferences.getStringSet(MainActivity.REGIONS, null);


    }

    // Reseta o jogo preparando o próximo quiz.
    public void resetQuiz(){

        AssetManager assets = getActivity().getAssets(); // Define uma referência para acessar a pasta assets onde estão
                                                        // os arquivos de imagem das bandeiras

        fileNameList.clear();

       try{

           //As bandeiras estão separadas por região na pasta assets
            for(String region : regionsSet){

                String [] paths = assets.list(region); // Lista os caminhos para cada imagem em cada região regionsSet
                for(String path : paths){
                   fileNameList.add(region +"-" + path.replace(".png", "")); // Popula fileNamelist com os nomes dos aaruivos das
                                                                              // das imagens
                }
            }

        }catch (IOException exception){

            Log.e(TAG, "Erro ao carregar o nomes dos arquivos das imagens", exception);
        }


        correctAnswers = 0;
        totalGuesses = 0;
        quizCoutriesList.clear();

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        // Esta condicional não no app original, ela foi adicionada para considerar o caso em que uma região
        // possui um número de bandeiras inferior ao estabelecido em FLAGS_IN_QUIZ e assim evitar diversos erro que podem ocorer.
        if(numberOfFlags < FLAGS_IN_QUIZ){

            FLAGS_IN_QUIZ = numberOfFlags;

        }


        //Escolhe aleatoriamente os nomes das bandeiras e popula a lista dos nomes dos países.
        while(flagCounter <= FLAGS_IN_QUIZ){

           int randomIndex = random.nextInt(numberOfFlags);

            String fileName = fileNameList.get(randomIndex);

            if(!quizCoutriesList.contains(fileName)){

               quizCoutriesList.add(fileName);
               flagCounter++;
            }
        }

        loadNextFlag();
    }


    // Carrega a próxima bandeira a ser exibida no quiz.

    public void loadNextFlag(){

        String nextImage = quizCoutriesList.remove(0); // A próxima bandeira é sempre o primeiro elemento de quizCountriesList
        correctAnswer = nextImage;
        answerTextView.setText("");


        questionNumberTextView.setText(getResources().getString(R.string.question,
                (correctAnswers + 1), FLAGS_IN_QUIZ));

        String region = nextImage.substring(0, nextImage.indexOf('-'));
        String file = nextImage.substring(nextImage.indexOf('-') + 1, nextImage.length());

        AssetManager asset = getActivity().getAssets();

        try{

            InputStream stream = asset.open(region + "/" + file + ".png"); // Abre um stream para o arquivo de imagem de endereço
                                                                            // passado como argumento
            Drawable flag = Drawable.createFromStream(stream, nextImage); // Desenha a imagem a partir do stream
            flagImageView.setImageDrawable(flag); // Exibe a imagem na tela.

        }catch (IOException exception){

            Log.e(TAG, "Error Loading " + nextImage, exception);
        }

        // Embaralha e reposicona a resposta correta na lista para ser exibida em uma botão aletório na interface gráfica
        Collections.shuffle(fileNameList);

        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));


        // Seta os textos dos botões com palpites de resposta a ser escolhida pelo usuário.
        for(int row = 0; row < guessRows; row++){

            for(int column = 0; column < guessLinearLayouts[row]. getChildCount(); column++){

                Button novoGuessButton = (Button)  guessLinearLayouts[row].getChildAt(column);
                novoGuessButton.setEnabled(true);

                String fileName = fileNameList.get((row*3) + column);
                novoGuessButton.setText(getCountryName(fileName));

            }
        }

        //  Novamente escolhe aleatoriamento um botão para colocar a resposta correta
        int row = random.nextInt(guessRows); // Pega uma linha aletória
        int column = random.nextInt(3); // Pega uma coluna aletória
        LinearLayout randomRow = guessLinearLayouts[row];
        String coutryName = getCountryName(correctAnswer); // Texto da reposta correta
        ((Button)randomRow.getChildAt(column)).setText(coutryName); // Seta o botão com a resposta correta

    }

    // Retorna o nome da bandeira formatado corretamente, apenas o nome do país correspondente.
    private String getCountryName(String name){

        return name.substring(name.indexOf('-')+ 1, name.length());

    }

    // Classe interna criada para trata os eventos de click nos botões
    private OnClickListener guessButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            Button guessButton = (Button) v; // Recupera o botão onde houve o click
            String guess = guessButton.getText().toString(); // Pega o seu texto (nome da bandeira) dado como palpite.
            String answer = guess; // Configrua a resposta do usuário com o o texto do botão
            ++totalGuesses; // Incrementa o avariável que controla a quantidade de tentivas do usuário

            // Se o palpite do usuário está correto
            if(guess.equals(getCountryName(correctAnswer))){

                ++correctAnswers; // incrementa o número de respostas corretas

                answerTextView.setText(answer + "!"); // Apresenta a reposta na cor verde
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer, null));
                disableButtons(); // Desabilita todos os botões

                // Se o número de respostas corretas é igual ao total deinido para quiz
                if(correctAnswers == FLAGS_IN_QUIZ){

                    // Cria e exibe uma caixa de diálogo com os resultados no teste.

                    DialogFragment quizRexults = new DialogFragment(){

                            @Override
                            public Dialog onCreateDialog(Bundle bundle){

                                // O DialogFragment armazena um Alertdialog
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setMessage(getResources().getString(R.string.results,
                                            totalGuesses, ((FLAGS_IN_QUIZ /(double) totalGuesses))*100));

                                //E exibe ao usuário a opção para reiniciar o teste.
                                alertDialogBuilder.setPositiveButton(R.string.reset_quiz, new DialogInterface.
                                            OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                resetQuiz();
                                            }

                                            ;
                                        }

                                );

                                return alertDialogBuilder.create();
                            }
                    };

                    quizRexults.show(getFragmentManager(), "quiz results");

                }else{

                    // Se não chegou ao fim, então aguarde 2s e carregue a próxima bandeira
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {

                                    loadNextFlag();
                                }
                            }, 2000
                    );
                }


            }else{

                // Se o palpite do usuário não é a resposta correta, então starta a animação
                flagImageView.startAnimation(shakeAnimation);
                answerTextView.setText(R.string.incorrect_answer); // Seta o TextView da reposta como incorreto
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, null)); // Na cor vermelha
                guessButton.setEnabled(false); // e desabilita o botão com o palpite do usuário.
            }

        }
    };

    // Disabilita todos os botões.
    private void disableButtons(){

        for (int row = 0; row < guessRows; row++)
        {
            LinearLayout guessRow = guessLinearLayouts[row];

            for (int i = 0; i < guessRow.getChildCount(); i++)

                guessRow.getChildAt(i).setEnabled(false);

            }
    }

}
