package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.Comment;

public class CommentDBHelper extends SQLiteHelperBase<Comment> {

    private static final String TABLE_NAME = "COMMENT";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_COMMENT_TEXT = "comment_text";

    public static final String COL_CREATED_ON = "created";

    public static final String COL_USER_NAME = "user_name";

    public static final String COL_COFFEE_SITE_ID = "coffee_site_id";

    private static String[] columnNames = new String[] { ID, COL_COMMENT_TEXT, COL_CREATED_ON,
            COL_USER_NAME, COL_COFFEE_SITE_ID };

    // Creating table query
    private static final String CREATE_COMMENTS_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_COMMENT_TEXT + " TEXT NOT NULL, "
            + COL_CREATED_ON + " TEXT NOT NULL, "
            + COL_USER_NAME + " TEXT NOT NULL, "
            + COL_COFFEE_SITE_ID + " INTEGER);";

    //private CoffeeSiteDBHelper coffeeSiteDBHelper;

    public CommentDBHelper(Context context, DBManager dbManager) {
        super(context);
        SQLiteHelperBase.dbManager = dbManager;

//        coffeeSiteDBHelper = new CoffeeSiteDBHelper(context, dbManager);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_COMMENTS_TABLE;
    }

    @Override
    public ContentValues getContentValues(Comment entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_COMMENT_TEXT, entity.getText());
        contentValue.put(COL_CREATED_ON, entity.getCreatedOnString());
        contentValue.put(COL_USER_NAME, entity.getUserName());

        contentValue.put(COL_COFFEE_SITE_ID, entity.getCoffeeSiteID());

        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public Comment getValue(Cursor cursor) {
        Comment comment = new Comment();
        comment.setId(getColumnIntValue(cursor, ID));
        comment.setText(getColumnStringValue(cursor, CommentDBHelper.COL_COMMENT_TEXT));
        comment.setUserName(getColumnStringValue(cursor, CommentDBHelper.COL_USER_NAME));
        comment.setCoffeeSiteID(getColumnIntValue(cursor, CommentDBHelper.COL_COFFEE_SITE_ID));
        comment.setCreatedOnString(getColumnStringValue(cursor, CommentDBHelper.COL_CREATED_ON));
        return comment;
    }

}
