package vn.androidhaui.foxtrip.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ActivityGuideBinding;
import vn.androidhaui.foxtrip.features.guide.dashboard.GuideHomeFragment;
import vn.androidhaui.foxtrip.features.user.auth.AuthViewModel;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.lifecycle.ViewModelProvider;

public class GuideActivity extends AppCompatActivity {

    private ActivityGuideBinding binding;
    private AuthViewModel authViewModel;
    private SharedPreferences authPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Full screen / Transparent status bar like User App
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        binding = ActivityGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Load GuideHomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(new GuideHomeFragment());
        }

        // Lắng nghe thay đổi token (Session expired)
        authPrefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        authListener = (prefs, key) -> {
            if ("accessToken".equals(key)) {
                String token = prefs.getString("accessToken", null);
                if (token == null) {
                    // Token bị xoá -> Đã logout hoặc session expired -> Về MainActivity
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        };
        authPrefs.registerOnSharedPreferenceChangeListener(authListener);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.guide_fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authPrefs != null && authListener != null) {
            authPrefs.unregisterOnSharedPreferenceChangeListener(authListener);
        }
    }
}
