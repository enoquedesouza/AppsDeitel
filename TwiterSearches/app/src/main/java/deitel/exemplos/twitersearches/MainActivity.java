package deitel.exemplos.twitersearches;


import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private static final String SEARCHES = "searches";

    private EditText queryEditText;
    private EditText tagEditText;
    private SharedPreferences preferences;
    private ArrayList<String> tags;
    private ArrayAdapter<String> adapter;
    private ListView lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        queryEditText = (EditText) findViewById(R.id.queryEditText); // Refrência ao queryEditText
        tagEditText = (EditText) findViewById(R.id.tagEditText); // Refrência ao tagEditText
        preferences = getSharedPreferences(SEARCHES, MODE_PRIVATE); // Recupera uma referência para o arquivo "searches"
                                                                    // no qual os pares tag, query são salvos.
        tags = new ArrayList<String>(preferences.getAll().keySet()); // Recupera uma lista de chaves (tags) de preferences

        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER); // Ordena tags

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, tags); // Configura uma adaptador para o ListView da aplicação

        ImageButton saveButton = (ImageButton)findViewById(R.id.imageButton);

        saveButton.setOnClickListener(saveButtonListener);

        lista = (ListView) findViewById(R.id.lista); // Referência ao ListView da aplicação
        lista.setAdapter(adapter); // seta o adaptador da lista 

        lista.setOnItemClickListener(itemClickListener);
        lista.setOnItemLongClickListener(itemLongClickListener);

        TextView texto = (TextView)findViewById(R.id.textView);
        texto.setText("Buscas Preferidas");

        queryEditText.setText("");
        tagEditText.setText("");


    }


    // Classe interna anônima que trata o evento gerado quando o usuário clica no botão salvar.
    public OnClickListener saveButtonListener = new OnClickListener(){

        @Override
        public void onClick(View v) {

                if(queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0){

                        addTaggedSearch(tagEditText.getText().toString(),
                                        queryEditText.getText().toString());

                        queryEditText.setText("");
                        tagEditText.setText("");

                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                            .hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);

                }else{

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setMessage(R.string.missingMessage);

                    builder.setPositiveButton(R.string.OK, null);

                    AlertDialog errorDialog = builder.create();
                    errorDialog.show();
                }

        }
    };

    //trata o evento de clique em um item da lista de pesquisas carregando a página web com a pesquisa desejada
    //no site do Twiter.
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String tag = ((TextView) view).getText().toString();
                String url = getString(R.string.searchUrl) +
                        Uri.encode(preferences.getString(tag, ""), "UTF-8");

                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); // Abre o navegador com o endereço passado
                                                                                    //como parâmetro
                startActivity(webIntent);
        }
    };

    //Trata o evento de click long em algum item da lista de pesquisa exibindo uma caixa de diálogo onde
    //o usuário opta por compartilhar, editar ou excluir uma pesquisa.
    OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            final String tag = ((TextView)view).getText().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(getString(R.string.shareEditDelete, tag));

            builder.setItems(R.array.dialog_items, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch(which){

                        case 0:
                            shareSearch(tag);
                            break;

                        case 1:
                            tagEditText.setText(tag);
                            queryEditText.setText(preferences.getString(tag, ""));
                            break;

                        case 2:
                            deleteSearch(tag);
                            break;

                    }

                }
            });

            builder.setNegativeButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            builder.create().show();

            return true;
        }
    };

    /*
    * Mpetodo que salva as informações digitadas pelo usuário.
    * @param tag, identificador digitado pelo usuário.
    * @param query interesse de pesquisa digitado pelo usuário
    */
    private void addTaggedSearch(String tag, String query){

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(tag, query); //Salva o par tag, query no arquivo searches referenciado por preferences.
        editor.apply();

        if(!tags.contains(tag)){

            tags.add(tag); // adiciona a tag ao arraylist
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER); // Ordena as buscas por ordem alfabético desconsiderando
                                                                   // maiúsculas ou minúsculas.
            adapter.notifyDataSetChanged(); //atualiza o listview da aplicação.

        }
    }

    /*
    * Método que permite compartilhar seu interesse de pesquisa no Twiter
    * @param tag é o identificador da busca que usuário deseja compartilhar.
    */
    private void shareSearch(String tag){

        String url = getString(R.string.searchUrl) +
                Uri.encode(preferences.getString(tag, ""), "UTF-8"); //Configura a url do acesso a pesquisa de interesse do usuário.


        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage, url));
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareSearch)));// Incia a atividade para a escolha do aplicativo de
                                                                             // pelo qual o compartilhamento será feito.

    }

    /*
    * Deleta uma pesquisa de interesse do suário
    * @param tag identificador da pesquisa que será excluída
    */
    private void deleteSearch(final String tag){

        AlertDialog.Builder confirmBuilder  = new AlertDialog.Builder(this);

        confirmBuilder.setMessage(getString(R.string.confirmMessage, tag)); //Configura uma mensagem de confirmação
                                                                                                // que será exibe ao usuário.
        confirmBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel(); //o susuário desiste de excluir a pesquisa
                    }
                }
        );

        //O susuário confirma a exclusão da pesquisa.
        confirmBuilder.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        tags.remove(tag);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove(tag);
                        editor.apply();

                        adapter.notifyDataSetChanged();

                    }
                }

        );

        confirmBuilder.create().show();
    }
}
