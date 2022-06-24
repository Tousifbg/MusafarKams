package com.app.kams_mosafir;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.onurkagan.ksnack_lib.Animations.Fade;
import com.onurkagan.ksnack_lib.MinimalKSnack.MinimalKSnack;
import com.onurkagan.ksnack_lib.MinimalKSnack.MinimalKSnackStyle;

public class ShowNow {
    MinimalKSnack minimalKSnack;
    Context context;
    private KProgressHUD hud;

    public ShowNow(Context context) {
        this.context = context;
        minimalKSnack=new MinimalKSnack((Activity) context);
    }
    public void displayErrorToast(String message){
        minimalKSnack
                .setMessage(message)
                .setStyle(MinimalKSnackStyle.STYLE_ERROR)
                .setBackgroundColor(R.color.error_color)
                .setAnimation(Fade.In.getAnimation(), Fade.Out.getAnimation())
                .setDuration(4000)
                .alignBottom()
                .show();

    }
    public void displayPositiveToast(String message){
        minimalKSnack
                .setMessage(message)
                .setStyle(MinimalKSnackStyle.STYLE_ERROR)
                .setBackgroundColor(R.color.green)
                .setAnimation(Fade.In.getAnimation(), Fade.Out.getAnimation())
                .setDuration(4000)
                .alignBottom()
                .show();

    }

    public void showLoadingDialog(final Context context){
        hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(dialogInterface -> Toast.makeText(context, "You " +
                        "cancelled manually!", Toast
                        .LENGTH_SHORT).show());

        hud.show();

    }

    public void scheduleDismiss() {
        hud.dismiss();

    }
}
