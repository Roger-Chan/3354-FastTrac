package utdallas.students.fasttrac;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class LoginActivity extends AppCompatActivity {
    DatabaseHelper db;
    Button bLoginbtn = null;
    EditText user_name = null;
    EditText pass_wrd = null;
    TextView invalid = null;
    TextView tvRegister = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = DatabaseHelper.getInstance(this);


        Button bLoginbtn = (Button) findViewById(R.id.bLogin);
        user_name = (EditText) findViewById(R.id.etUsername);
        pass_wrd = (EditText) findViewById(R.id.etPassword);
        invalid = (TextView) findViewById(R.id.invalid_login);
        tvRegister = (TextView) findViewById(R.id.tvRegister);

        bLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the strings from the field
                String un = user_name.getText().toString();
                String pwd = pass_wrd.getText().toString();
                User user = db.validCredentials(un, pwd); // initially error code
                Intent secondActivity;

                //shows error message if bad credentials
                if (user == null) {
                    Toast.makeText(getApplicationContext(), "Wrong Username and/or Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                //student is 0, professor =1
                if (user.authorization == 0) {
                    secondActivity = new Intent(getApplicationContext(), StudentPage.class);
                    secondActivity.putExtra("Student", user);
                    startActivity(secondActivity);
                } else if (user.authorization == 1) {
                    secondActivity = new Intent(getApplicationContext(), ProfessorPage.class);
                    secondActivity.putExtra("Professor", user);
                    startActivity(secondActivity);
                } else {
                    invalid.setVisibility(View.VISIBLE);
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);

                LoginActivity.this.startActivity(registerIntent);

            }
        });
    }
}