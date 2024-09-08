package com.developersstack.edumanage.view.tm;

import javafx.scene.control.Button;

import java.util.Date;

public class IntakeTM {
      private String intakeId;
      private Date startDate;
      private String intakeName;
      private String program;
      private String intakeState;
      private Button btn;


    public IntakeTM() {
    }

    public IntakeTM(String intakeId, Date startDate, String intakeName, String program, String intakeState, Button btn) {
        this.intakeId = intakeId;
        this.startDate = startDate;
        this.intakeName = intakeName;
        this.program = program;
        this.intakeState = intakeState;
        this.btn = btn;
    }

    public String getIntakeId() {
        return intakeId;
    }

    public void setIntakeId(String intakeId) {
        this.intakeId = intakeId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getIntakeName() {
        return intakeName;
    }

    public void setIntakeName(String intakeName) {
        this.intakeName = intakeName;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getIntakeState() {
        return intakeState;
    }

    public void setIntakeState(String intakeState) {
        this.intakeState = intakeState;
    }

    public Button getBtn() {
        return btn;
    }

    public void setBtn(Button btn) {
        this.btn = btn;
    }

    @Override
    public String toString() {
        return "IntakeTM{" +
                "intakeId='" + intakeId + '\'' +
                ", startDate=" + startDate +
                ", intakeName='" + intakeName + '\'' +
                ", program='" + program + '\'' +
                ", intakeState='" + intakeState + '\'' +
                ", btn=" + btn +
                '}';
    }
}
