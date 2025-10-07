package com.example;

public class EmployeeRow {
    private final int employeeId;
    private final String employeeName;
    private final String role;
    private final String status;

    public EmployeeRow(int employeeId, String employeeName, String role, String status) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.role = role;
        this.status = status;
    }

    public int getEmployeeId() {
        return employeeId;
    }
    public String getEmployeeName() {
        return employeeName;
    }
    public String getRole() {
        return role;
    }
    public String getStatus() {
        return status;
    }
}
