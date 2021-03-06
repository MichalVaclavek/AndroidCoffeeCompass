package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.entity.repository.dao.relations.CoffeeSiteWithComments;
import io.reactivex.Flowable;

@Dao
public interface CommentDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM comment_table")
    LiveData<List<Comment>> getAllComments();

    @Query("SELECT * FROM comment_table WHERE text LIKE :stringValue  LIMIT 1")
    Flowable<Comment> getComment(String stringValue);

    @Query("SELECT * FROM comment_table WHERE id = :commentId  LIMIT 1")
    Flowable<Comment> getCommentById(int commentId);

    @Transaction
    @Query("SELECT * FROM coffee_site_table WHERE id = :coffeeSiteId LIMIT 1")
    LiveData<List<CoffeeSiteWithComments>> getCoffeeSiteWithComments(long coffeeSiteId);

    @Query("DELETE FROM comment_table")
    void deleteAll();

    @Insert
    void insertAll(List<Comment> comments);

    @Insert
    void insertComment(Comment comment);


}
