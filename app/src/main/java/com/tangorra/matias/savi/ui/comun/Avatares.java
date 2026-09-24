package com.tangorra.matias.savi.ui.comun;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.data.UsuarioRepositorio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Fotos de perfil: carga desde Storage y preparacion para subir. */
public final class Avatares {

    // Suficiente para un avatar y muy por debajo del limite de 5 MB de storage.rules
    private static final int LADO_MAXIMO = 720;
    private static final int CALIDAD_JPEG = 85;

    private Avatares() {
    }

    public static void cargar(final ImageView destino, String uid) {
        destino.setImageResource(R.drawable.icon_casa);
        if (uid == null) {
            return;
        }
        UsuarioRepositorio.urlFoto(uid).addOnSuccessListener(uri -> {
            if (destino.getContext() instanceof Activity && ((Activity) destino.getContext()).isDestroyed()) {
                return;
            }
            Glide.with(destino).load(uri).placeholder(R.drawable.icon_casa).centerCrop().into(destino);
        });
    }

    /** Lee la imagen elegida, la reduce y la comprime a JPEG. */
    public static byte[] prepararParaSubir(ContentResolver resolver, Uri imagen) throws IOException {
        Bitmap original;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            original = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, imagen), (decoder, info, source) -> {
                int lado = Math.max(info.getSize().getWidth(), info.getSize().getHeight());
                if (lado > LADO_MAXIMO) {
                    float escala = (float) LADO_MAXIMO / lado;
                    decoder.setTargetSize(Math.round(info.getSize().getWidth() * escala),
                            Math.round(info.getSize().getHeight() * escala));
                }
            });
        } else {
            try (InputStream entrada = resolver.openInputStream(imagen)) {
                original = BitmapFactory.decodeStream(entrada);
            }
            int lado = Math.max(original.getWidth(), original.getHeight());
            if (lado > LADO_MAXIMO) {
                float escala = (float) LADO_MAXIMO / lado;
                original = Bitmap.createScaledBitmap(original, Math.round(original.getWidth() * escala),
                        Math.round(original.getHeight() * escala), true);
            }
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        original.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida);
        return salida.toByteArray();
    }
}
