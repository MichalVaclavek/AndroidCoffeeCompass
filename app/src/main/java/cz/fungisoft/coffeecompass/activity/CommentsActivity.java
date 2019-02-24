package cz.fungisoft.coffeecompass.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass.entity.Comment;

public class CommentsActivity extends AppCompatActivity {

    private CoffeeSite cs;
    private List<Comment> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        cs = (CoffeeSite) getIntent().getSerializableExtra("site");
        comments = cs.getComments();

        View recyclerView = findViewById(R.id.commentsList);
        assert recyclerView != null;

        //TODO recyclerView
    }

}
