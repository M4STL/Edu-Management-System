package com.developersstack.edumanage.controller;

import com.developersstack.edumanage.db.DbConnection;
import com.developersstack.edumanage.db.database;
import com.developersstack.edumanage.model.Program;
import com.developersstack.edumanage.model.Student;
import com.developersstack.edumanage.model.Teacher;
import com.developersstack.edumanage.view.tm.ProgramTM;
import com.developersstack.edumanage.view.tm.StudentTM;
import com.developersstack.edumanage.view.tm.TechAddTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;


public class ProgramFormController {


    public AnchorPane context;
    public TextField txtCode;
    public TextField txtName;
    public TextField txtTechnology;
    public ComboBox<String> cmbTeacher;
    public TextField txtCost;
    public TextField txtSearch;
    public Button btn;
    public TableView<ProgramTM> tblProgram;
    public TableColumn colId;
    public TableColumn colName;
    public TableColumn colTeacher;
    public TableColumn colTechnology;
    public TableColumn colCost;
    public TableColumn colOptions;
    public TableView<TechAddTM> tblTechnologies;
    public TableColumn colTCode;
    public TableColumn colTName;
    public TableColumn colTRemove;

    String searchtext ="";
   // private String searchText;

    public void initialize() throws NoSuchMethodException {
        setProgramId();
        setTeachers();
       // loadPrograms();
        setTableData(searchtext);

        colId.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        colTechnology.setCellValueFactory(new PropertyValueFactory<>("btnTech"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colOptions.setCellValueFactory(new PropertyValueFactory<>("btn"));

        colTCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colTName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTRemove.setCellValueFactory(new PropertyValueFactory<>("btn"));

        txtSearch.textProperty().addListener((observable,oldValue,newValue)->{
            searchtext = newValue;
            setTableData(searchtext);
        });
        tblProgram.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(null != newValue) {
                setData(newValue);
            }
        });
        
    }


    ArrayList<String> teachersArray = new ArrayList<>();
    private void setTeachers() throws NoSuchMethodException {
      try {
          TeacherFormController controller = new TeacherFormController();
          Class<? extends TeacherFormController> controllerClass = controller.getClass();
          Method method = controllerClass.getDeclaredMethod("searchTeachers", String.class);
          method.setAccessible(true);

          List<Teacher> teacherList = (List<Teacher>) method.invoke(controller,searchtext);

          for(Teacher t: teacherList  ){
              teachersArray.add(t.getCode()+"-"+t.getName());
          }
          ObservableList<String> obList = FXCollections.observableArrayList(teachersArray);
          cmbTeacher.setItems(obList);
      }catch (Exception e){
          e.printStackTrace();
      }
    }

    public void newProgramOnAction(ActionEvent actionEvent) {
        clear();
        setProgramId();
        btn.setText("Save Program");
    }

    private void setUi(String location) throws IOException {
        Stage stage = (Stage)context.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/"+location+".fxml"))));
        stage.centerOnScreen();

    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        setUi("DashboardForm");
    }

    public void saveOnAction(ActionEvent actionEvent) {
        String[] selectedTechs = new String[tmList.size()];
        int pointer = 0;
        for(TechAddTM t: tmList){
            selectedTechs[pointer]= t.getName();
            pointer++;
        }
        Program program = new Program(
                txtCode.getText(),
                txtName.getText(),
                selectedTechs,
                cmbTeacher.getValue().split("\\.")[0],
                Double.parseDouble(txtCost.getText())
        );
        try{
        if (btn.getText().equals("Save Program")){
                if(saveProgram(program)){
                    setProgramId();
                    clear();
                    setTableData(searchtext);
                    new Alert(Alert.AlertType.INFORMATION,"Saved").show();
                }else {
                    new Alert(Alert.AlertType.WARNING,"Try Again!").show();
                }
            } else {
                if(updateProgram(program)){
                    setTableData(searchtext);
                    clear();
                    //setProgramId();
                    btn.setText("Update Program");
                    new Alert(Alert.AlertType.INFORMATION,"Program Updated").show();
                }
            }

        }catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR,e.toString()).show();
        }

    }

   /* private void loadPrograms(){
        ObservableList<ProgramTM> programTmList = FXCollections.observableArrayList();
        for(Program p: database.programTable){
            Button techButton = new Button("show Tech");
            Button removeButton = new Button("Delete");
            ProgramTM tm = new ProgramTM(
                    p.getCode(),
                    p.getName(),
                    p.getTeacherId(),
                    techButton,
                    p.getCost(),
                    removeButton
            );
            programTmList.add(tm);
        }
        tblProgram.setItems(programTmList);
    }*/
    private void setTableData(String searchText) {
        ObservableList<ProgramTM> obList = FXCollections.observableArrayList();

        try {
            for (Program p : searchPrograms(searchText)) {
                Button techButton = new Button("show Tech");
                Button removeButton = new Button("Delete");
                ProgramTM tm = new ProgramTM(
                        p.getCode(), p.getName(),
                        p.getTeacherId(),
                        techButton,
                        p.getCost(),
                        removeButton
                );
               
                techButton.setOnAction(e -> {
                            // Show technologies in a dialog or another UI component
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Technologies");
                            alert.setHeaderText("Technologies for Program: " + p.getName());
                            alert.setContentText(String.join(", ", p.getTechnologies()));
                            alert.showAndWait();
                        });


                removeButton.setOnAction(e -> {
                    Alert alert = new Alert(
                            Alert.AlertType.CONFIRMATION, "Are you sure?",
                            ButtonType.YES, ButtonType.NO
                    );
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.get().equals(ButtonType.YES)) {
                        try {
                            if (deleteProgram(p.getCode())) {
                                new Alert(Alert.AlertType.INFORMATION, "Deleted").show();
                                setTableData(searchText);
                                setProgramId();
                            } else {
                                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
                            }
                        } catch (SQLException | ClassNotFoundException ex) {
                            new Alert(Alert.AlertType.ERROR, ex.toString()).show();
                        }
                    }
                });
                obList.add(tm);
            }
            tblProgram.setItems(obList);
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*private void showTech(Stage primaryStage,Button techButton) {
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(String.valueOf(tmList));
        // Initially hide the ListView
        listView.setVisible(false);
        // Set Button action to show/hide the ListView
        techButton.setOnAction(event -> {
            // Toggle the visibility of the ListView
            listView.setVisible(!listView.isVisible());
        });

        // Layout using VBox
        VBox vbox = new VBox(10, techButton, listView);
        vbox.setSpacing(10);

        // Create the Scene
        Scene scene = new Scene(vbox, 300, 200);

        // Set the Stage

        primaryStage.setTitle("Show List on Button Click");
        primaryStage.setScene(scene);
        primaryStage.show();

    }*/

    private void clear() {
        txtName.clear();
        txtCost.clear();
        cmbTeacher.setValue(null);
        tblTechnologies.setItems(null);
    }


    private void setProgramId() {
        try {
            String lastId = getLastId();
            if(lastId != null) {
                String splitData[] = lastId.split("-");
                String lastIdIntegerNumberAsAString = splitData[1];
                int lastIntegerIdAsInt = Integer.parseInt(lastIdIntegerNumberAsAString);
                lastIntegerIdAsInt++;
                String generatedProgramId = "P-"+lastIntegerIdAsInt;
                txtCode.setText(generatedProgramId);
            }else {
                txtCode.setText("P-1");
            }
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }


    }

    private void setData(ProgramTM tm) {
        txtCode.setText(tm.getCode());
        txtName.setText(tm.getName());
        txtCost.setText(String.valueOf(tm.getCost()));
    }

    ObservableList<TechAddTM> tmList = FXCollections.observableArrayList();
    public void addTechOnAction(ActionEvent actionEvent) {
        if(!isExists(txtTechnology.getText().trim())){
            Button btn = new Button("Remove");
            TechAddTM tm = new TechAddTM(
                    tmList.size()+1,txtTechnology.getText().trim(),btn
            );
            btn.setOnAction(e->{
                Alert alert = new Alert(
                        Alert.AlertType.CONFIRMATION,"Are you sure?",
                        ButtonType.YES,ButtonType.NO
                );
                Optional<ButtonType> buttonType = alert.showAndWait();
                if(buttonType.get().equals(ButtonType.YES)){
                    tmList.remove(tm);
                    new Alert(Alert.AlertType.INFORMATION,"Deleted").show();
                    setTableData(searchtext);
                    setProgramId();
                }
            });
            tmList.add(tm);
            tblTechnologies.setItems(tmList);
            txtTechnology.clear();
        }else{
            txtTechnology.selectAll();
            new Alert(Alert.AlertType.WARNING,"Already Exists!").show();
        }
    }

    private  boolean isExists(String tech){
        for(TechAddTM tm: tmList){
            if (tm.getName().equals(tech)){
                return true;
            }
        }
        return false;
    }
    //===================================================================================
    private boolean saveProgram(Program program) throws ClassNotFoundException, SQLException {
        // Convert technologies array to a comma-separated string
        String technologiesString = String.join(",", program.getTechnologies());

        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("INSERT INTO program Values(?,?,?,?,?)");
        preparedStatement.setString(1,program.getCode());
        preparedStatement.setString(2,program.getName());
        preparedStatement.setString(3,technologiesString);
        preparedStatement.setString(4,program.getTeacherId());
        preparedStatement.setDouble(5,program.getCost());

        return preparedStatement.executeUpdate()>0;
    }

    private String getLastId() throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT program_id FROM program ORDER BY CAST(SUBSTRING(program_id,3) AS UNSIGNED ) DESC LIMIT 1");

        ResultSet resultSet= preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getString(1);
        }
        return null;
    }

    private List<Program> searchPrograms(String text) throws ClassNotFoundException, SQLException {
        text = "%"+text +"%";
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM program WHERE name LIKE ? OR teacher LIKE ?");
        preparedStatement.setString(1,text);
        preparedStatement.setString(2,text);
        ResultSet resultSet= preparedStatement.executeQuery();
        List<Program> list = new ArrayList<>();
        while(resultSet.next()){
            String technologiesString = resultSet.getString("technologies"); // Assuming technologies is stored as a comma-separated string
            String[] technologiesArray = technologiesString.split(","); // Convert it to String[]
            list.add(new Program(
                    resultSet.getString(1),
                    resultSet.getString(2),
                    technologiesArray,
                    resultSet.getString(4),
                    resultSet.getDouble(5)
            ));
            //    System.out.println(list);
        }
        return list;
        // System.out.println(list);
    }

    private boolean deleteProgram(String id) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("DELETE FROM program WHERE program_id=?");
        preparedStatement.setString(1,id);

        return preparedStatement.executeUpdate()>0;

    }

    private boolean updateProgram(Program program) throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("UPDATE program SET name=?,technologies=?,teacher=?,cost=? WHERE program_id=?");

        preparedStatement.setString(1,program.getCode());
        preparedStatement.setString(2,program.getName());
        preparedStatement.setObject(3,program.getTechnologies());
        preparedStatement.setString(4,program.getTeacherId());
        preparedStatement.setDouble(5,program.getCost());
        return preparedStatement.executeUpdate()>0;
    }
}
