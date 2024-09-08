package com.developersstack.edumanage.controller;

import com.developersstack.edumanage.db.DbConnection;
import com.developersstack.edumanage.db.database;
import com.developersstack.edumanage.model.Student;
import com.developersstack.edumanage.model.Teacher;
import com.developersstack.edumanage.view.tm.StudentTM;
import com.developersstack.edumanage.view.tm.TeacherTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TeacherFormController {
    public AnchorPane context;
    public TextField txtCode;
    public TextField txtSearch;
    public TextField txtAddress;
    public TextField txtName;
    public Button btn;
    public TableView<TeacherTM> tblTeachers;
    public TableColumn colId;
    public TableColumn colName;
    public TableColumn colAddress;
    public TableColumn colContact;
    public TableColumn colOptions;
    public TextField txtContact;
    String searchText = "";

    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colOptions.setCellValueFactory(new PropertyValueFactory<>("btn"));
        setTeacherCode();
        setTableData(searchText);

        txtSearch.textProperty().addListener((observable,oldValue,newValue)->{
            searchText = newValue;
            setTableData(searchText);
        });

        tblTeachers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(null != newValue) {
                setData(newValue);
            }

        });
    }

    private void setData(TeacherTM tm) {
        txtCode.setText(tm.getCode());
        txtName.setText(tm.getName());
        txtAddress.setText(tm.getAddress());
        txtContact.setText(tm.getContact());
        btn.setText("Update Teacher");
    }

    private void setTableData(String searchText){
        ObservableList<TeacherTM> obList = FXCollections.observableArrayList();
        try {
            for(Teacher tch:searchTeachers(searchText)){
                    Button btn = new Button("Delete");
                    TeacherTM tm = new TeacherTM(
                            tch.getCode(),tch.getName(),
                            tch.getAddress(),tch.getContact(),btn
                    );
                    btn.setOnAction(e->{
                        Alert alert = new Alert(
                                Alert.AlertType.CONFIRMATION,"Are you sure?",
                                ButtonType.YES,ButtonType.NO
                        );
                        Optional<ButtonType> buttonType = alert.showAndWait();
                        if(buttonType.get().equals(ButtonType.YES)){
                            try {
                                if (deleteTeacher(tch.getCode())){
                                    new Alert(Alert.AlertType.INFORMATION,"Deleted").show();
                                    setTableData(searchText);
                                    setTeacherCode();
                                }else {
                                    new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                                }
                            } catch (SQLException | ClassNotFoundException ex) {
                                new Alert(Alert.AlertType.ERROR,ex.toString()).show();
                            }
                            // database.teacherTable.remove(tch);
                        }
                    });
                    obList.add(tm);
            }
            tblTeachers.setItems(obList);

        }catch (SQLException |ClassNotFoundException e){
            e.printStackTrace();
        }

    }


    public void newTeacherOnAction(ActionEvent actionEvent) {
        clear();
        setTeacherCode();
        btn.setText("Save Teacher");
    }


    public void saveOnAction(ActionEvent actionEvent) {
        Teacher teacher = new Teacher(
                txtCode.getText(),txtName.getText(),
                txtAddress.getText(),txtContact.getText()) ;
        if(btn.getText().equalsIgnoreCase("Save Teacher")){
            try {
                if(saveTeacher(teacher)){
                    setTeacherCode();
                    clear();
                    setTableData(searchText);
                    new Alert(Alert.AlertType.INFORMATION,"Teacher Added Successfully").show();
                }else {
                    new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                }
            } catch (SQLException | ClassNotFoundException e) {
                new Alert(Alert.AlertType.ERROR,e.toString()).show();
            }
        }else{
           /* Optional<Student> selectedStudent = database.studentTable.stream().filter(e->e.getStudentId().equals(txtId.getText())).findFirst();
            if(!selectedStudent.isPresent()){
                new  Alert(Alert.AlertType.WARNING,"Not Found").show();
                return;
            }
            selectedStudent.get().setAddress();*/
            try {
                if(updateTeacher(teacher)){
                    //  setStudentId();
                    clear();
                    setTableData(searchText);
                    new Alert(Alert.AlertType.INFORMATION,"Teacher Updated").show();
                }else {
                    new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                }
            } catch (SQLException | ClassNotFoundException e) {
                new Alert(Alert.AlertType.ERROR,e.toString()).show();
            }
        }
    }

    private void setTeacherCode() {
        try {
            String lastId = getLastId();
            if (lastId != null){
                String splitData[] = lastId.split("-");
                String lastIdIntegerNumberAsAString = splitData[1];
                int lastIntegerIdAsInt = Integer.parseInt(lastIdIntegerNumberAsAString);
                lastIntegerIdAsInt++;
                String generatedStudentId = "T-"+lastIntegerIdAsInt;
                txtCode.setText(generatedStudentId);
            }else{
                txtCode.setText("T-1");
            }
        } catch (SQLException | ClassNotFoundException e) {
           e.printStackTrace();
        }
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        setUi("DashboardForm");
    }
    private void setUi(String location) throws IOException {
        Stage stage = (Stage)context.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/"+location+".fxml"))));
        stage.centerOnScreen();

    }
    private void clear(){
        txtContact.clear();
        txtName.clear();
        txtAddress.clear();
    }

    private boolean saveTeacher(Teacher teacher) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("INSERT INTO teacher VALUES(?,?,?,?)");
        preparedStatement.setString(1,teacher.getCode());
        preparedStatement.setString(2,teacher.getName());
        preparedStatement.setString(3,teacher.getAddress());
        preparedStatement.setString(4,teacher.getContact());

        return preparedStatement.executeUpdate()>0;
    }
    private String getLastId() throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT teacher_id FROM teacher ORDER BY CAST(SUBSTRING(teacher_id,3) AS UNSIGNED ) DESC LIMIT 1");

        ResultSet resultSet= preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getString(1);
        }
        return null;
    }
    private List<Teacher> searchTeachers(String text) throws ClassNotFoundException, SQLException {
        text = "%"+text +"%";
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM teacher WHERE name LIKE ? OR address LIKE ?");
        preparedStatement.setString(1,text);
        preparedStatement.setString(2,text);
        ResultSet resultSet= preparedStatement.executeQuery();
        List<Teacher> list = new ArrayList<>();
        while(resultSet.next()){
            list.add(new Teacher(
                    resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4)
            ));
            //    System.out.println(list);
        }
        return list;
    }

    private boolean deleteTeacher(String id) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("DELETE FROM teacher WHERE teacher_id=?");
        preparedStatement.setString(1,id);

        return preparedStatement.executeUpdate()>0;

    }

    private boolean updateTeacher(Teacher teacher) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("UPDATE teacher SET name=?,address=?,contact=? WHERE teacher_id=?");

        preparedStatement.setString(1,teacher.getName());
        preparedStatement.setString(2,teacher.getAddress());
        preparedStatement.setString(3,teacher.getContact());
        preparedStatement.setString(4,teacher.getCode());
        return preparedStatement.executeUpdate()>0;
    }
}

