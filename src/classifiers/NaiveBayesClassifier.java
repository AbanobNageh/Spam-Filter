package classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import email.Email;
import email.Word;

public class NaiveBayesClassifier {
	private ArrayList<Email> exampleEmails = new ArrayList<Email>();   // this array list is used to hold all the training data.
	private ArrayList<Email> testEmails = new ArrayList<Email>();      // this array list is used to hold all the testing data.
	private ArrayList<String> classes = new ArrayList<String>();       // this array list holds all the classes (in this case spam and ham)
	private ArrayList<String> stopWords = new ArrayList<String>();
	private ArrayList<Word> vocabulary = new ArrayList<Word>();
	private BigDecimal spamClassProbability = new BigDecimal(0);
	private BigDecimal hamClassProbability = new BigDecimal(0);
	private StanfordCoreNLP pipeline = null;
	
	public NaiveBayesClassifier(){
		// initialize the classes array list.
		classes.add("spam");
		classes.add("ham"); // ham = non-spam
		loadStopWordsList();
	}
	
	// used to read any test data provided in 'batch mode' from file.
	public void fit(){
		int itemsDone = 0, totalItems = 0;
		System.out.println("starting to fit testing data.");
		//readTestingData();
		readTestingDataNew();
		preprocess(this.testEmails);
		
		System.out.println("starting class prediction calculations.");
		totalItems = this.testEmails.size();
		for (Email email: this.testEmails){
			BigDecimal spamProbability = this.spamClassProbability;
			BigDecimal hamProbability = this.hamClassProbability;
			
			for (Word word: email.getFilteredEmail()){
				if (this.vocabulary.contains(word)){
					spamProbability = spamProbability.multiply(vocabulary.get(vocabulary.indexOf(word)).getSpamProbability());
					hamProbability = hamProbability.multiply(vocabulary.get(vocabulary.indexOf(word)).getHamProbability());
				}
				else{
					double spamEmailsCount = 0, hamEmailsCount = 0;
					
					for (Email tempEmail: this.exampleEmails){
						if (tempEmail.getClassName().equals("ham")){
							hamEmailsCount++;
						}
						else{
							spamEmailsCount++;
						}
					}
					
					spamProbability = spamProbability.multiply(new BigDecimal(1/(spamEmailsCount + this.vocabulary.size() + 1)));
					hamProbability = hamProbability.multiply(new BigDecimal(1/(hamEmailsCount + this.vocabulary.size() + 1)));
				}
			}
			
			if (spamProbability.compareTo(hamProbability) == 1){
				email.setPredictedClassName("spam");
			}
			else if (spamProbability.compareTo(hamProbability) == -1){
				email.setPredictedClassName("ham");
			}
			else {
				System.out.println("can't decide, equal probabilities!!");
			}
			itemsDone++;
			System.out.println("donw predicting " + itemsDone + "\\" + totalItems + " Emails");
		}
		System.out.println("done fitting data.");
		
		float spamEmails = 0, hamEmails = 0, predictedSpamEmails = 0, predictedHamEmails = 0, errors = 0;
		for (Email email: this.testEmails){
			if (email.getPredictedClassName().equals("")){
				errors++;
				continue;
			}
			
			if (email.getClassName().equals("spam")){
				spamEmails++;
				if (email.getPredictedClassName().equals("spam")){
					predictedSpamEmails++;
				}
			}
			else if (email.getClassName().equals("ham")){
				hamEmails++;
				if (email.getPredictedClassName().equals("ham")){
					predictedHamEmails++;
				}
			}
		}
		
		System.out.println("number of error emails: " + errors);
		System.out.println("number of spam emails: " + spamEmails);
		System.out.println("number of ham emails: " + hamEmails);
		System.out.println("number of predicted spam emails: " + predictedSpamEmails);
		System.out.println("number of predicted ham emails: " + predictedHamEmails);
		System.out.println("percentage of predicted spam emails: " + (predictedSpamEmails/spamEmails)*100);
		System.out.println("percentage of predicted ham emails: " + (predictedHamEmails/hamEmails)*100);
	}
	
	// used to fit the provided input and display its class.
	public void fit(String input){
		Email tempEmail = new Email(input);
		ArrayList<Email> tempList = new ArrayList<Email>();
		tempList.add(tempEmail);
		preprocess(tempList);
		
		for (Email email: tempList){
			BigDecimal spamProbability = this.spamClassProbability;
			BigDecimal hamProbability = this.hamClassProbability;
			
			for (Word word: email.getFilteredEmail()){
				if (this.vocabulary.contains(word)){
					spamProbability = spamProbability.multiply(vocabulary.get(vocabulary.indexOf(word)).getSpamProbability());
					hamProbability = hamProbability.multiply(vocabulary.get(vocabulary.indexOf(word)).getHamProbability());
				}
				else{
					double spamEmailsCount = 0, hamEmailsCount = 0;
					
					for (Email tempEmail2: this.exampleEmails){
						if (tempEmail2.getClassName().equals("ham")){
							hamEmailsCount++;
						}
						else{
							spamEmailsCount++;
						}
					}
					
					spamProbability = spamProbability.multiply(new BigDecimal(1/(spamEmailsCount + this.vocabulary.size() + 1)));
					hamProbability = hamProbability.multiply(new BigDecimal(1/(hamEmailsCount + this.vocabulary.size() + 1)));
				}
			}
			
			
			if (spamProbability.compareTo(hamProbability) == 1){
				System.out.println("this is spam");
			}
			else if (spamProbability.compareTo(hamProbability) == -1){
				System.out.println("this is ham");
			}
			else {
				System.out.println("can't decide, equal probabilities!!");
			}
		}
	}
	
	// this is the learn function. it will have the Naive Bayes algorithm.
	// it will read the training data from file.
	public void learn(){
		int itemsDone = 0, totalItems = 0;
		
		// read the training data and preprocess it.
		//readTrainingData();
		System.out.println("started learning");
		readTrainingData();
		preprocess(this.exampleEmails);
		
		// collect all (unrepeated!!) vocabulary.
		System.out.println("started collecting vocabulary words.");
		totalItems = this.exampleEmails.size();
		for (Email email: this.exampleEmails){
			for (Word word: email.getFilteredEmail()){
				if (vocabulary.contains(word)){
					continue;
				}
				
				vocabulary.add(word);
			}
			itemsDone++;
			System.out.println("done collecting words from " + itemsDone + "//" + totalItems + " Emails");
		}
		
		for (String className: this.classes){
			System.out.println("starting calculations for class " + className);
			ArrayList<Email> classEmails = new ArrayList<Email>();
			ArrayList<Word> classWords = new ArrayList<Word>();
			
			// the subset of emails from the training data that are in this class.
			System.out.println("starting to collect emails for class " + className);
			totalItems = this.exampleEmails.size();
			itemsDone = 0;
			for (Email email: this.exampleEmails){
				if (email.getClassName().equals(className)){
					classEmails.add(email);
				}
				itemsDone++;
				System.out.println("done checking " + itemsDone + "//" + totalItems + " Emails");
			}
			
			// calculate the probability of the class.
			System.out.println("calculating the probability for class " + className);
			if (className.equals("spam")){
				this.spamClassProbability = new BigDecimal((classEmails.size() * 1.0)/(this.exampleEmails.size() * 1.0));
			}
			else{
				this.hamClassProbability = new BigDecimal((classEmails.size() * 1.0)/(this.exampleEmails.size() * 1.0));
			}
			
			// collect all words in this class.
			System.out.println("adding all words into one list");
			for (Email email: classEmails){
				classWords.addAll(email.getFilteredEmail());
			}
			
			int distinctWordsCount = classWords.size();
			
			System.out.println("starting to calculate the ptobability of class " + className + " for all vocabulary words.");
			itemsDone = 0;
			totalItems = this.vocabulary.size();
			for (Word word: vocabulary){
				int wordCount = 0;
				for (Word tempWord: classWords){
					if (word.equals(tempWord)){
						wordCount++;
					}
				}
				
				BigDecimal classProbability = new BigDecimal(((wordCount + 1) * 1.0)/((distinctWordsCount + vocabulary.size()) * 1.0));
				if (className.equals("spam")){
					word.setSpamProbability(classProbability);
				}
				else{
					word.setHamProbability(classProbability);
				}
				itemsDone++;
				System.out.println("done calculating " + itemsDone + "//" + totalItems + " words");
			}
		}
		System.out.println("done learning");
	}
	
	// this function will be used to preprocess an arrayList of emails.
	// this function uses the 'Stanford corenlp' library to preprocess each email.
	private void preprocess(ArrayList<Email> emails){
		int emailsDone = 0, totalEmails = emails.size();
		System.out.println("started preprocessing");
		if (this.pipeline == null){
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma");
			this.pipeline = new StanfordCoreNLP(props);
		}
		
		for (Email email: emails){
			ArrayList<String> words = new ArrayList<String>();
			ArrayList<Word> filteredEmail = new ArrayList<Word>();
			
			Annotation emailAnnotation = new Annotation(email.getEmailText());
			this.pipeline.annotate(emailAnnotation);
			
			List<CoreMap> sentences = emailAnnotation.get(SentencesAnnotation.class);
			for(CoreMap sentence: sentences) {
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					words.add(token.get(LemmaAnnotation.class));
				}
			}
			
			words.removeAll(this.stopWords);
			
			for (String wordText: words){
				if (!Pattern.matches("(?!([^']*'){2})['\\p{L}]+", wordText)){
					continue;
				}
				
				Word word = new Word(wordText);
				filteredEmail.add(word);
			}
			
			email.setFilteredEmail(filteredEmail);
			emailsDone++;
			System.out.println("done preprocessing " + emailsDone + "\\" + totalEmails + " Emails.");
		}
		System.out.println("done preprocessing");
	}
	
	// used to read training emails from file and save each of them as an email class.
	private void readTrainingData(){
		System.out.println("starting to read training emails.");
		String currentDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
		//String hamDirectory = currentDirectory + "\\trainingData\\ham";
		//String spamDirectory = currentDirectory + "\\trainingData\\spam";
		File hamDirectory = new File(currentDirectory + "\\trainingData\\ham");
		File spamDirectory = new File(currentDirectory + "\\trainingData\\spam");
		
		for (File file: hamDirectory.listFiles()){
			String emailText = "", tempString = "";
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader read = new BufferedReader(fileReader);
				
				while ((tempString = read.readLine()) != null){
					emailText = emailText + tempString;
				}
				
				emailText = emailText.trim();
				Email testingEmail = new Email(emailText, "ham");
				this.exampleEmails.add(testingEmail);
				
				read.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (File file: spamDirectory.listFiles()){
			String emailText = "", tempString = "";
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader read = new BufferedReader(fileReader);
				
				while ((tempString = read.readLine()) != null){
					emailText = emailText + tempString;
				}
				
				emailText = emailText.trim();
				Email testingEmail = new Email(emailText, "spam");
				this.exampleEmails.add(testingEmail);
				
				read.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("finished reading training emails.");
	}
	
	// used to read testing emails from file and save each of them them as an email class.
	private void readTestingData(){
		String emailText;
		try {
			FileReader file = new FileReader("testingData.txt");
			BufferedReader read = new BufferedReader(file);
			
			while ((emailText = read.readLine()) != null){
				emailText = emailText.trim();
				Email testingEmail = new Email(emailText);
				this.testEmails.add(testingEmail);
			}
			
			read.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// used to read testing emails from file and save each of them them as an email class.
	private void readTestingDataNew(){
		System.out.println("starting to read testing emails.");
		String currentDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
		//String hamDirectory = currentDirectory + "\\trainingData\\ham";
		//String spamDirectory = currentDirectory + "\\trainingData\\spam";
		File hamDirectory = new File(currentDirectory + "\\testingData\\ham");
		File spamDirectory = new File(currentDirectory + "\\testingData\\spam");
		
		for (File file: hamDirectory.listFiles()){
			String emailText = "", tempString = "";
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader read = new BufferedReader(fileReader);
				
				while ((tempString = read.readLine()) != null){
					emailText = emailText + tempString;
				}
				
				emailText = emailText.trim();
				Email testingEmail = new Email(emailText, "ham");
				this.testEmails.add(testingEmail);
				
				read.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (File file: spamDirectory.listFiles()){
			String emailText = "", tempString = "";
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader read = new BufferedReader(fileReader);
				
				while ((tempString = read.readLine()) != null){
					emailText = emailText + tempString;
				}
				
				emailText = emailText.trim();
				Email testingEmail = new Email(emailText, "spam");
				this.testEmails.add(testingEmail);
				
				read.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("finished reading testing emails.");
	}
	
	private void loadStopWordsList(){
		String stopWord;
		try {
			FileReader file = new FileReader("stopWordsShort.txt");
			BufferedReader read = new BufferedReader(file);
			
			while ((stopWord = read.readLine()) != null){
				stopWord = stopWord.trim();
				this.stopWords.add(stopWord);
			}
			
			read.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
