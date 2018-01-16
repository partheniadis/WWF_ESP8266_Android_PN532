package udea.edu.co.blink_wemos;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    Button encender, apagar, connect;
    EditText ipServer;
    TextView estado;
    boolean socketStatus = false;
    Socket socket;
    MyClientTask myClientTask;
    String address;
    int port = 80;
    TextView cardTextView;
    boolean isCardDetected=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

//Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
//        encender = (Button)findViewById(R.id.encender);
//        encender.setEnabled(false);
//        apagar = (Button)findViewById(R.id.apagar);
//        apagar.setEnabled(false);
        connect = (Button)findViewById(R.id.connect);
        ipServer = (EditText)findViewById(R.id.ip_server);
        estado = (TextView)findViewById(R.id.estado);
        cardTextView = (TextView) findViewById(R.id.cardDetection);

        connect.setOnClickListener(connectOnClickListener);
//        encender.setOnClickListener(OnOffLedClickListener);
//        apagar.setOnClickListener(OnOffLedClickListener);
    }


    OnClickListener connectOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if(socketStatus)
                Toast.makeText(MainActivity.this,"Already talking to a Socket!! Disconnect and try again!", Toast.LENGTH_LONG).show();
            else {
                socket = null;
                address = ipServer.getText().toString();
                if (address == null)
                    Toast.makeText(MainActivity.this, "Please enter valid address", Toast.LENGTH_LONG).show();
                else {
                    myClientTask = new MyClientTask(address);
                    ipServer.setEnabled(false);
                    connect.setEnabled(false);
                    myClientTask.execute("card"); //TODO: put it under a button click when its time
//                    encender.setEnabled(true);
                    estado.setText("IP guardada");
                }
            }
        }
    };

    public class MyClientTask extends AsyncTask<String,Void,String>{

        String server;

        MyClientTask(String server){
            this.server = server;
        }

        @Override
        protected String doInBackground(String... params) {

            StringBuffer chaine = new StringBuffer("");

            final String val = params[0];
            final String p = "http://"+ server+"/"+val;

            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    estado.setText(p);
//                    if(isCardDetected) cardTextView.setText("Card Detected!");
//                    else cardTextView.setText("Waiting for card...");
                }
            });

            //link refresh end

            String serverResponse = "";
            try {
                URL url = new URL(p);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream inputStream = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream)); //THIS LINE RETURNS MESSAGE FROM SERVER
                String line = "";
                while ((line = rd.readLine()) != null) {
                    chaine.append(line);
                }
                inputStream.close();

                String message = "chaine: " + chaine.toString();

                if(message.equals("chaine: I detected a card!")){
                    cardDetected(true);
                }else if(message.equals("chaine: Looking for cards...")){
                    cardDetected(false);
                }

                System.out.println(message);
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(String s) {

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //requestHttp();
                    MyClientTask taskEsp = new MyClientTask(address);
                    taskEsp.execute("card");
                }
            }, 200);

        }
    }

    private void cardDetected(final boolean b) {
        isCardDetected = b;

        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                if(b){
                    cardTextView.setText("Card Detected!");
                }
                else{
                    cardTextView.setText("Waiting for card...");
                }
            }
        });
        //save to firebase as 1
        //when the other app reads that it is 1 it changes it to 0

    }


}
