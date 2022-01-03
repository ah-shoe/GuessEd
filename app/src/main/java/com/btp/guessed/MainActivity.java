package com.btp.guessed;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.btp.guessed.Adapter.CategoryAdapter;
import com.btp.guessed.Common.Common;
import com.btp.guessed.Common.SpaceDecoration;
import com.btp.guessed.DBHelper.DBHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.opencv.android.OpenCVLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;
import io.paperdb.Paper;

//import com.afollestad.materialdialogs.MaterialDialog;
//import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;


public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recycler_category;

    private static final int MY_REQUEST_CODE = 7117;
    List<AuthUI.IdpConfig> providers;
    //Button btn_sign_out;
    static{
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity","OpenCV is loaded");
        }
        else {
            Log.d("MainActivity","OpenCV is not loaded");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Paper
        Paper.init(this);

        //Get value online mode
        Common.isOnlineMode = Paper.book().read(Common.KEY_SAVE_ONLINE_MODE, false); //Default false

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("GuessEd");
        toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        setSupportActionBar(toolbar);

        recycler_category = findViewById(R.id.recycler_category);
        recycler_category.setHasFixedSize(true);
        recycler_category.setLayoutManager(new GridLayoutManager(this, 1));

        //Get Screen Height
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels / 8;//Max size of item in Category
        CategoryAdapter adapter = new CategoryAdapter(MainActivity.this, DBHelper.getInstance(this).getAllCategories());
        int spaceInPixel = 4;
        recycler_category.addItemDecoration(new SpaceDecoration(spaceInPixel));
        recycler_category.setAdapter(adapter);

//        btn_sign_out = findViewById(R.id.btn_sign_out);
//        btn_sign_out.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                // Sign out
//                AuthUI.getInstance()
//                        .signOut(MainActivity.this)
//                        .addOnCompleteListener(new OnCompleteListener<Void>()
//                        {
//                            public void onComplete(@NonNull Task<Void> task)
//                            {
//                                btn_sign_out.setEnabled(false);
//                                showSignInOptions();
//                            }
//                        }).addOnFailureListener(new OnFailureListener()
//                {
//                    @Override
//                    public void onFailure(@NonNull Exception e)
//                    {
//                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                // [END auth_fui_signout]
//            }
//        });

        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        showSignInOptions();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in, now get user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                Toast.makeText(this, "Signed in as: "+((user.getEmail()!=null)?user.getEmail():user.getPhoneNumber()), Toast.LENGTH_SHORT).show();
                // ...
//                btn_sign_out.setEnabled(true);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                assert response != null;
                Toast.makeText(this, ""+ Objects.requireNonNull(response.getError()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSignInOptions()
    {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.Mytheme)
                        .build(),MY_REQUEST_CODE
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.menu_settings)
        {
            showSettings();
        }
        if(item.getItemId() == R.id.btn_sign_out)
        {
            AuthUI.getInstance()
                    .signOut(MainActivity.this)
                    .addOnCompleteListener(task -> {
//                            btn_sign_out.setEnabled(false);
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        return true;
    }

    private void showSettings()
    {
        View setting_layout = LayoutInflater.from(this).inflate(R.layout.settings_layout, null);
        final CheckBox ckb_online_mode = setting_layout.findViewById(R.id.ckb_online_mode);

        //Load data from paper, if not available just init default false
        ckb_online_mode.setChecked(Paper.book().read(Common.KEY_SAVE_ONLINE_MODE, false));

        //Show Dialog
        /*new MaterialStyledDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.ic_settings_white_24dp)
                .setTitle("Settings")
                .setDescription("Please choose action")
                .setCustomView(setting_layout)
                .setNegativeText("DISMISS")
                .onNegative(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                    {
                        dialog.dismiss();
                    }
                })
                .setPositiveText("SAVE")
                .onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                    {
                        if(ckb_online_mode.isChecked())
                        {
                            Common.isOnlineMode = true;
                        }
                        else
                        {
                            Common.isOnlineMode = false;
                        }

                        //Save
                        Paper.book().write(Common.KEY_SAVE_ONLINE_MODE, ckb_online_mode.isChecked());
                    }
                }).show()
*/
        MaterialDialog mDialog = new MaterialDialog.Builder(MainActivity.this)
                .setTitle("Settings")
                .setMessage("Please choose action")
                .setPositiveButton("SAVE", R.drawable.fui_done_check_mark, (dialogInterface, which) -> {
                    Common.isOnlineMode = ckb_online_mode.isChecked();

                    //Save
                    Paper.book().write(Common.KEY_SAVE_ONLINE_MODE, ckb_online_mode.isChecked());
                })
                .setNegativeButton("DISMISS", R.drawable.ic_clear_white_24dp, (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        mDialog.show();
    }
}
