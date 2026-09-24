package com.tangorra.matias.savi.ui.comun;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tangorra.matias.savi.R;

/** Muestra un codigo QR para que otro usuario lo escanee (familia o grupo vecinal). */
public class QrDialogo extends BottomSheetDialogFragment {

    private static final String ARG_TITULO = "titulo";
    private static final String ARG_DETALLE = "detalle";
    private static final String ARG_CONTENIDO = "contenido";

    public static void mostrar(FragmentManager fm, String titulo, String detalle, String contenido) {
        QrDialogo dialogo = new QrDialogo();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_DETALLE, detalle);
        args.putString(ARG_CONTENIDO, contenido);
        dialogo.setArguments(args);
        dialogo.show(fm, "qr");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.dialogo_qr, container, false);
        Bundle args = requireArguments();
        ((TextView) vista.findViewById(R.id.titulo)).setText(args.getString(ARG_TITULO));
        ((TextView) vista.findViewById(R.id.detalle)).setText(args.getString(ARG_DETALLE));
        try {
            int lado = getResources().getDimensionPixelSize(R.dimen.lado_qr);
            Bitmap qr = new BarcodeEncoder().encodeBitmap(args.getString(ARG_CONTENIDO), BarcodeFormat.QR_CODE, lado, lado);
            ((ImageView) vista.findViewById(R.id.qr)).setImageBitmap(qr);
        } catch (WriterException e) {
            dismiss();
        }
        return vista;
    }
}
