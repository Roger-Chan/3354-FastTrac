package utdallas.students.fasttrac;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;



public class CoursesDatabase extends SQLiteOpenHelper {
    private static CoursesDatabase cd = null;

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "course_manager.db";
    public static final String COURSES_TABLE_NAME = "COURSES";
    public static final String KEY_ID = "ID";                         //column index 0
    public static final String KEY_NAME= "COURSE_NAME";               //column index 1
    public static final String KEY_CODE = "COURSE_CODE";              //column index 2
    public static final String KEY_HOUR = "COURSE_HOUR";              //column index 3
    public static final String KEY_MINUTE = "COURSE_MINUTE";          //column index 4
    public static final String KEY_INSTRUCTOR = "COURSE_INSTRUCTOR";  //column index 5
    public static final String IS_ON = "ISON";                        //column index 6
    public static final String LATEST_TIME = "LATESTTIME";            //column index 7

    private CoursesDatabase(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        deleteAll();
        addCourse(new Course("SE_3354", "Software_Engineering", "12345", 10, 30, "Dr. WEI", 0, ""));
        addCourse(new Course("CE_2337", "CompSci_2", "13346", 11, 0, "jason smith", 0, ""));
    }

    public static CoursesDatabase getInstance(Context context){
        if (cd == null){
            cd = new CoursesDatabase(context);
        }
        return cd;
    }

    @Override
    public void onCreate(SQLiteDatabase cd) {
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + COURSES_TABLE_NAME + "(" +
                KEY_ID          +   " TEXT NOT NULL, "  +   // column 0
                KEY_NAME        +   " TEXT NOT NULL, "  +   // column 1
                KEY_CODE        +   " TEXT NOT NULL, "  +   // column 2
                KEY_HOUR        +   " INT, "            +   // column 3
                KEY_MINUTE      +   " INT, "            +   // column 4
                KEY_INSTRUCTOR  +   " TEXT, "            +   // column 5
                IS_ON           +   " INT DEFAULT 0,"    +   // column 6
                LATEST_TIME     +   " String);";             // column 7


        cd.execSQL(SQL_CREATE_USER_TABLE);
    }

    //Creates a table to log attendance for each class
    String KEY_STUDENT_FIRST_NAME = "First_Name";
    String KEY_STUDENT_LAST_NAME = "Last_Name";

    public void newClassTable(Course course){
        SQLiteDatabase cd = this.getWritableDatabase();
        String CLASS_TABLE_NAME = course.getId() + "_" + course.getName() + "_" + course.getHour() + course.getMinute();//needs refactoring

        final String SQL_CREATE_CLASS_TABLE = "CREATE TABLE IF NOT EXISTS " + "'" + CLASS_TABLE_NAME + "'" +  "(" +
                KEY_STUDENT_FIRST_NAME     +   " STRING, " +  // column 0
                KEY_STUDENT_LAST_NAME      +   " STRING);";   // column 1
        cd.execSQL(SQL_CREATE_CLASS_TABLE);
    }

    public boolean addSession(Course course)
    {
        SQLiteDatabase cd = this.getWritableDatabase();
        //current date in numerical format, e.g. 08/12/18
        String CLASS_TABLE_NAME = course.getId() + "_" + course.getName() + "_" + course.getHour() + course.getMinute();//needs refactoring
        cd.execSQL("ALTER TABLE " + CLASS_TABLE_NAME + " ADD COLUMN " + "'" + course.getLatestTime() + "'" + " INT;");
        return true;
    }

    //****NEEDS TESTING ****
    //takes the attendance of a student in course table. If the student does not exist in the course table, the student is then added.
    public boolean attendStudentInCourse(User student, Course course) {
        SQLiteDatabase cd = this.getWritableDatabase();
        String CLASS_TABLE_NAME = course.getId() + "_" + course.getName() + "_" + course.getHour() + course.getMinute();//needs refactoring

        //if professor has not turned on the course yet exit
        if (!course.isAvailable()) {
            return false;
        }

        //if student has never attended course before, add their row
        Cursor cursor = cd.rawQuery("SELECT * FROM " + CLASS_TABLE_NAME + " WHERE First_Name = ? AND Last_Name = ? ",
                                                                new String[]{student.getFirst_name(),student.getLast_name()});
        if (!cursor.moveToNext())
        {
            ContentValues studentvalues = new ContentValues();
            studentvalues.put(KEY_STUDENT_FIRST_NAME, student.getFirst_name());
            studentvalues.put(KEY_STUDENT_LAST_NAME, student.getLast_name());
            cd.insert(CLASS_TABLE_NAME, null, studentvalues);
        }
        //otherwise, just tick their attendance on the current date
        ContentValues datevalue = new ContentValues();
        datevalue.put("'" + course.getLatestTime() + "'", 1);
        cd.update(CLASS_TABLE_NAME, datevalue, KEY_STUDENT_FIRST_NAME + " = ? AND " + KEY_STUDENT_LAST_NAME+" = ?", new String [] {student.getFirst_name(), student.getLast_name()});
        return true;
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + COURSES_TABLE_NAME);
        onCreate(db);
    }

    public boolean addCourse(Course course){
        SQLiteDatabase cd = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ID, course.getId());
        values.put(KEY_NAME , course.getName());
        values.put(KEY_CODE, course.getCode());
        values.put(KEY_HOUR , course.getHour());
        values.put(KEY_MINUTE, course.getMinute());
        values.put(KEY_INSTRUCTOR, course.getInstructor());

        // find all the courses the instructor is in
        Cursor cursor = cd.rawQuery("SELECT * FROM " + COURSES_TABLE_NAME + " WHERE COURSE_CODE = ?",new String[]{course.getCode()});

        // means our course code is already in the database comewhere
        while(cursor.moveToNext()){
            return false;
        }

        // if we didn't find the course then add it and return true
        cd.insert(COURSES_TABLE_NAME, null, values);
        //create new table for the course
        newClassTable(course);
        return true;
    }

    //returns course object from Courses table
    public Course findCourse(String code) {
        SQLiteDatabase cd = this.getWritableDatabase();

        // query to find the username
        Cursor cursor = cd.rawQuery("select * from " + COURSES_TABLE_NAME, null);

        while (cursor.moveToNext()) {
            if (cursor.getString(2).contains(code)){
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                int hour = cursor.getInt(3);
                int minute = cursor.getInt(4);
                String instructor = cursor.getString(5);
                int ison = cursor.getInt(6);
                String latesttime = cursor.getString(7);
                return new Course(id, name, code, hour, minute, instructor, ison, latesttime);
            }
        }
        return null;
    }

    public void deleteAll(){
        SQLiteDatabase cd = this.getWritableDatabase();
        cd.execSQL("DELETE FROM " + COURSES_TABLE_NAME);
    }

    public ArrayList<String> getProfessorCourses(String firstName, String lastName){
        SQLiteDatabase cd = this.getWritableDatabase();
        ArrayList<String> names = new ArrayList<String>();

        // search for the student and get all the codes they have
        Cursor cursor = cd.rawQuery("SELECT * FROM " + COURSES_TABLE_NAME + " WHERE COURSE_INSTRUCTOR = ?",new String[] {firstName + " " + lastName});

        while(cursor.moveToNext()){
            names.add(cursor.getString(1));
        }

        return names;
    }

    public boolean isOn(String code){
        SQLiteDatabase cd = this.getWritableDatabase();
        // search for the student and get all the codes they have
        Cursor cursor = cd.rawQuery("SELECT * FROM " + COURSES_TABLE_NAME + " WHERE COURSE_CODE = ?",new String[] {code});

        return (cursor.getInt(6) == 1);
    }
    public int updateTime(String code, String time){
        SQLiteDatabase cd = this.getWritableDatabase();
        // search for the student and get all the codes they have
        Cursor cursor = cd.rawQuery("SELECT * FROM " + COURSES_TABLE_NAME + " WHERE COURSE_CODE = ?",new String[] {code});
        if (!cursor.moveToNext())
        {
            return 0;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(LATEST_TIME, time);

        return cd.update(COURSES_TABLE_NAME, contentValues, "COURSE_CODE = ?", new String[]{code});
    }

    public int toggleClass(String code, int available){
        SQLiteDatabase cd = this.getWritableDatabase();
        // search for the student and get all the codes they have
        Cursor cursor = cd.rawQuery("SELECT * FROM " + COURSES_TABLE_NAME + " WHERE COURSE_CODE = ?",new String[] {code});
        if (!cursor.moveToNext())
        {
            return 0;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(IS_ON, available);

        return cd.update(COURSES_TABLE_NAME, contentValues, "COURSE_CODE = ?", new String[]{code});

    }

    public void deleteCourse(String code){
        SQLiteDatabase cd = this.getWritableDatabase();
        cd.delete(COURSES_TABLE_NAME, "COURSE_CODE = ?", new String[]{code});
    }


    public ArrayList<String> getProfessorCoursesCodes(String firstName, String lastName){
        SQLiteDatabase cd = this.getWritableDatabase();
        ArrayList<String> codes = new ArrayList<String>();

        // search for the student and get all the codes they have
        Cursor cursor = cd.rawQuery("SELECT * FROM " + COURSES_TABLE_NAME + " WHERE COURSE_INSTRUCTOR = ?",new String[] {firstName + " " + lastName});

        while(cursor.moveToNext()){
            codes.add(cursor.getString(2));
        }

        return codes;
    }
}
