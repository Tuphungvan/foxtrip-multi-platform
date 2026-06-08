package vn.androidhaui.foxtrip.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ActivityMainBinding;
import vn.androidhaui.foxtrip.features.user.account.AccountFragment;
import vn.androidhaui.foxtrip.features.user.cart.CartFragment;
import vn.androidhaui.foxtrip.features.user.chatbot.ChatbotFragment;
import vn.androidhaui.foxtrip.features.user.home.HomeFragment;
import vn.androidhaui.foxtrip.features.user.auth.AuthViewModel;
import vn.androidhaui.foxtrip.features.user.auth.ForgotPasswordFragment;
import vn.androidhaui.foxtrip.features.user.auth.LoginFragment;
import vn.androidhaui.foxtrip.features.user.auth.RegisterFragment;
import vn.androidhaui.foxtrip.features.user.auth.VerifyEmailFragment;
import vn.androidhaui.foxtrip.features.user.home.HomeViewModel;
import vn.androidhaui.foxtrip.features.user.order.OrdersFragment;
import vn.androidhaui.foxtrip.features.user.video.VideoFragment;
import vn.androidhaui.foxtrip.models.domain.User;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private boolean isAuthMode = true;
    private SharedPreferences authPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(ContextCompat.getColor(this, R.color.background_light));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getUser().observe(this, this::handleUserSession);

        authViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                authViewModel.clearMessage();
            }
        });

        authViewModel.getLoading().observe(this, isLoading -> {
        });

        if (savedInstanceState == null) {
            authViewModel.loadSession();
        }

        authPrefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        authListener = (prefs, key) -> {
            if ("accessToken".equals(key)) {
                String token = prefs.getString("accessToken", null);
                if (token == null) {
                    runOnUiThread(() -> authViewModel.loadSession());
                }
            }
        };
        authPrefs.registerOnSharedPreferenceChangeListener(authListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void handleUserSession(User currentUser) {
        if (currentUser != null && currentUser.isGuide()) {
            Intent intent = new Intent(this, GuideActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setMainMode();
        loadMainFragments();

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            loadFragment(new HomeFragment(), true);
        }
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void setAuthMode() {
        isAuthMode = true;
        binding.bottomNavigation.setVisibility(View.GONE);
    }

    private void setMainMode() {
        isAuthMode = false;
        binding.bottomNavigation.setVisibility(View.VISIBLE);
    }

    private void loadMainFragments() {
        binding.bottomNavigation.setVisibility(View.VISIBLE);
        HomeViewModel homeVm = new ViewModelProvider(this)
                .get(HomeViewModel.class);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
                homeVm.clearSearch();
                homeVm.loadData();
            } else if (id == R.id.nav_cart) {
                selected = new CartFragment();
            } else if (id == R.id.nav_video) {
                selected = new VideoFragment();
            } else if (id == R.id.nav_chatbot) {
                selected = new ChatbotFragment();
            } else if (id == R.id.nav_account) {
                selected = new AccountFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });

        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                homeVm.clearSearch();
                loadFragment(new HomeFragment(), true, false);
                homeVm.loadData();
            }
        });
    }

    public void loadFragment(Fragment fragment, boolean showBottomNav, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        if (!showBottomNav) {
            setAuthMode();
        } else {
            setMainMode();
        }
    }

    public void loadFragment(Fragment fragment, boolean showBottomNav) {
        loadFragment(fragment, showBottomNav, true);
    }

    @Override
    public void onBackPressed() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (current instanceof LoginFragment || current instanceof RegisterFragment ||
                current instanceof ForgotPasswordFragment || current instanceof VerifyEmailFragment) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            } else {
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                setMainMode();
            }
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        if (binding.bottomNavigation.getSelectedItemId() != R.id.nav_home) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
            return;
        }
        finish();
    }

    public void updateCartBadge(int count) {
        if (binding == null)
            return;

        binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart).setVisible(count > 0);
        binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart).setNumber(count);
    }

    public void setBottomNavigationVisibility(int visibility) {
        if (binding != null && binding.bottomNavigation != null) {
            binding.bottomNavigation.setVisibility(visibility);
        }
    }

    public void onLoginSuccess() {
        getSupportFragmentManager().popBackStackImmediate(null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        handleUserSession(authViewModel.getUser().getValue());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (v.getId() == R.id.search_input || v.getId() == R.id.etSearch) {
                        ((EditText) v).setText("");
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void navigateToOrders() {
        // 1. Xóa toàn bộ backstack
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        // 2. Đặt OrdersFragment trực tiếp không qua listener
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OrdersFragment())
                .commitAllowingStateLoss();
        // 3. Cập nhật UI navigation bar thủ công, không trigger listener
        binding.bottomNavigation.getMenu().findItem(R.id.nav_account).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authPrefs != null && authListener != null) {
            authPrefs.unregisterOnSharedPreferenceChangeListener(authListener);
        }
    }
}
