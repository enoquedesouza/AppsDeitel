package deitel.exemplos.cannongame;

import android.app.Fragment;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Enoque on 22/01/2019.
 *
 * Fragmento criado para o jogo. O layout do fragmento é inflado como um SurfaceView, nesta superfícei serão
 * desenhado os elementos gráficos do jogo.
 * Na classe também são sobreescritos  os método do ciclo de vida do fragmento, como este fragmento será anexado
 * à atividade principal o cliclo de vida do fragmento segue o ciclo de vida da atividade.
 */

public class CannonGameFragment extends Fragment {

    private CannonView cannonView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedIntanceBundle){

        super.onCreateView(inflater, container, savedIntanceBundle);

        View view = inflater.inflate(R.layout.fragment_game,container, false);// Infla o layout do fragemento, neste caso um view
                                                                               // do tipo SurfaceView

        cannonView = (CannonView) view.findViewById(R.id.cannon_view); // Recupera uma refrência de SurfaceView

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceBundle){

        super.onActivityCreated(savedInstanceBundle);

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC); // Estabelece que o controle do volume do áudio
                                                                         // do game será feito pelos controles laterais do telefone

    }


    @Override
    public void onPause(){

        super.onPause();
        cannonView.stopGame(); // comando para parar o jogo quando a ativida sair de foco
    }


    @Override
    public void onDestroy(){

        super.onDestroy();

        cannonView.releaseResources(); //libera os recursos utilizados no game, como os arquivos de audio.
    }


}
