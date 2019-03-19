package com.tangorra.matias.savi.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.StringUtils;

public class PopUpViewQRGrupo extends AppCompatActivity {

    private Context popAlarmas;
    private ImageView codigoQR;
    private TextView nombreGrupo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_up_codigo_grupo);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int)(width*.9),(int)(heigth*.7) );

        getSupportActionBar().hide();

        popAlarmas=this;

        nombreGrupo = findViewById(R.id.textViewNombreGrupo);
        nombreGrupo.setText(StringUtils.getTextoFormateado(SesionManager.getGrupo().getNombre().toString()));

        codigoQR = findViewById(R.id.grupo_codigo_qr);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(SesionManager.getGrupo().getId(), BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            codigoQR.setImageBitmap(bitmap);
        } catch (WriterException e){
            e.printStackTrace();
        }
    }

}
