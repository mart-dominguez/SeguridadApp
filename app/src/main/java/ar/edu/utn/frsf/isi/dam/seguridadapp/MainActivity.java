package ar.edu.utn.frsf.isi.dam.seguridadapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "APP_PERMISOS";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_RECORD_LOCATION_PERMISSION = 300;
    private static String mFileName = null;

    private FusedLocationProviderClient mFusedLocationClient;

    private MediaRecorder mRecorder = null;

    private MediaPlayer mPlayer = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permisoParaGrabarConcedido = false;
    private boolean permisoParaSaberDondeEstasConcedido = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private static int ESTADO_ACTUAL = 2;
    private static final int ESTADO_MEDIA_ACTIVO = 1;
    private static final int ESTADO_MEDIA_INACTIVO = 2;

    private Button btnGrabar;
    private Button btnReproducir;
    private Button btnDondeEstoy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
        btnGrabar = (Button) findViewById(R.id.btnGrabar);
        btnGrabar.setOnClickListener(eventoBtnGrabar);

        btnReproducir = (Button) findViewById(R.id.btnReproducir);
        btnReproducir.setOnClickListener(eventoBtnReproducir);

        btnDondeEstoy = (Button) findViewById(R.id.btnDondeEstoy);
        btnDondeEstoy.setOnClickListener(eventoBtnUbicacion);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.e(LOG_TAG, "Create finaliza");

    }

    private View.OnClickListener eventoBtnUbicacion = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.e(LOG_TAG, "Tiene Permiso ?"+tienePermisoUbicacion());

            if (tienePermisoUbicacion()) procesarUbicacion();
            else
                pedirPermiso(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, "Con este boton le mandas tu ubicacion al que te lo solicite! Ubicate!", REQUEST_RECORD_LOCATION_PERMISSION);
        }
    };


    private View.OnClickListener eventoBtnGrabar = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (tienePermiso()) procesarEventoBtnGrabar();
            else
                pedirPermiso(new String[]{Manifest.permission.RECORD_AUDIO}, "No seas ortiba! Dame los permisos o se arma bondi. ", REQUEST_RECORD_AUDIO_PERMISSION);
        }
    };


    private View.OnClickListener eventoBtnReproducir = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ESTADO_ACTUAL == ESTADO_MEDIA_INACTIVO) {
                btnGrabar.setEnabled(false);
                btnReproducir.setText("Detener");
                ESTADO_ACTUAL = ESTADO_MEDIA_ACTIVO;
                startPlaying();
            } else {
                btnGrabar.setEnabled(true);
                btnReproducir.setText("Reproducir");
                stopPlaying();
                ESTADO_ACTUAL = ESTADO_MEDIA_ACTIVO;
            }
        }
    };

    private boolean tienePermiso() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private boolean tienePermisoUbicacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void pedirPermiso(final String[] permisosPedidos, String mensajeRacional, final int codigo) {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                permisosPedidos[0])) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("NECESITO LOS PERMISOS !!!!")
                    .setMessage(mensajeRacional)
                    .setPositiveButton("Metale!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    permisosPedidos,
                                    codigo);
                        }
                    })
                    .setNegativeButton("Volá de acá", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(MainActivity.this, " Vos te lo perdes ", Toast.LENGTH_LONG).show();
                        }
                    });
            builder.create().show();

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(MainActivity.this,
                    permisosPedidos,
                    codigo);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    private void procesarUbicacion() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.e(LOG_TAG, "OBTENGO LOCATION: "+location);
                        String latLng="";
                        if (location != null) {
                            latLng = "geo:" + location.getLatitude() + "," + location.getLongitude();
                        }else{
                            latLng = "geo:36.1223222,-115.1672533?z=9";
                        }
                        Toast.makeText(MainActivity.this, "Estas en " + latLng, Toast.LENGTH_LONG).show();
                        Uri gmmIntentUri = Uri.parse(latLng);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    }
                });
    }

    private void procesarEventoBtnGrabar(){
        if(ESTADO_ACTUAL== ESTADO_MEDIA_ACTIVO){
            stopRecording();
            btnGrabar.setText("Grabar");
            btnReproducir.setEnabled(true);
            ESTADO_ACTUAL= ESTADO_MEDIA_INACTIVO;
        }else{
            btnReproducir.setEnabled(false);
            btnGrabar.setText("Detener");
            ESTADO_ACTUAL= ESTADO_MEDIA_ACTIVO;
            startRecording();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permisoParaGrabarConcedido = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                Log.e(LOG_TAG, "Tengo MICROFONO ?"+permisoParaGrabarConcedido);
                procesarEventoBtnGrabar();
                break;
            case REQUEST_RECORD_LOCATION_PERMISSION:
                permisoParaSaberDondeEstasConcedido = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                Log.e(LOG_TAG, "Tengo UBICACION ?"+permisoParaSaberDondeEstasConcedido);
                procesarUbicacion();
                break;

        }
        if (!permisoParaGrabarConcedido) Toast.makeText(MainActivity.this," Asi no se puede! para que me instalaste!",Toast.LENGTH_LONG).show();
        if (!permisoParaSaberDondeEstasConcedido ) Toast.makeText(MainActivity.this," Nadie sabe donde estas!!!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


}
