package fafa.com.visitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 首页信息
 *
 * @author Silence
 */
public class HomePage {

    private View external;
    private View staff;
    private View checkout;

    private Activity activity;

    public HomePage(View external, View staff, View checkout, Activity activity) {
        this.external = external;
        this.staff = staff;
        this.checkout = checkout;
        this.activity = activity;
    }

    public void init() {
        this.initEvent();
    }

    private void initEvent() {
        external.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ExternalActivity.class);
                activity.startActivity(intent);
            }
        });
        staff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, StaffActivity.class);
                activity.startActivity(intent);
            }
        });
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, CheckoutActivity.class);
                activity.startActivity(intent);
            }
        });
    }

}
