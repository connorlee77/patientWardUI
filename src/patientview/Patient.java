/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientview;

/**
 *
 * @author s1571182
 */
public class Patient {
    
    private final String ward;
    private final String bed;
    private final String firstName;
    private final String lastName;
    private final String sex;
    private final String dob;
    
    public Patient(String [] data, String ward) {
        this.ward = ward;
        bed = data[0];
        firstName = data[1];
        lastName = data[2];
        sex = data[3];
        dob = data[4];
    }
    
    public String getWard() {
        return ward;
    }
    
    public String getBed() {
        return bed;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getSex() {
        return sex;
    }
    
    public String getDOB() {
        return dob;
    }
}
