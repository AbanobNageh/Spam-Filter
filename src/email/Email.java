package email;

import java.util.ArrayList;

public class Email {
	private ArrayList<Word> filteredEmail = new ArrayList<Word>();  // the array list representation of the email after the email is preprocessed.
	private String className = "";                                  // the name of the class this email belongs to.
	private String emailText = "";                                  // the original text of the email
	private String predictedClassName = "";
	
	public Email (String emailText){
		// this email constructor will be used will unlabeled testing data.
		this.emailText = emailText;
		this.className = "";
	}
	
	public Email (String emailText, String className){
		// this constructor will be used with labeled training data.
		this.emailText = emailText;
		this.className = className;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getEmailText() {
		return emailText;
	}
	
	public ArrayList<Word> getFilteredEmail() {
		return filteredEmail;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setFilteredEmail(ArrayList<Word> filteredEmail) {
		this.filteredEmail = filteredEmail;
	}

	public String getPredictedClassName() {
		return predictedClassName;
	}

	public void setPredictedClassName(String predictedClassName) {
		this.predictedClassName = predictedClassName;
	}
}
