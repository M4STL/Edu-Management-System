package com.developersstack.edumanage.controller;

import com.developersstack.edumanage.db.DbConnection;
import com.developersstack.edumanage.db.database;
import com.developersstack.edumanage.model.Intake;
import com.developersstack.edumanage.model.Program;
import com.developersstack.edumanage.model.Student;
import com.developersstack.edumanage.model.Teacher;
import com.developersstack.edumanage.view.tm.IntakeTM;
import com.developersstack.edumanage.view.tm.StudentTM;
import com.developersstack.edumanage.view.tm.TechAddTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.*;
import java.util.Date;

import java.util.List;
import java.util.Optional;

public class IntakeFormController {
    public AnchorPane context;
    public TextField txtCode;
    public Button btn;
    public TableView<IntakeTM> tblIntakes;
    public TableColumn colId;
    public TableColumn colIntake;
    public TableColumn colStartDate;
    public TableColumn colProgram;
    public TableColumn colState;
    public TableColumn colOptions;
    public DatePicker txtDate;
    public ComboBox cmbProgram;
    public TextField txtName;
    public TextField txtSearch;
    public RadioButton rBtnCompleted;
    public RadioButton rBtnPending;
    String searchText = "";

    public  void initialize() {
        setIntakeId();
        setPrograms();
       // printDataOnAction();
        colId.setCellValueFactory(new PropertyValueFactory<IntakeTM, Integer>("intakeId"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colIntake.setCellValueFactory(new PropertyValueFactory<IntakeTM, Integer>("intakeName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("program"));
        colState.setCellValueFactory(new PropertyValueFactory<>("intakeState"));
        colOptions.setCellValueFactory(new PropertyValueFactory<>("btn"));

        setTableData(searchText);
        rBtnCompleted.setSelected(false);
        rBtnPending.setSelected(true);

    }

    public void newIntakeOnAction(ActionEvent actionEvent) {
        clear();
        setIntakeId();
        btn.setText("Save Intake");
    }

    private void setIntakeId() {
        try {
            String lastId = getLastId();
            if(lastId != null) {
                String splitData[] = lastId.split("-");
                String lastIdIntegerNumberAsAString = splitData[1];
                int lastIntegerIdAsInt = Integer.parseInt(lastIdIntegerNumberAsAString);
                lastIntegerIdAsInt++;
                String generatedProgramId = "I-"+lastIntegerIdAsInt;
                txtCode.setText(generatedProgramId);
            }else {
                txtCode.setText("I-1");
            }
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }



    }

    private void clear() {
        txtDate.setValue(null);
        txtName.clear();
        cmbProgram.setValue(null);
        rBtnCompleted.selectedProperty().set(false);
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        setUi("DashboardForm");
    }


    ObservableList<IntakeTM> tmList = FXCollections.observableArrayList();
    public void saveOnAction(ActionEvent actionEvent) {
        Intake intake = new Intake(
                txtCode.getText(),
                Date.from(txtDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                txtName.getText(),
                cmbProgram.getValue().toString(),
                rBtnCompleted.isSelected()
        );
        if(btn.getText().equalsIgnoreCase("Save Intake")){
            try{
                if (saveIntake(intake)){
                    setIntakeId();
                    clear();
                    setTableData(searchText);
                    new Alert(Alert.AlertType.INFORMATION,"Intake Added Successfully").show();
                }else {
                    new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                }
            } catch (SQLException | ClassNotFoundException e) {
                new Alert(Alert.AlertType.ERROR,e.toString()).show();
            }
        }else{
            try {
                if (updateIntake(intake)){
                    setTableData(searchText);
                    clear();
                    setIntakeId();
                    btn.setText("Save Intake");
                    new Alert(Alert.AlertType.INFORMATION,"Intake Updated").show();
                }else {
                    new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,e.toString()).show();
            }
        }
    }

    ArrayList<String> programsArray = new ArrayList<>();
    private void setPrograms() {
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


    private void setTableData(String searchText) {
        ObservableList<IntakeTM> obList = FXCollections.observableArrayList();
        try{
            for (Intake i : searchIntakes(searchText)){
                Button btn = new Button("Delete");
                String intakeState = i.isIntakeCompleteness() ? "Completed" : "Pending";
                IntakeTM tm = new IntakeTM(
                        i.getIntakeId(),
                        i.getStartDate(),
                        i.getIntakeName(),
                        i.getProgramId(),
                        intakeState,
                        btn
                );

                btn.setOnAction(e->{
                    Alert alert = new Alert(
                            Alert.AlertType.CONFIRMATION,"Are you sure?",
                            ButtonType.YES,ButtonType.NO
                    );
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if(buttonType.get().equals(ButtonType.YES)){
                        try {
                            if (deleteIntake(i.getIntakeId())){
                                new Alert(Alert.AlertType.INFORMATION,"Deleted").show();
                                setTableData(searchText);
                                setIntakeId();
                            }else {
                                new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                            }
                        } catch (SQLException | ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }

                    }
                });
                obList.add(tm);
            }
            tblIntakes.setItems(obList);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


    private void setUi(String location) throws IOException {
        Stage stage = (Stage)context.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/"+location+".fxml"))));
        stage.centerOnScreen();
    }
    //============================================================================================================
    private boolean saveIntake(Intake intake) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("INSERT INTO intake Values(?,?,?,?,?)");
        preparedStatement.setString(1,intake.getIntakeId());
        preparedStatement.setObject(2,intake.getStartDate());
        preparedStatement.setString(3,intake.getIntakeName());
        preparedStatement.setString(4,intake.getProgramId());
        preparedStatement.setBoolean(5,intake.isIntakeCompleteness());

        return preparedStatement.executeUpdate()>0;
    }

    private String getLastId() throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT intake_id FROM intake ORDER BY CAST(SUBSTRING(intake_id,3) AS UNSIGNED ) DESC LIMIT 1");

        ResultSet resultSet= preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getString(1);
        }
        return null;
    }

    private List<Intake> searchIntakes(String text) throws ClassNotFoundException, SQLException {
        text = "%"+text +"%";
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM intake WHERE intake_name LIKE ? OR program LIKE ? ");
        preparedStatement.setString(1,text);
        preparedStatement.setString(2,text);

        ResultSet resultSet= preparedStatement.executeQuery();
        List<Intake> list = new ArrayList<>();
        while(resultSet.next()){
            list.add(new Intake(
                    resultSet.getString(1),
                    resultSet.getDate(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getBoolean(5)
            ));

        }
        return list;

    }

    private boolean deleteIntake(String id) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("DELETE FROM intake WHERE intake_id=?");
        preparedStatement.setString(1,id);

        return preparedStatement.executeUpdate()>0;

    }

    private boolean updateIntake(Intake intake) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("UPDATE intake SET start_date=?,intake_name=?,program=?,completeState=? WHERE intake_id=?");

        preparedStatement.setString(1,intake.getIntakeId());
        preparedStatement.setObject(2,intake.getStartDate());
        preparedStatement.setString(3,intake.getIntakeName());
        preparedStatement.setString(4,intake.getProgramId());
        preparedStatement.setBoolean(5,intake.isIntakeCompleteness());
        return preparedStatement.executeUpdate()>0;
    }


   /* public void printDataOnAction(String state) {
        boolean completeState = rBtnCompleted.isSelected();
        String State = completeState?"Completed":"Pending";

    }*/
}
