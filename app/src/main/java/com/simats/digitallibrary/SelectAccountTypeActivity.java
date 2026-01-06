package com.simats.digitallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Account Type Selection Screen
 * Shows Reader and Admin options - leads to Login or Register based on flow
 */
public class SelectAccountTypeActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT_TYPE = "account_type";
    public static final String EXTRA_FLOW_TYPE = "flow_type";
    public static final String TYPE_READER = "reader";
    public static final String TYPE_ADMIN = "admin";
    public static final String FLOW_LOGIN = "login";
    public static final String FLOW_SIGNUP = "signup";

    private String flowType = FLOW_LOGIN; // Default to login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_account_type);

        // Check if this is for signup or login
        flowType = getIntent().getStringExtra(EXTRA_FLOW_TYPE);
        if (flowType == null) {
            flowType = FLOW_LOGIN; // Default to login
        }

        // Update title based on flow
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            if (FLOW_SIGNUP.equals(flowType)) {
                tvTitle.setText("Create Account");
            } else {
                tvTitle.setText("Select Account Type");
            }
        }

        // Reader Account click
        LinearLayout layoutReader = findViewById(R.id.layoutReaderAccount);
        layoutReader.setOnClickListener(v -> {
            Intent intent;
            if (FLOW_SIGNUP.equals(flowType)) {
                intent = new Intent(this, RegisterActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            intent.putExtra(EXTRA_ACCOUNT_TYPE, TYPE_READER);
            startActivity(intent);
        });

        // Admin Account click
        LinearLayout layoutAdmin = findViewById(R.id.layoutAdminAccount);
        layoutAdmin.setOnClickListener(v -> {
            Intent intent;
            if (FLOW_SIGNUP.equals(flowType)) {
                intent = new Intent(this, RegisterActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            intent.putExtra(EXTRA_ACCOUNT_TYPE, TYPE_ADMIN);
            startActivity(intent);
        });
    }
}
