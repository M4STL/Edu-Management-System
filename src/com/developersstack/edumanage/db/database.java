package com.developersstack.edumanage.db;

import com.developersstack.edumanage.model.*;
import com.developersstack.edumanage.util.security.PasswordManager;

import java.util.ArrayList;

public class database {
    public static ArrayList<User> userTable = new ArrayList();
    public static ArrayList<Student> studentTable = new ArrayList();
    public static ArrayList<Teacher> teacherTable = new ArrayList();
    public static ArrayList<Program> programTable = new ArrayList();
    public static ArrayList<Intake> intakeTable = new ArrayList();
    public static ArrayList<Registration> registrationTable = new ArrayList();

    static {
        userTable.add(
                new User("Marina","Thumbovila","m@gmail.com",
                        new PasswordManager().encrypt("1234"))
        );
    }
}
