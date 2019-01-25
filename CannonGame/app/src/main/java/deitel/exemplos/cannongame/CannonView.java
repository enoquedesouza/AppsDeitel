package deitel.exemplos.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;


/**
 * Created by Enoque on 22/01/2019.
 *
 *  Esta classe é onde o jodo se desenrola. É um SurfaceView onde serão desenhados os elementos gráficos do jogo e
 *  frame a frame esses elementos vão sendo atualizados no desenrolar da partida.
 *  A classe implementa a interface SurfaceHolder.Callback para ser possível monitorar as alterações que acontecem
 *  no SurfaceView. Desta forma também pe possível recuperar o objeto SurfaceHolder responsável por gerenciar o SurfaceView;
 *  Com o SurfaceHolder, passado á thread responsável pela dinâmica do jogo, se obtém o objeto Canvas associado
 *  e assim se torna possível desenhar na superfície SurfaceView;
 *
 */

public class CannonView extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "CannonView"; // Tag de identificação da classe no arquivo de log de android.
    private CannonThread cannonThread; // Thread responsável por atualizar frame a frame os elementos gráficos desenhados.
    private Activity activity; // Referência para a atividade principal.
    private boolean dialogIsDiplayed = false; // Monitora a exibição da caixa de dialogo com os resultados do jogo
    public static final int TARGET_PIECES = 7; // Numero de partes do alvo.
    public static final int MISS_PENALTY = 2; // Quando o usuário acertar a barreira será penalizado com o decréscimo do tempo
                                              // para concluir o jogo por este fator.
    public static final int HIT_REWARD = 3; // Quando o usuário acertar o alvo ele é recompensado com o acréscimo no tempo
                                           // para concluir o jogo por este fator.

    private boolean gameOver; // Monitora o fim do jogo.
    private double timeLeft; // Armazena o tempo restante para acertar todos os alvos.
    private int shotsFired; // Armazena os tiros disparados
    private double totalElapsedTime; // Monitora o tempo total passado desde o início do jogo.

    private Line blocker; // A barreira nada mais do um objeto linha com comprimento, largura e posição definidas
    private int blockerDistance;  // Armazena a distância da barreira a partir do lado esquerdo da tela
    private int blockerBeginning; //  Ponto inicial, vertical, onde a barreira inicia
    private int blockerEnd; // ponto final, vertical, onde a barrira termina
    private int initialBlockerVelocity; // armazena velocidade de deslocamento, inicial, do barreira
    private float blockerVelocity; // Armazena a velocidade da barreira ao longo do jogo.


    private Line target; // O alvo é uma linha que será dividida em 7 segmentos, PIECES
    private int targetDistance; // Distância do alvo a partir do lado esquerdo da tela
    private int targetBeginning; // Ponto inicial do alvo, na direção vertical.
    private double pieceLength; // Comprimento do segmentos de linha nos quais o alvo será divido
    private int targetEnd; //Posição final do alvo, na direção vertical
    private int initialTargetVelocity; // Velocidade inicial do alvo
    private double targetVelocity;// Velocidade do alvo, desendolar do jogo.

    private int lineWidth; // Largura da linha
    private boolean [] hitStates; // Armzena o estado das seções do alvo, se estas seções foram atingidas ou não.
    private int targetPiecesHit; // Armazena o número de seções do alvo que foram atingidas

    //Definição da bola
    private Point cannonBall; // Bala de canhão, definida por um ponto, circundado por um raio.
    private int cannonBallVelocityX; // velocidade da bala na direção x
    private int cannonBallVelocityY; // velocidade da bala na direção y
    private boolean cannonBallOnScreen; // Monitora se há uma uma bala de canhão na tela
    private int cannonBallRadius;// Raio da bala de canhão
    private int cannonBallSpeedy;// Velocidade da bala de canhão
    private int cannonBaseRadius; // Raio da base do canhão
    private int cannonLength; //Comprimento do cano do canhão
    private Point barrelEnd; // posição da extremidade do cano do canhão
    private int screenWidth; // largura da tela
    private int screenHeight; // altura da tela

    private static final int TARGET_SOUND_ID = 0; // id para o som da bala de canhão atingindo alvo
    private static final int CANNON_SOUND_ID = 1; // id para o som do disparo do canhão
    private static final int BLOCKER_SOUND_ID = 2; // id para o som da bala de canhão atingindo a barreira
    private SoundPool soundPool; // o bjeto SoundPool armazena recursos de áudio utilizados no app
                                 // esses recursos de áudio ficam armazenados na pasta raw do projeto
    private SparseIntArray soundMap; // Mapeia um id para um recurso de áudio


    // Objetos paint para cada elemento gráfico. Estes objetos são utilizados pelo Canvas da superfífica
    // para que sejam desenhado da maneira apropriada. Como foram definidas no Paint correspondente.
    private Paint textPaint; // objeto Paint usado para desenhar texto
    private Paint cannonballPaint; // objeto Paint usado para desenhar a bala de canhão
    private Paint cannonPaint; // objeto Paint usado para desenhar o canhão
    private Paint blockerPaint; // objeto Paint usado para desenhar a barreira
    private Paint targetPaint; // objeto Paint usado para desenhar o alvo
    private Paint backgroundPaint; // objeto Paint usado para limpar a área de desenho


    /*
    * Contrutor de CannonView
    * @param context é o contexto da atividade ao o view está associado.
    * @attrs é o objeto AttributeSe que carrega as característica de layout para a superfície.
    * Permitem que a superfície seja incializada corretamente.
    * */

    public CannonView(Context context, AttributeSet attrs) {

        super(context, attrs); // Informações são passadas para a super classe

        activity = (Activity) context; // Define activity passada como parâmetro, é uma forma
                                        // de referenciar a atividade utilizada hospedeira.

        getHolder().addCallback(this); // passa o callback para o holder da superfífice
                                       // o callback é a referência ao própria superfície, ja que ela implementa
                                      //o SurfaceHolder.Callback


        blocker = new Line(); //instancia um objeto linha para representar a barreira
        target = new Line(); // outro objeto linha para representar o alvo
        cannonBall = new Point(); // a bala de canhão é representa como um ponto

        hitStates = new boolean[TARGET_PIECES]; // para armazenar os estados das seções do alvo

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0); // dfine conjunto de objetos áudio que devem ser
                                                                    // excutados como streams de musicas

        soundMap = new SparseIntArray(3); // para maper áudio - identificador

        // Realiza o mapeamento áudio-identificador
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));

        // Objetos paint para desenhar cada componente gráfico do jogo
        textPaint = new Paint();
        cannonPaint = new Paint();
        cannonballPaint = new Paint();
        blockerPaint = new Paint();
        targetPaint = new Paint();
        backgroundPaint = new Paint();

    }


    // Se o tamanho da superfície mudar, adequada os componentes para as novas dimensões da superfície

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH){

        super.onSizeChanged(w, h, oldW, oldH);

        screenWidth = w;
        screenHeight = h;
        cannonBaseRadius = h /18;
        cannonLength = w/8;

        cannonBallRadius = w/36;
        cannonBallSpeedy = w*3/2;
        lineWidth = w/24;

        blockerDistance = w*5/8;
        blockerBeginning = h/8;
        blockerEnd = h*3/8;
        initialBlockerVelocity = h/2;

        blocker.pInicial = new Point(blockerDistance, blockerBeginning);
        blocker.pFinal = new Point(blockerDistance, blockerEnd);

        targetDistance = w*7/8;
        targetBeginning = h/8;
        targetEnd = h*7/8;
        pieceLength = (targetEnd - targetBeginning)/TARGET_PIECES;
        initialTargetVelocity = -h/4;
        target.pInicial = new Point(targetDistance, targetBeginning);
        target.pFinal = new Point(targetDistance, targetEnd);

        barrelEnd = new Point(cannonLength, h/2);

        textPaint.setTextSize(w/20);
        textPaint.setAntiAlias(true);

        cannonPaint.setStrokeWidth(lineWidth*1.5f);;
        blockerPaint.setStrokeWidth(lineWidth);
        targetPaint.setStrokeWidth(lineWidth);
        backgroundPaint.setColor(Color.WHITE);

        newGame();
    }



    // Configura uma nova partida
    public void newGame(){

        for(int i = 0; i<TARGET_PIECES; i++) // Habilita todas as seções do alvo
            hitStates[i] = false;

        targetPiecesHit = 0; // nenhuma seção foi atingida
        blockerVelocity = initialBlockerVelocity;  // redefine as velocidades do objeto com o valor inicial
        targetVelocity = initialTargetVelocity;
        timeLeft = 10; // resenta o restante para o seu valor inicial
        cannonBallOnScreen = false; // retira bola da tela
        shotsFired = 0; // reseta a contagem de tiros disparados
        totalElapsedTime = 0; //reseta o tempo transcorrido

        blocker.pInicial.set(blockerDistance, blockerBeginning); // seta a posição inicial da barreira
        blocker.pFinal.set(blockerDistance,blockerEnd); // seta a posicção final da barreira
        target.pInicial.set(targetDistance, targetBeginning); // seta a posição inicial do alvo
        target.pFinal.set(targetDistance, targetEnd); // seta a posição final do alvo

        // se o jogo acabou
        if(gameOver){

            gameOver = false; // Jogo então não acabou, pois reinciado
            cannonThread = new CannonThread(getHolder()); // instancia a thread para desenhar na superfície
            cannonThread.start(); // inicia o jogo
        }
    }


    // Método que atualiza a posição do elementos gráficos a cada frame de elpsedTimeMS milisegundos
    ///@param elapsedTimeMS tempo decorrido entre uma atualização e outra do frame, dado em milisegundos
    private void updatePositions(double elapsedTimeMs){

        double interval = elapsedTimeMs/1000;  // converte para segundos

        // se a bala de canhão está na tela, (bola de canhão fica estranho)
        if(cannonBallOnScreen){

            // atualiza sua posição de acorodo com sua velocidade de deslocamento
            // a sua posição atual é posição anterior mais a velocida x o tempo trancorrido entre uma aatualização e outra
            cannonBall.x += interval*cannonBallVelocityX;
            cannonBall.y += interval*cannonBallVelocityY;

            // Verifica se houve colisão com a barreira , utilizando colisão simples
            if(cannonBall.x + cannonBallRadius > blockerDistance &&
                    cannonBall.x - cannonBallRadius < blockerDistance &&
                    cannonBall.y + cannonBallRadius > blocker.pInicial.y &&
                    cannonBall.y - cannonBallRadius < blocker.pFinal.y){


                cannonBallVelocityX *= -1; // Se colidiu com a barreira, inverte a direção do movimento
                timeLeft -= MISS_PENALTY; // penalisa o jogador decrementanto 2 segundos do tempo para atingir todos alvos
                soundPool.play(soundMap.get(BLOCKER_SOUND_ID), 1, 1, 1, 0, 1f); // executa o som da bala colidindo com a barreira



            //Se a bala atingir os limites horizontais (esquerda|direita) da tela
            }else if(cannonBall.x + cannonBallRadius > screenWidth ||
                     cannonBall.x - cannonBallRadius < 0){

                    cannonBallOnScreen = false; // retire a bala da tela


            //Ou se a bala atingir os limites verticais (superior|inferior) da tela
            }else if(cannonBall.y +cannonBallRadius > screenHeight ||
                    cannonBall.y - cannonBallRadius < 0){

                    cannonBallOnScreen = false; // retire a bola da tela


             // Ou se a bala atingiu os limites do alvo
            }else if(cannonBall.x + cannonBallRadius > targetDistance &&
                    cannonBall.x - cannonBallRadius < targetDistance &&
                    cannonBall.y + cannonBallRadius > target.pInicial.y &&
                    cannonBall.y - cannonBallRadius < target.pFinal.y){

                    // Define a seção do alvo
                    int secao = (int) ((cannonBall.y - target.pInicial.y)/pieceLength);

                    // se não for até a última secção  e ela não foi atingida antes
                    if((secao >= 0 && secao < TARGET_PIECES) && !hitStates[secao]){

                            hitStates[secao] = true; // seta a seção como atingida
                            cannonBallOnScreen = false; // retire a bola da tela
                            timeLeft += HIT_REWARD; // premie o jogoador adicionando 3 segundos ao tempo restante
                                                    // para acertar as outras seções

                        //toque o som da bala atingindo o alvo
                        soundPool.play(soundMap.get(TARGET_SOUND_ID), 1, 1, 1, 0, 1f);


                        //Se atingiu todas as seção
                        if(++targetPiecesHit == TARGET_PIECES){

                            cannonThread.setRunning(false); // para a execução da thread
                            showGameOverDialog(R.string.win); // exibe a caixa de diálogo com o resumo do jogo e
                                                            // informando que o jogoador venceu

                            gameOver = true; // termina o jogo
                        }

                    }

            }

        }

        // atualiza a posição da barreira fazendo-a se desclocar verticamelmente por fator blockerUpdate
        // o sentido de deslocamento é dado pelo sentido da velocidade do objeto, muda o ao tocar na parte superior
        // ou inferior da tela
        double blockerUpdate = interval*blockerVelocity;
        blocker.pInicial.y += blockerUpdate;
        blocker.pFinal.y += blockerUpdate;

        double targetUpdate = interval*targetVelocity;
        target.pInicial.y += targetUpdate;
        target.pFinal.y += targetUpdate;

        // Invertido o sentido das velocidades quando os objetos tocam na parte superior ou inferior da tela
        if (blocker.pInicial.y < 0 || blocker.pFinal.y > screenHeight)
              blockerVelocity *= -1;

        if (target.pInicial.y < 0 || target.pFinal.y > screenHeight)
              targetVelocity *= -1;

        // Decrementa o tempo restante
        timeLeft -= interval;

        // se o tempo acabou
        if (timeLeft <= 0.0) {

             timeLeft = 0.0;
             gameOver = true; // o jogo terminou
             cannonThread.setRunning(false); // termina a thread
             showGameOverDialog(R.string.lose); // mostra caixa de diálogo de derrota
        }


    }



    // dipara uma bala de cahão
    //@param event é o objeto motion event que armazena o evento de toque no na tela do dispositivo.
    public void fireCannonBall(MotionEvent event){

        // Se uma bala está na tela então, não dispara outra
        if(cannonBallOnScreen)
            return;

        double angle = alignCannon(event); // angulo de disparo é obtido de acordo com o toque na tela do jogador
                                           //o cano do canhão é alinhado seguidno este agulo

        cannonBall.x = cannonBallRadius; // configura as coordenadas da bala
        cannonBall.y = screenHeight/2;

        cannonBallVelocityX = (int) (cannonBallSpeedy*Math.sin(angle)); // define a velocidade de acordo com as componentes
        cannonBallVelocityY = (int) (-cannonBallSpeedy*Math.cos(angle)); // vertico e horizontal do movimento

        cannonBallOnScreen = true; // tiro disparado

        ++shotsFired; // incrementa o número de tiros disparados

        soundPool.play(soundMap.get(CANNON_SOUND_ID), 1,1,1,0, 1f); // executa o som de disparo


    }




    //Alinha o cano do canhão de acordo com o âgulo de orientação, o ângulo é obtido a partir do alinhamento
    // do cano com o ponto onde ocorreu o to que na tela. Esta será a direção do diparo
    //@param event é o objeto motion event que armazena o evento de toque no na tela do dispositivo.

    public double alignCannon(MotionEvent event){

        Point touchPoint = new Point((int) event.getX(), (int) event.getY()); //Recupera as coordenada do ponto onde o jogador
                                                                                // tocou na tela

        double centerMinusY = (screenHeight/2 - touchPoint.y); // estabelece o centro da tela, na vertical, como referência

        double angle = 0; // inicializa o angulo com 0

        // Se  o toque não foi no centro
        if(centerMinusY != 0)
            angle = Math.atan((double) touchPoint.x/centerMinusY);

        // Se o to que foi na parte inferior o ângulo é corrigido por 180º
        // Este ângulo está entre o vetor que vai da origem até o ponto, com o eixo vertical(y).
        if(touchPoint.y > screenHeight/2)
            angle += Math.PI;

        // define os novos pontos finais do cano do canhão para ser desenhado novamente
        barrelEnd.x = (int) (cannonLength*Math.sin(angle));
        barrelEnd.y = (int) (-cannonLength*Math.cos(angle) + screenHeight/2);

        return angle;
    }

    // Método utilizado para desenhar o elementos na tela.
    //@param canvas, objeto  Canvas da superfície utilizado para desenhar os camponentes.
    // Este parâmetro é passado pela Thread e recuperado pelo SurfaceHolder que gerencia a superfície.
    public void drawGameElements(Canvas canvas){

        //Desenha um fundo branco na superfície
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        //Desenha o texto que informa o tempo transcorrido
        canvas.drawText(getResources().getString(R.string.time_remaining_format, timeLeft), 30, 50, textPaint);

        //Se a bala está na tela
        if (cannonBallOnScreen)
            //desenha a bala de canhão
            canvas.drawCircle(cannonBall.x, cannonBall.y, cannonBallRadius, cannonballPaint);

        //desenha o cano do canhão
        canvas.drawLine(0, (int) screenHeight / 2, barrelEnd.x, barrelEnd.y, cannonPaint);

        //desenha a base do canhão
        canvas.drawCircle(0,(int) screenHeight / 2, cannonBaseRadius, cannonPaint);
        //desenha a barreira
        canvas.drawLine(blocker.pInicial.x, blocker.pInicial.y, blocker.pFinal.x, blocker.pFinal.y, blockerPaint);

        Point currentPoint = new Point();

        //coordenadas para a posição atual do alvo na tela
        currentPoint.x = target.pInicial.x;
        currentPoint.y = target.pInicial.y;

        //Desenha as seções do alvo
        for(int i = 0; i<TARGET_PIECES; i++ ){

            if(!hitStates[i]){

                if(i % 2 == 0) // seções pares são definidas a cor azul
                    targetPaint.setColor(Color.BLUE);
                else
                    targetPaint.setColor(Color.YELLOW); // seções ímpares de amarelo

                //Desenha a seção
                canvas.drawLine(currentPoint.x, currentPoint.y, target.pFinal.x,
                        (int) (currentPoint.y + pieceLength), targetPaint );
            }

            currentPoint.y += pieceLength;
        }

    }

    //Exibe um DialogFragment com a memnsagem passada pelo parâmetro
    private void showGameOverDialog(final int messageId){

        final DialogFragment gameResult = new DialogFragment(){

            @Override
            public Dialog onCreateDialog(Bundle bundle){

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(messageId));

                builder.setMessage(getResources().getString(R.string.results_format, shotsFired,
                                    totalElapsedTime));

                builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialogIsDiplayed = false;
                        newGame();

                    }
                });

                return builder.create();

            }

        };

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                dialogIsDiplayed = true;
                gameResult.setCancelable(false);
                gameResult.show(activity.getFragmentManager(), "results");
            }
        });

    }

    // Para a thread de execução
    public void stopGame() {

        if(cannonThread != null){

            cannonThread.setRunning(false);
        }
    }

    //Libera os recursos de áudio que foram alocados
    public void releaseResources() {

        soundPool.release();
        soundPool = null;
    }

    //Chamado quando a superfície é criada
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // se caixa de dialogo não estiver mais sendo exibida
        if(!dialogIsDiplayed){

            //Inicializa a Thread de execução do jogo
            cannonThread = new CannonThread(holder);
            cannonThread.setRunning(true);
            cannonThread.start();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // Quando a superfície é destruída, encerra a Thread da maneira apropriada
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        boolean retry = true;
        cannonThread.setRunning(false);

        while(retry){

            try{

                cannonThread.join();
                retry = false;

            }catch (InterruptedException excp){
                Log.e(TAG, "Thread interrupted", excp);
            }
        }

    }


    //Chamado quando um evendo de toque  na tela ocorre
    @Override
    public boolean onTouchEvent(MotionEvent e){

        int action = e.getAction();

        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE){

            fireCannonBall(e);

        }

        return true;

    }

    //Thread responável pelo desenho e atualização dos elementos gráficos e por processar as interações do jogo

    private class CannonThread  extends Thread{

        private SurfaceHolder surfaceHolder;
        private boolean threadIsRunning = true;

        public CannonThread(SurfaceHolder holder){

            surfaceHolder = holder;
            setName("CannonThread");
        }

        //Seta o estado de execução da Thread
        public void setRunning(boolean running){

            threadIsRunning = running;

        }

        @Override
        public void run(){

            Canvas canvas = null;

            //Recupera o instante atual do sistema em milisegundos
            long previousFrameTime = System.currentTimeMillis();

            // enquanto a Thread estiver executando
            while(threadIsRunning){

                try{


                    //Recupera uma instância do objetos Canvas da superfície a partir do seu SurfaceHolder
                    canvas = surfaceHolder.lockCanvas();

                    //Sincronica o acesso ao SurfaceHolder para poder desenhar na superfície de maneira segura.
                    // A sincrozinação é importante por causa dos inúmeros acessos ao objeto para desenhar frame a
                    //frame no SurfaceView
                    synchronized (surfaceHolder){
                        long currentTime = System.currentTimeMillis(); // Instante atual
                        double elapsedTimeMS = currentTime - previousFrameTime; // Tempo decorrido do último frame até o atual
                        totalElapsedTime += elapsedTimeMS / 1000.0; //Tempo total transcorrido em segundos
                        updatePositions(elapsedTimeMS); // atualiza a posição dos elementos.
                        drawGameElements(canvas); // desenha os elementos na posicão atual
                        previousFrameTime = currentTime;
                    }
                }finally{

                    //Se canvas não é recuperado pelo lockCanvas(), força-se a obtenção  por este método.
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

        }


    }
}
