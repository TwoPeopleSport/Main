package com.example.my_future;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.my_future.MenuFlowing.CalculatedFragment;
import com.example.my_future.MenuFlowing.MenuListFragment;
import com.example.my_future.MenuFlowing.NavItemSelectedListener;
import com.example.my_future.MenuBottom.FoodFragment;
import com.example.my_future.MenuBottom.ForumFragment;
import com.example.my_future.MenuBottom.NotebookFragment;
import com.example.my_future.MenuBottom.PlanFragment;
import com.example.my_future.MenuBottom.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.my_future.Variables.APP_PREFERENCES;
import static com.example.my_future.Variables.APP_PREFERENCES_BOOLEAN_ACTIVITY;
import static com.example.my_future.Variables.APP_PREFERENCES_BOOLEAN_HEALTH;
import static com.example.my_future.Variables.APP_PREFERENCES_BOOLEAN_PROFILE;

public class MainActivity extends AppCompatActivity implements NavItemSelectedListener {
    FirebaseDatabase db;
    DatabaseReference myRef;
    FirebaseAuth mAuth;

    BottomNavigationView bottomNav;
    SharedPreferences mSettings;

    private final List<Fragment> fragmentsInStack = new ArrayList<>();
    private final FoodFragment fragmentFood = new FoodFragment();
    private final ForumFragment fragmentForum = new ForumFragment();
    private final NotebookFragment fragmentNotebook = new NotebookFragment();
    private final PlanFragment fragmentPlan = new PlanFragment();
    private final ProfileFragment fragmentProfile = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        clickBottomNavigationMenu();
        setupMenu();
    }

    private void init() {
        bottomNav = findViewById(R.id.bottom_navigation);
        fragmentsInStack.add(fragmentPlan);
        changeFragment(fragmentPlan);

        db = FirebaseDatabase.getInstance();
        myRef = db.getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        mSettings = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        checkProfile();
    }

    private void checkProfile() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SharedPreferences.Editor editor = mSettings.edit();
                if (!String.valueOf(mSettings.contains(APP_PREFERENCES_BOOLEAN_PROFILE)).equals("true")) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(mAuth.getUid())) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                if (s.getKey().equals("profile")) {
                                    if (s.getValue().equals("none")) {
                                        startActivity(new Intent(MainActivity.this, FillingDataUserActivity.class));
                                        finish();
                                    } else {
                                        editor.putString(APP_PREFERENCES_BOOLEAN_PROFILE, "true");
                                        editor.apply();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (String.valueOf(mSettings.contains(APP_PREFERENCES_BOOLEAN_PROFILE)).equals("true")) {
                    if (!String.valueOf(mSettings.contains(APP_PREFERENCES_BOOLEAN_HEALTH)).equals("true")) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.getKey().equals(mAuth.getUid())) {
                                for (DataSnapshot s : snapshot.getChildren()) {
                                    if (s.getKey().equals("health")) {
                                        if (s.getValue().equals("none")) {
                                            startActivity(new Intent(MainActivity.this, FillingDataUserHealthActivity.class));
                                            finish();
                                        } else {
                                            editor.putString(APP_PREFERENCES_BOOLEAN_HEALTH, "true");
                                            editor.apply();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (String.valueOf(mSettings.contains(APP_PREFERENCES_BOOLEAN_HEALTH)).equals("true")) {
                    if (!String.valueOf(mSettings.contains(APP_PREFERENCES_BOOLEAN_ACTIVITY)).equals("true")) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.getKey().equals(mAuth.getUid())) {
                                for (DataSnapshot s : snapshot.getChildren()) {
                                    if (s.getKey().equals("health")) {
                                        for (DataSnapshot health : s.getChildren()) {
                                            if (health.getKey().equals("activity")) {
                                                if (health.getValue().equals("none")) {
                                                    startActivity(new Intent(MainActivity.this, FillingDataUserHealthActivity.class).putExtra("openActivity", "false"));
                                                    finish();
                                                } else {
                                                    editor.putString(APP_PREFERENCES_BOOLEAN_ACTIVITY, "true");
                                                    editor.apply();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MyToast(databaseError.getMessage());
            }
        });
    }

    private void setupMenu() {
        FragmentManager fm = getSupportFragmentManager();
        MenuListFragment mMenuFragment = (MenuListFragment) fm.findFragmentById(R.id.id_container_menu);
        if (mMenuFragment == null) {
            mMenuFragment = new MenuListFragment();
            mMenuFragment.setNavItemSelectedListener(this);
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment).commit();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onNavItemSelectedListener(MenuItem item) {
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.id_calculate:
                selectedFragment = new CalculatedFragment();
                break;
            case R.id.id_out:
                mAuth.signOut();
                selectedFragment = new Fragment();
                startActivity(new Intent(MainActivity.this, FirstScreenActivity.class));
                finish();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
    }

    private void clickBottomNavigationMenu() {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            Fragment targetFragment = itemId == R.id.fragmentPlan ? fragmentPlan
                    : itemId == R.id.fragmentFood ? fragmentFood
                    : itemId == R.id.fragmentForum ? fragmentForum
                    : itemId == R.id.fragmentNotebook ? fragmentNotebook
                    : fragmentProfile;

            if (itemId != R.id.fragmentPlan) {
                fragmentsInStack.remove(targetFragment);
                fragmentsInStack.add(targetFragment);
            }

            changeFragment(targetFragment);
            return true;
        });
    }

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.fragment_container, fragment).
                commit();
    }

    @Override
    public void onBackPressed() {
        if (!fragmentsInStack.isEmpty()) {
            fragmentsInStack.remove(fragmentsInStack.size() - 1);
            int lastId = fragmentsInStack.size() - 1;

            if (lastId != -1) {
                changeFragment(fragmentsInStack.get(lastId));
                bottomNav.setSelectedItemId(
                        fragmentsInStack.get(lastId) == fragmentPlan ? R.id.fragmentPlan
                                : fragmentsInStack.get(lastId) == fragmentFood ? R.id.fragmentFood
                                : fragmentsInStack.get(lastId) == fragmentForum ? R.id.fragmentForum
                                : fragmentsInStack.get(lastId) == fragmentNotebook ? R.id.fragmentNotebook
                                : R.id.fragmentProfile

                );
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void OnClickExit(View view) {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, FirstScreenActivity.class));
        finish();
    }

    private void MyToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}