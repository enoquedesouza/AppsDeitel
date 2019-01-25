package deitel.exemplos.cannongame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// A atividade principal hospedará o fragmento que terá anexado um SurfaceView onde serão desenhados
// os elementos gráficos, frame a frame, do jogo.
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
