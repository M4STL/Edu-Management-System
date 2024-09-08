package com.developersstack.edumanage.controller;

import com.developersstack.edumanage.db.DbConnection;
import com.developersstack.edumanage.db.database;
import com.developersstack.edumanage.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegistrationFormController {
    public TextField txtId;
    public TextField txtSearch;
    public ComboBox<String> cmbProgram;
    public AnchorPane context;
    public RadioButton rBtnPending;
    public ToggleGroup PaymentState;
    public RadioButton rBtnPaid;
    public Button btn;
    public ComboBox<String> cmbStudent;
    String searchText = "";

    public void initialize() {
        setRegistrationId();
        setProgram();
        setStudent();

        rBtnPaid.setSelected(false);
        rBtnPending.setSelected(true);

    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        setUi("DashboardForm");
    }

    public void saveOnAction(ActionEvent actionEvent) {
        Registration registration = new Registration(
                txtId.getText(),
                cmbStudent.getValue().toString(),
                cmbProgram.getValue().toString(),
                rBtnPaid.isSelected()
        );
        if(btn.getText().equalsIgnoreCase("Save Registration")) {
            try {
                if (saveRegistration(registration)){
                    setRegistrationId();
                    clear();
                    new Alert(Alert.AlertType.INFORMATION, "Registration Saved").show();
                }else {
                    new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        /*else{
            for (Registration rg:database.registrationTable){
                if(rg.getRegistrationID().equals(txtId.getText())){
                    rg.setName(txtSearch.getText());
                    rg.setPrograms(new String[]{cmbProgram.getValue().split("\\.")[0]});
                    rg.setPaid(true);
                    clear();
                    setRegistrationId();
                    btn.setText("Save Registration");
                    return;
                }
            }
            new Alert(Alert.AlertType.WARNING,"Not Found").show();
        }*/

    }

    private boolean saveRegistration(Registration registration) throws SQLException, ClassNotFoundException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("INSERT INTO registration Values(?,?,?,?)");
        preparedStatement.setString(1,registration.getRegistrationID());
        preparedStatement.setString(2,registration.getName());
        preparedStatement.setString(3,registration.getPrograms());
        preparedStatement.setBoolean(4,registration.isPaid());

        return preparedStatement.executeUpdate()>0;
    }

    private void clear() {
        cmbStudent.setValue(null);
        cmbProgram.setValue(null);
        rBtnPaid.setSelected(false);
        rBtnPending.setSelected(true);
    }

    private void setRegistrationId() {
        try{
            String lastId = getLastId();
            if(lastId != null) {
                String splitData[] = lastId.split("-");
                String lastIdIntegerNumberAsAString = splitData[1];
                int lastIntegerIdAsInt = Integer.parseInt(lastIdIntegerNumberAsAString);
                lastIntegerIdAsInt++;
                String generatedRegistrationId = "R-"+lastIntegerIdAsInt;
                txtId.setText(generatedRegistrationId);
            }else{
                txtId.setText("R-1");
            }
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    private void setUi(String location) throws IOException {
        Stage stage = (Stage)context.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/"+location+".fxml"))));
        stage.centerOnScreen();
    }

    ArrayList<String> programsArray = new ArrayList<>();
    private void setProgram() {
        try {
            ProgramFormController controller = new ProgramFormController();
            Class<? extends ProgramFormController> controllerClass = controller.getClass();
            Method method = controllerClass.getDeclaredMethod("searchPrograms", String.class);
            method.setAccessible(true);

            List<Program> programList = (List<Program>) method.invoke(controller,searchText);

            for(Program p: programList ){
                programsArray.add(p.getCode()+"-"+p.getName());
            }
            ObservableList<String> obList = FXCollections.observableArrayList(programsArray);
            cmbProgram.setItems(obList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    ArrayList<String> studentsArray = new ArrayList<>();
    private void setStudent() {
        try {
          
            List<User> userList = searchStudents(searchText);

            for (User user : userList) {
                studentsArray.add(user.getFirstName() + " " + user.getLastName());
            }
            ObservableList<String> obList = FXCollections.observableArrayList(studentsArray);
            cmbStudent.setItems(obList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getLastId() throws SQLException, ClassNotFoundException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT registration_id FROM registration ORDER BY CAST(SUBSTRING(registration_id,3) AS UNSIGNED ) DESC LIMIT 1");

        ResultSet resultSet= preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getString(1);
        }
        return null;
    }

    private List<User> searchStudents(String text) throws ClassNotFoundException, SQLException {
        text = "%" + text + "%";
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM user WHERE first_name LIKE ? OR last_name LIKE ? ");
        preparedStatement.setString(1, text);
        preparedStatement.setString(2, text);

        ResultSet resultSet = preparedStatement.executeQuery();
        List<User> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(new User(
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getString(1)

            ));



        }
        return list;
    }

}






