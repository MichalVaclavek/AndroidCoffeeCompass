package cz.fungisoft.coffeecompass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import cz.fungisoft.coffeecompass.R;

public class SelectSearchDistanceActivity extends AppCompatActivity implements  View.OnClickListener {

    private ListView selectSearchDistanceListView;

    private Button selectOKButton;
    private Button selectCancelButton;

    String[] vzdalenosti;
    private String selectedDistance;

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_search_distance);

        this.selectedDistance = getIntent().getStringExtra("searchRange");

        selectSearchDistanceListView = (ListView) findViewById(R.id.selectSearchDistListview);
        selectOKButton = (Button) findViewById(R.id.selectDistanceButtonOK);
        selectCancelButton = (Button) findViewById(R.id.selectDistanceButtonCancel);

        vzdalenosti = getResources().getStringArray(R.array.vzdalenosti);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, vzdalenosti);

        selectSearchDistanceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        selectSearchDistanceListView.setAdapter(adapter);
        // Show current selected distance
        for (int i = 0; i < vzdalenosti.length; i++) {
            if (vzdalenosti[i].equals(this.selectedDistance)) {
                selectSearchDistanceListView.setItemChecked(i, true);
                break;
            }
        }

        selectOKButton.setOnClickListener(this);
        selectCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // get selected distance
        Intent i = new Intent(this, MainActivity.class);
        if (v.getId() == R.id.selectDistanceButtonOK) {

            if (selectSearchDistanceListView.getCheckedItemPosition() >= 0
                && selectSearchDistanceListView.getCheckedItemPosition() < vzdalenosti.length) {
                this.selectedDistance = vzdalenosti[selectSearchDistanceListView.getCheckedItemPosition()];
            }
        }
        // back to main activity
        i.putExtra("searchRange", this.selectedDistance);
        this.startActivity(i);
        finish();
    }
}
